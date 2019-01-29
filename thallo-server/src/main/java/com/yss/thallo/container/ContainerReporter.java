package com.yss.thallo.container;

import com.yss.thallo.AM.Launcher;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ContainerReporter extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(ContainerReporter.class);
    protected ContainerId containerId;
    protected JsonObject metrix;
    private EventBus eventBus;


    public ContainerReporter(ContainerId containerId) {
        this.containerId = containerId;
        this.eventBus = Launcher.getVertx().eventBus();
        this.metrix = new JsonObject();
        this.metrix.put("containerId", containerId.toString());
    }

    public abstract void updateProcessInfo() throws Exception;

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(30 * 1000);
                logger.info("update metrix");
                this.updateProcessInfo();
                eventBus.send("web", this.metrix);
            } catch (Exception e) {
                logger.error("", e);
            }

        }
    }
}
