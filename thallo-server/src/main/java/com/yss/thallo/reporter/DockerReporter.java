package com.yss.thallo.reporter;

import com.yss.thallo.api.ApplicationContainerProtocol;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DockerReporter extends ContainerReporter {

    private Logger logger = LoggerFactory.getLogger(DockerReporter.class);

    public DockerReporter(ContainerId containerId, ApplicationContainerProtocol amClient) {
        super(containerId);
    }

    @Override
    public void updateProcessInfo() {

    }

    @Override
    public void sendToMaster() {
        logger.info("sendToMaster");
    }
}
