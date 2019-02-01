package com.yss.thallo.NM;

import com.yss.thallo.api.ApplicationContainerProtocol;
import com.yss.thallo.api.ThalloConstants;
import com.yss.thallo.conf.ThalloConfiguration;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

public class DockerProxyContainer {

    private static final Logger logger = LoggerFactory.getLogger(DockerProxyContainer.class);

    public static void main(String[] args) {
        String appMasterHost = System.getenv(ThalloConstants.Environment.THALLO_AM_HOSTNAME.name());
        int appMasterPort = Integer.valueOf(System.getenv(ThalloConstants.Environment.THALLO_AM_PORT.name()));
        InetSocketAddress addr = new InetSocketAddress(appMasterHost, appMasterPort);
        Configuration conf = new ThalloConfiguration();
        ApplicationContainerProtocol amClient = null;
        try {
            amClient = RPC.getProxy(ApplicationContainerProtocol.class,
                    ApplicationContainerProtocol.versionID, addr, conf);

        } catch (IOException e) {
            logger.error("Connecting to ApplicationMaster " + appMasterHost + ":" + appMasterPort + " failed!");
            logger.error("Container will suicide!");
            System.exit(1);
        }

        String containerId = System.getenv(ApplicationConstants.Environment.CONTAINER_ID.name());
        String hostName = System.getenv(ApplicationConstants.Environment.NM_HOST.name());
        amClient.registerContainer(containerId, hostName, "ubuntu:14.04");

        while (true){
            logger.info("registerContainer: {}", containerId);
            amClient.heartbeat(containerId);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
