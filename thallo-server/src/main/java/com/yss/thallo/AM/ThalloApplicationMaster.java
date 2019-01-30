package com.yss.thallo.AM;


import com.yss.thallo.api.ApplicationContext;
import com.yss.thallo.conf.ThalloConfiguration;
import com.yss.thallo.container.AMReporter;
import com.yss.thallo.container.ContainerReporter;
import com.yss.thallo.web.WebVerticle;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.protocolrecords.RegisterApplicationMasterResponse;
import org.apache.hadoop.yarn.api.records.ApplicationAttemptId;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.client.api.async.AMRMClientAsync;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class ThalloApplicationMaster {

    private static Logger logger = LoggerFactory.getLogger(ThalloApplicationMaster.class);


    private Configuration conf;
    private ApplicationContext applicationContext;
    private ApplicationAttemptId applicationAttemptID;
    private RMCallbackHandler rmCallbackHandler;
    private AMRMClientAsync amrmAsync;
    private String applicationMasterHostname;
    private ContainerReporter reporter;
    private ContainerId containerId;

    public ApplicationContext getApplicationContext() {
        return this.applicationContext;
    }


    public ThalloApplicationMaster() {

        conf = new ThalloConfiguration();
//            System.setProperty(ThalloConstants.Environment.HADOOP_USER_NAME.toString(), conf.get("hadoop.job.ugi").split(",")[0]);

        Map<String, String> envs = System.getenv();
        if (envs.containsKey(ApplicationConstants.Environment.CONTAINER_ID.toString())) {
            containerId = ConverterUtils
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

        logger.info("init am reporter");
        reporter = new AMReporter(containerId);
        reporter.start();
    }

    public void run() throws InterruptedException, IOException, YarnException {
        try {
            logger.info("registerApplicationMaster ...........");
            RegisterApplicationMasterResponse response = amrmAsync.registerApplicationMaster(applicationMasterHostname, 0, "http://" + applicationMasterHostname + ":" + WebVerticle.port);
            Runtime.getRuntime().addShutdownHook(new CleanAMThread());
        } catch (Exception e) {
            logger.error("", e);
            amrmAsync.unregisterApplicationMaster(FinalApplicationStatus.FAILED, "", "");
        }

    }


    private class RunningAppContext implements ApplicationContext {

        @Override
        public ApplicationId getApplicationID() {
            return applicationAttemptID.getApplicationId();
        }

        @Override
        public void stopService() {
            try {
                amrmAsync.unregisterApplicationMaster(FinalApplicationStatus.SUCCEEDED, "", "");
                amrmAsync.stop();
            } catch (Exception e) {

            }
            System.exit(0);
        }

        @Override
        public ContainerId getContainerId() {
            return containerId;
        }

        @Override
        public String getAppMasterHostName() {
            return applicationMasterHostname;
        }

        @Override
        public String getConf(String key) {
            return conf.get(key);
        }
    }


}
