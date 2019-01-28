package com.yss.thallo.api;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.codehaus.jettison.json.JSONObject;

public abstract class ContainerReporter extends Thread {
    protected ContainerId containerId;
    protected JsonObject metrix;
    private EventBus eventBus;


    public ContainerReporter(){
        this.eventBus = Vertx.vertx().eventBus();
    }

    public abstract void updateProcessInfo();



    @Override
    public void run() {
        while (true){
            this.updateProcessInfo();
            eventBus.send("web", this.metrix);
        }
    }
}
