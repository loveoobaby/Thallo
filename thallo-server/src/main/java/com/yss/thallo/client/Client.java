package com.yss.thallo.client;


import com.yss.thallo.AM.Launcher;
import com.yss.thallo.conf.ThalloConfiguration;
import com.yss.thallo.util.Utilities;
import org.apache.commons.cli.ParseException;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.protocolrecords.GetNewApplicationResponse;
import org.apache.hadoop.yarn.api.records.*;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.client.api.YarnClientApplication;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.util.Records;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Client {

    private static Logger logger = LoggerFactory.getLogger(Client.class);

    private ThalloConfiguration conf;
    private final FileSystem dfs;
    private YarnClient yarnClient;
    private ClientArguments clientArguments;
    private YarnClientApplication newApp;
    private ApplicationId applicationId;
    private FsPermission JOB_FILE_PERMISSION;

    private Client(String[] args) throws IOException, ParseException {
        this.conf = new ThalloConfiguration();
        this.dfs = FileSystem.get(conf);
        this.clientArguments = new ClientArguments(args);
        JOB_FILE_PERMISSION = FsPermission.createImmutable((short) 0644);
    }

    private void init() throws IOException, YarnException {
        String appSubmitterUserName = System.getenv(ApplicationConstants.Environment.USER.name());
        if (conf.get("hadoop.job.ugi") == null) {
            UserGroupInformation ugi = UserGroupInformation.createRemoteUser(appSubmitterUserName);
            conf.set("hadoop.job.ugi", ugi.getUserName() + "," + ugi.getUserName());
        }
        conf.set(ThalloConfiguration.THALLO_AM_MEMORY, String.valueOf(clientArguments.amMem));
        conf.set(ThalloConfiguration.THALLO_AM_VCORES, String.valueOf(clientArguments.amCores));
        conf.set(ThalloConfiguration.THALLO_QUEUE, clientArguments.queue);

        yarnClient = YarnClient.createYarnClient();
        yarnClient.init(conf);
        yarnClient.start();

        logger.info("Requesting a new application from cluster with " + yarnClient.getYarnClusterMetrics().getNumNodeManagers() + " NodeManagers");
        newApp = yarnClient.createApplication();

    }

    private boolean submitAndMonitor() throws IOException, YarnException {
        GetNewApplicationResponse newAppResponse = newApp.getNewApplicationResponse();
        applicationId = newAppResponse.getApplicationId();
        logger.info("Got new Application: " + applicationId.toString());


        Path jobConfPath = uploadConfigFile();
        Path appMasterJarPath = uploadAppMasterJar();

        Map<String, LocalResource> resources = prepareLocalResource(jobConfPath, appMasterJarPath);

        ContainerLaunchContext amClc = Records.newRecord(ContainerLaunchContext.class);
        amClc.setLocalResources(resources);
        amClc.setEnvironment(prepareEnv());

        List<String> appMasterArgs = new ArrayList<>(20);
        appMasterArgs.add("${JAVA_HOME}" + "/bin/java");
        appMasterArgs.add("-Xms" + conf.getInt(ThalloConfiguration.THALLO_AM_MEMORY, ThalloConfiguration.DEFAULT_THALLO_AM_MEMORY) + "m");
        appMasterArgs.add("-Xmx" + conf.getInt(ThalloConfiguration.THALLO_AM_MEMORY, ThalloConfiguration.DEFAULT_THALLO_AM_MEMORY) + "m");
        appMasterArgs.add(Launcher.class.getName());
        appMasterArgs.add("1>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR
                + "/" + ApplicationConstants.STDOUT);
        appMasterArgs.add("2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR
                + "/" + ApplicationConstants.STDERR);
        amClc.setCommands(appMasterArgs);

        ApplicationSubmissionContext appSc = newApp.getApplicationSubmissionContext();
        appSc.setApplicationName(clientArguments.appName);
        appSc.setApplicationType(clientArguments.appType);
        appSc.setAMContainerSpec(amClc);
        appSc.setQueue(clientArguments.queue);

        Resource amResource = Records.newRecord(Resource.class);
        amResource.setMemory(conf.getInt(ThalloConfiguration.THALLO_AM_MEMORY, ThalloConfiguration.DEFAULT_THALLO_AM_MEMORY));
        amResource.setVirtualCores(conf.getInt(ThalloConfiguration.THALLO_AM_VCORES, ThalloConfiguration.DEDAULT_THALLO_AM_VCORE));
        appSc.setResource(amResource);

        yarnClient.submitApplication(appSc);


        return true;
    }

    private Path uploadConfigFile() throws IOException {
        Path jobConfPath = Utilities.getRemotePath(conf, applicationId, ThalloConfiguration.THALLO_JOB_CONFIGURATION);
        FSDataOutputStream out =
                FileSystem.create(jobConfPath.getFileSystem(conf), jobConfPath,
                        new FsPermission(JOB_FILE_PERMISSION));
        conf.writeXml(out);
        out.close();
        return jobConfPath;
    }

    private Path uploadAppMasterJar() throws IOException {
        Path appJarSrc = new Path(clientArguments.appMasterJar);
        Path appJarDst = Utilities
                .getRemotePath(conf, applicationId, ThalloConfiguration.THALLO_APP_MASTER_NAME);
        logger.info("Copying " + appJarSrc + " to remote path " + appJarDst.toString());
        dfs.copyFromLocalFile(false, true, appJarSrc, appJarDst);
        return appJarDst;
    }

    private Map<String, LocalResource> prepareLocalResource(Path configFile, Path jarPath) throws IOException {
        Map<String, LocalResource> localResource = new HashMap<>();
        localResource.put(configFile.getName(), Utilities.createApplicationResource(dfs, configFile, LocalResourceType.FILE));
        localResource.put(jarPath.getName(), Utilities.createApplicationResource(dfs, jarPath, LocalResourceType.FILE));
        return localResource;
    }

    private Map<String, String> prepareEnv() {
        Map<String, String> env = new HashMap<>();
        StringBuilder classPathEnv = new StringBuilder();
        classPathEnv.append("$PWD/*");

        for (String c : conf.getStrings(YarnConfiguration.YARN_APPLICATION_CLASSPATH,
                YarnConfiguration.DEFAULT_YARN_APPLICATION_CLASSPATH)) {
            classPathEnv.append(":");
            classPathEnv.append(c.trim());
        }
        env.put("CLASSPATH", classPathEnv.toString());
        return env;
    }


    private static void showWelcome() {
        System.err.println("Welcome to\n " +
                "\n" +
                "  _____  _             _  _        \n" +
                " |_   _|| |__    __ _ | || |  ___  \n" +
                "   | |  | '_ \\  / _` || || | / _ \\ \n" +
                "   | |  | | | || (_| || || || (_) |\n" +
                "   |_|  |_| |_| \\__,_||_||_| \\___/ \n" +
                "                                   \n"
        );
    }


    public static void main(String[] args) {
        showWelcome();
        try {
            logger.info("Initializing Client");
            Client client = new Client(args);
            client.init();
            client.submitAndMonitor();

        } catch (Exception e) {
            logger.error("Error run Client", e);
            System.exit(1);
        }


    }
}
