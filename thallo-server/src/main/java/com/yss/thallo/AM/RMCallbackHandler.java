package com.yss.thallo.AM;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yss.thallo.NM.DockerProxyContainer;
import com.yss.thallo.api.ThalloConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.records.*;
import org.apache.hadoop.yarn.client.api.async.AMRMClientAsync.CallbackHandler;
import org.apache.hadoop.yarn.client.api.async.NMClientAsync;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.apache.hadoop.yarn.util.Records;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class RMCallbackHandler implements CallbackHandler {
    private static final Log LOG = LogFactory.getLog(RMCallbackHandler.class);

    private final List<Container> cancelContainers;

    public final List<Container> acquiredContainers;

    public final Set<String> blackHosts;

    private int neededContainersCount;

    private final AtomicInteger acquiredContainersCount;

    private final AtomicBoolean containersAllocating;

    public final List<ThalloContainer> needDockerContainers;

    public final List<ThalloContainer> startFailedContainer;

    private final NMClientAsync nmAsync;

    private final Configuration conf;

    private float progress;

    private ContainerListener containerListener;

    public RMCallbackHandler(NMClientAsync nmAsync, Configuration conf, ContainerListener containerListener) {
        cancelContainers = Collections.synchronizedList(new ArrayList<Container>());
        acquiredContainers = Collections.synchronizedList(new ArrayList<Container>());
        needDockerContainers = Collections.synchronizedList(new ArrayList<ThalloContainer>());
        blackHosts = Collections.synchronizedSet(new HashSet<String>());
        acquiredContainersCount = new AtomicInteger(0);
        containersAllocating = new AtomicBoolean(false);
        startFailedContainer = Collections.synchronizedList(new ArrayList<>());
        progress = 0.0f;
        this.nmAsync = nmAsync;
        this.conf = conf;
        this.containerListener = containerListener;
    }

    public List<String> getBlackHosts() {
        List<String> blackHostList = new ArrayList<>(blackHosts.size());
        for (String host : blackHosts) {
            blackHostList.add(host);
        }
        return blackHostList;
    }

    public int getAllocatedWorkerContainerNumber() {
        return acquiredContainersCount.get();
    }

    public int getAllocatedContainerNumber() {
        return acquiredContainersCount.get();
    }

    public List<Container> getCancelContainer() {
        return cancelContainers;
    }

    public List<Container> getAcquiredContainer() {
        return new ArrayList<>(acquiredContainers);
    }


    public void setNeededContainersCount(int count) {
        neededContainersCount = count;
    }

    public void setContainersAllocating() {
        containersAllocating.set(true);
    }

    @Override
    public void onContainersCompleted(List<ContainerStatus> containerStatuses) {
        for (ContainerStatus containerStatus : containerStatuses) {
            LOG.info("Container " + containerStatus.getContainerId() + " completed with status "
                    + containerStatus.getState().toString());
        }
    }

    @Override
    public void onContainersAllocated(List<Container> containers) {
        for (Container acquiredContainer : containers) {
            LOG.info("Acquired container " + acquiredContainer.getId()
                    + " on host " + acquiredContainer.getNodeId().getHost()
                    + " , with the resource " + acquiredContainer.getResource().toString());
            String host = acquiredContainer.getNodeId().getHost();
            if (!blackHosts.contains(host)) {
                Resource resource = acquiredContainer.getResource();
                ThalloContainer allocted = new ThalloContainer();
                allocted.setMemeory(resource.getMemory());
                allocted.setVcores(resource.getVirtualCores());

                for (int i = 0; i < needDockerContainers.size(); i++) {
                    if (needDockerContainers.get(i).equals(allocted)) {
                        startContainer(acquiredContainer, needDockerContainers.get(i));
                        needDockerContainers.remove(i);
                        break;
                    }
                }
            } else {
                LOG.info("Add container " + acquiredContainer.getId() + " to cancel list");
                cancelContainers.add(acquiredContainer);
            }
        }

    }

    @Override
    public float getProgress() {
        return progress;
    }

    public void setProgress(float reportProgress) {
        this.progress = reportProgress;
    }

    @Override
    public void onShutdownRequest() {
    }

    @Override
    public void onNodesUpdated(List<NodeReport> updatedNodes) {
    }

    @Override
    public void onError(Throwable e) {
        LOG.error("Error from RMCallback: ", e);
    }

    private void startContainer(Container container, ThalloContainer thalloContainer) {
        List<String> containerCmd = buildContainerLaunchCommand(thalloContainer);
        Map<String, LocalResource> containerLocalResource = prepareContainerResource();
        Map<String, String> containerEnv = prepareContainerEnv();

        //TODO launch container in special thread take with fault-tolerant
        try {
            launchContainer(containerLocalResource, containerEnv,
                    containerCmd, container);
        } catch (IOException e) {
            LOG.error("", e);
            startFailedContainer.add(thalloContainer);
        }

    }

    private void launchContainer(Map<String, LocalResource> containerLocalResource,
                                 Map<String, String> containerEnv,
                                 List<String> containerLaunchcommands,
                                 Container container) throws IOException {
        LOG.info("Setting up launch context for containerID="
                + container.getId());
        LOG.info("container command = {}" + containerLaunchcommands);
        LOG.info("container env = " + containerEnv);

        ContainerLaunchContext ctx = ContainerLaunchContext.newInstance(
                containerLocalResource, containerEnv, containerLaunchcommands, null, null, null);

        try {
            nmAsync.startContainerAsync(container, ctx);
        } catch (Exception e) {
            throw new RuntimeException("Launching container " + container.getId() + " failed!");
        }
    }


    private List<String> buildContainerLaunchCommand(ThalloContainer container) {
        List<String> containerLaunchcommands = new ArrayList<>();
        LOG.info("Setting up container command");
        Vector<CharSequence> vargs = new Vector<>(10);
        vargs.add("${JAVA_HOME}" + "/bin/java");
        vargs.add("-Xmx" + 125 + "m");
        vargs.add("-Xms" + 125 + "m");
        vargs.add(DockerProxyContainer.class.getName());
        vargs.add("1>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/" + ApplicationConstants.STDOUT);
        vargs.add("2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/" + ApplicationConstants.STDERR);

        StringBuilder containerCmd = new StringBuilder();
        for (CharSequence str : vargs) {
            containerCmd.append(str).append(" ");
        }
        containerLaunchcommands.add(containerCmd.toString());
        LOG.info("Container launch command: " + containerLaunchcommands.toString());
        return containerLaunchcommands;
    }

    private Map<String, String> prepareContainerEnv() {
        Map<String, String> env = new HashMap<>();
        StringBuilder classPathEnv = new StringBuilder();
        classPathEnv.append("$PWD/*");

        for (String c : conf.getStrings(YarnConfiguration.YARN_APPLICATION_CLASSPATH,
                YarnConfiguration.DEFAULT_YARN_APPLICATION_CLASSPATH)) {
            classPathEnv.append(":");
            classPathEnv.append(c.trim());
        }
        env.put("CLASSPATH", classPathEnv.toString());

        env.put(ThalloConstants.Environment.THALLO_AM_HOSTNAME.name(), System.getenv(ApplicationConstants.Environment.NM_HOST.name()));
        env.put(ThalloConstants.Environment.THALLO_AM_PORT.name(), ""+ containerListener.getServicePort());
        return env;
    }

    private Map<String, LocalResource> prepareContainerResource() {

        Map<String, LocalResource> containerLocalResource = Maps.newHashMap();

        String[] resourceName = System.getenv(ThalloConstants.Environment.THALLO_RESOURCE_NAME.name()).split(",");
        String[] resourcePath = System.getenv(ThalloConstants.Environment.THALL_RESOURCE_URL.name()).split(",");
        String[] resourceSize = System.getenv(ThalloConstants.Environment.THALLO_RESOURCE_SIZE.name()).split(",");
        String[] resourceTimeStamp = System.getenv(ThalloConstants.Environment.THALLO_RESOURCE_TIMESTAP.name()).split(",");
        String[] resourceVisib = System.getenv(ThalloConstants.Environment.THALLO_RESOURCE_VISUAL.name()).split(",");

        for (int i = 0; i < resourceName.length; i++) {
            LocalResource localResource = Records.newRecord(LocalResource.class);
            localResource.setResource(ConverterUtils.getYarnUrlFromPath(new Path(resourcePath[i])));
            localResource.setSize(Long.valueOf(resourceSize[i]));
            localResource.setTimestamp(Long.valueOf(resourceTimeStamp[i]));
            localResource.setType(LocalResourceType.FILE);
            localResource.setVisibility(LocalResourceVisibility.valueOf(resourceVisib[i]));
            containerLocalResource.put(resourceName[i], localResource);
        }

        return containerLocalResource;
    }


}
