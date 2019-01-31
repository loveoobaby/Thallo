package com.yss.thallo.reporter;

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
        this.metrix.put("msgType", "monitor");
        this.metrix.put("containerId", containerId.toString());
    }

    public abstract void updateProcessInfo() throws Exception;

    @Override
    public void run() {
        while (true) {
            try {
                logger.info("updateProcessInfo");
                this.updateProcessInfo();
                eventBus.send("web", this.metrix);
                Thread.sleep(5 * 1000);
            } catch (Exception e) {
                logger.error("", e);
            }

        }
    }
}
