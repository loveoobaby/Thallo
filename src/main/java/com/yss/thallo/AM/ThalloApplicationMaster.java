package com.yss.thallo.AM;


import com.yss.thallo.conf.ThalloConfiguration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.ipc.RPC;
import org.apache.hadoop.service.CompositeService;
import org.apache.hadoop.yarn.api.protocolrecords.AllocateResponse;
import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.Priority;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.client.api.AMRMClient;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

public class ThalloApplicationMaster {

    private static Logger logger = LoggerFactory.getLogger(ThalloConfiguration.class);

    private static class ApplicationMaster extends CompositeService {

        private ThalloConfiguration conf;

        public ApplicationMaster() {
            super(ApplicationMaster.class.getName());

            conf = new ThalloConfiguration();
//            System.setProperty(XLearningConstants.Environment.HADOOP_USER_NAME.toString(), conf.get("hadoop.job.ugi").split(",")[0]);

        }

        private void init() {
        }

        private void run() throws InterruptedException, IOException, YarnException {
            logger.info("run ...........");
            AMRMClient amClient = AMRMClient.createAMRMClient();
            amClient.init(conf);
            amClient.start();

            try {
                logger.info("registerApplicationMaster ...........");
                amClient.registerApplicationMaster(InetAddress.getLocalHost().getCanonicalHostName(), 0, "");
                for (int i = 0; i < 10 * 60; i++) {
                    amClient.allocate(0.5f);
                    Thread.sleep(1000);
                }
                amClient.unregisterApplicationMaster(FinalApplicationStatus.SUCCEEDED, "", "");
            } catch (Exception e) {
                logger.error("", e);
                amClient.unregisterApplicationMaster(FinalApplicationStatus.FAILED, "", "");
            }

        }

    }


    public static void main(String[] args) {
        ApplicationMaster appMaster;
        try {
            appMaster = new ApplicationMaster();
            appMaster.init();
            appMaster.run();
        } catch (Exception e) {
            logger.error("Error running ApplicationMaster", e);
            System.exit(1);
        }
    }


}
