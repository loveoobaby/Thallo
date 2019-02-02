package com.yss.thallo.AM;

import com.google.common.collect.Maps;
import com.yss.thallo.api.ApplicationContainerProtocol;
import com.yss.thallo.api.ThalloConstants;
import com.yss.thallo.conf.ThalloConfiguration;
import com.yss.thallo.reporter.DockerReporter;
import com.yss.thallo.util.Utilities;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.Map;

public class DockerProxyContainer {

    private static final Logger logger = LoggerFactory.getLogger(DockerProxyContainer.class);

    private ApplicationContainerProtocol amClient;

    private DockerReporter reporter;

    private ContainerId containerId;


    private void init() {
        String appMasterHost = System.getenv(ThalloConstants.Environment.THALLO_AM_HOSTNAME.name());
        int appMasterPort = Integer.valueOf(System.getenv(ThalloConstants.Environment.THALLO_AM_PORT.name()));
        InetSocketAddress addr = new InetSocketAddress(appMasterHost, appMasterPort);
        Configuration conf = new ThalloConfiguration();
        try {
            amClient = RPC.getProxy(ApplicationContainerProtocol.class,
                    ApplicationContainerProtocol.versionID, addr, conf);

        } catch (IOException e) {
            logger.error("Connecting to ApplicationMaster " + appMasterHost + ":" + appMasterPort + " failed!");
            logger.error("Container will suicide!");
            System.exit(1);
        }
        containerId = ConverterUtils
                .toContainerId(System.getenv(ApplicationConstants.Environment.CONTAINER_ID.name()));
        reporter = new DockerReporter(containerId, amClient);
    }

    private void run() {

        String LOG_DIRS = System.getenv(ApplicationConstants.Environment.LOG_DIRS.name());
//        Map<String, String> env = Maps.newHashMap();
//        env.put("USER", "yarn");
        String[] env = new String[]{"USER=yarn"};
        // 启动Docker
        String command = String.format("docker run -i --rm --name %s  training/webapp python app.py", containerId.toString(), LOG_DIRS, LOG_DIRS);
        try {
            logger.info("docker command: {}", command);
            Process process = Runtime.getRuntime().exec(command, env);

            new Thread(() -> {
                InputStream is = process.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                InputStream err = process.getErrorStream();
                BufferedReader errReader = new BufferedReader(new InputStreamReader(err));
                String line;
                try {
                    while ((line = br.readLine()) != null || (line = errReader.readLine()) != null) {
                        logger.info(line);
                    }
                    logger.info("docker run exit code = " + process.exitValue());
                    System.exit(process.exitValue());
                } catch (IOException e) {
                    logger.error("", e);
                }

            }).start();

        } catch (IOException e) {
            logger.error("", e);
        }

        String localHostName = System.getenv(ApplicationConstants.Environment.NM_HOST.name());
        amClient.registerContainer(containerId.toString(), localHostName, "training/webapp");

        reporter.start();

        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            String killContainerCmd = "docker kill " + containerId;

            try {
                logger.info("exec kill container {} at shutdown hook!", containerId);
                Runtime.getRuntime().exec(killContainerCmd);
            } catch (IOException e) {
                logger.error("", e);
            }
        }));
    }

    public static void main(String[] args) {

        DockerProxyContainer proxy = new DockerProxyContainer();
        proxy.init();
        proxy.run();
    }

}
