package com.yss.thallo.client;


import com.yss.thallo.conf.ThalloConfiguration;
import com.yss.thallo.util.Utilities;
import org.apache.commons.cli.ParseException;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.protocolrecords.GetNewApplicationResponse;
import org.apache.hadoop.yarn.api.records.*;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.client.api.YarnClientApplication;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.apache.hadoop.yarn.util.Records;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
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


//        String jarPath = "D:\\yarn_app_demo\\yarn_demo_app\\target\\yarn_demo_app-shade.jar";
//        Map<String, LocalResource> resources = prepareLocalResource(jarPath, destPath);
//
//        ContainerLaunchContext amClc = Records.newRecord(ContainerLaunchContext.class);
//        amClc.setLocalResources(resources);
//        amClc.setEnvironment(prepareEnv());
//
//        String userCmd = "$JAVA_HOME/bin/java "  +
//                " -server -Xms" + 100 + "m -Xmx" + 100 + "m" +
//                " " + amMainClass +
//                " 1>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout" +
//                " 2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stderr";
//        amClc.setCommands(Collections.singletonList(userCmd));

//        ApplicationSubmissionContext appSc = newApp.getApplicationSubmissionContext();
//        appSc.setApplicationName("Yarn_demo");
//        appSc.setApplicationType("Yarn_hello");
//        appSc.setAMContainerSpec(amClc);
        //appSc.setQueue();
////            appSc.setPriority(Priority.newInstance(0));
//        Resource amResource = Records.newRecord(Resource.class);
//        amResource.setMemory(1024);
//        amResource.setVirtualCores(1);
//        appSc.setResource(amResource);
//
//        yarnClient.submitApplication(appSc);

//        ApplicationSubmissionContext applicationContext = newAPP.getApplicationSubmissionContext();
//        applicationContext.setApplicationId(applicationId);
//        applicationContext.setApplicationName(clientArguments.appName);
//        applicationContext.setApplicationType(clientArguments.appType);
//

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

    private Path uploadAppMasterJar(){
        return null;
    }

    private Map<String, LocalResource> prepareLocalResource(Path configFile, Path jarPath) throws IOException {
        Map<String, LocalResource> localResource = new HashMap<>();
//        Path jarResource = copyToHDFS(jarPath, hdfsPath);
//        LocalResource resource = Records.newRecord(LocalResource.class);
//        resource.setResource(ConverterUtils.getYarnUrlFromPath(jarResource));
//        resource.setType(LocalResourceType.FILE);
//        resource.setVisibility(LocalResourceVisibility.APPLICATION);
//        FileStatus destStatus = dfs.getFileStatus(jarResource);
//        resource.setSize(destStatus.getLen());
//        resource.setTimestamp(destStatus.getModificationTime());
//        File jarFile = new File(jarPath);
//        localResource.put(jarFile.getName(), resource);
        return localResource;

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
