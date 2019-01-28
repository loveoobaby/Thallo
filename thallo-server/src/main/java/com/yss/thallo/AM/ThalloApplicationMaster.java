package com.yss.thallo.AM;


import com.yss.thallo.api.ApplicationContext;
import com.yss.thallo.api.ThalloConstants;
import com.yss.thallo.conf.ThalloConfiguration;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.ipc.RPC;
import org.apache.hadoop.service.CompositeService;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.protocolrecords.AllocateResponse;
import org.apache.hadoop.yarn.api.protocolrecords.RegisterApplicationMasterResponse;
import org.apache.hadoop.yarn.api.records.*;
import org.apache.hadoop.yarn.client.api.AMRMClient;
import org.apache.hadoop.yarn.client.api.async.AMRMClientAsync;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;

public class ThalloApplicationMaster {

    private static Logger logger = LoggerFactory.getLogger(ThalloConfiguration.class);


        private Configuration conf;
        private ApplicationContext applicationContext;
        private ApplicationAttemptId applicationAttemptID;
        private RMCallbackHandler rmCallbackHandler;
        private AMRMClientAsync amrmAsync;
        private String applicationMasterHostname;

        public ApplicationContext getApplicationContext(){
            return this.applicationContext;
        }

        public ThalloApplicationMaster() {

            conf = new ThalloConfiguration();
//            System.setProperty(ThalloConstants.Environment.HADOOP_USER_NAME.toString(), conf.get("hadoop.job.ugi").split(",")[0]);

            Map<String, String> envs = System.getenv();
            if (envs.containsKey(ApplicationConstants.Environment.CONTAINER_ID.toString())) {
                ContainerId containerId = ConverterUtils
                        .toContainerId(envs.get(ApplicationConstants.Environment.CONTAINER_ID.toString()));
                applicationAttemptID = containerId.getApplicationAttemptId();
            } else {
                throw new IllegalArgumentException(
                        "Application Attempt Id is not available in environment");
            }
            this.applicationContext = new RunningAppContext();

            if (envs.containsKey(ApplicationConstants.Environment.NM_HOST.toString())) {
                applicationMasterHostname = envs.get(ApplicationConstants.Environment.NM_HOST.toString());
            }
        }

        public void init() throws Exception {
            this.rmCallbackHandler = new RMCallbackHandler();
            this.amrmAsync = AMRMClientAsync.createAMRMClientAsync(1000, rmCallbackHandler);
            this.amrmAsync.init(conf);
            this.amrmAsync.start();
        }

        public void run() throws InterruptedException, IOException, YarnException {
            try {
                logger.info("registerApplicationMaster ...........");
                RegisterApplicationMasterResponse response = amrmAsync.registerApplicationMaster(applicationMasterHostname, 0, "http://" + applicationMasterHostname + ":8080/hello");
                Runtime.getRuntime().addShutdownHook(new CleanAMThread());
            } catch (Exception e) {
                logger.error("", e);
                amrmAsync.unregisterApplicationMaster(FinalApplicationStatus.FAILED, "", "");
            }

        }



    private class RunningAppContext implements ApplicationContext {

        @Override
        public ApplicationId getApplicationID() {
            return null;
        }

        @Override
        public void stopService() {
            try{
                amrmAsync.unregisterApplicationMaster(FinalApplicationStatus.SUCCEEDED, "", "");
                amrmAsync.stop();
            }catch (Exception e){

            }
            System.exit(0);
        }
    }


}
