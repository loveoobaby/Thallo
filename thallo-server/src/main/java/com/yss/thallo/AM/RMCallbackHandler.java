package com.yss.thallo.AM;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.ContainerStatus;
import org.apache.hadoop.yarn.api.records.NodeReport;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.client.api.async.AMRMClientAsync.CallbackHandler;

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

    public final List<DockerContainer> needDockerContainers;

    private float progress;

    public RMCallbackHandler() {
        cancelContainers = Collections.synchronizedList(new ArrayList<Container>());
        acquiredContainers = Collections.synchronizedList(new ArrayList<Container>());
        needDockerContainers = Collections.synchronizedList(new ArrayList<DockerContainer>());
        blackHosts = Collections.synchronizedSet(new HashSet<String>());
        acquiredContainersCount = new AtomicInteger(0);
        containersAllocating = new AtomicBoolean(false);
        progress = 0.0f;
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
                DockerContainer allocted = new DockerContainer();
                allocted.setMemeory(resource.getMemory());
                allocted.setVcores(resource.getVirtualCores());

                for(int i=0; i<needDockerContainers.size(); i++){
                    if(needDockerContainers.get(i).equals(allocted)){
                        startContainer(needDockerContainers.get(i));
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

    private void startContainer(DockerContainer container){


    }

    private List<String> buildContainerLaunchCommand(DockerContainer container) {
        List<String> containerLaunchcommands = new ArrayList<>();
        LOG.info("Setting up container command");
        Vector<CharSequence> vargs = new Vector<>(10);
        vargs.add("${JAVA_HOME}" + "/bin/java");
        vargs.add("-Xmx" + 125 + "m");
        vargs.add("-Xms" + 125 + "m");
//        vargs.add(XLearningContainer.class.getName());
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


}
