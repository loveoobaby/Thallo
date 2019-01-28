package com.yss.thallo.AM;

import com.yss.thallo.Message.CustomMessage;
import com.yss.thallo.Message.MsgWapper;
import com.yss.thallo.conf.ThalloConfiguration;
import com.yss.thallo.util.Utilities;
import com.yss.thallo.web.WebVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher {

    private static final Logger logger = LoggerFactory.getLogger(Launcher.class);



    public static void main(String[] args) {
        System.setProperty("vertx.logger-delegate-factory-class-name",
                "io.vertx.core.logging.SLF4JLogDelegateFactory");
        //先启动Web服务
        Vertx vertx = Vertx.vertx();
        vertx.executeBlocking(future -> {
            int port = Utilities.findUnusedPort(ThalloConfiguration.DEFAULT_THALLO_WEB_PORT);
            future.complete(port);
        }, res -> {
            if (res.succeeded()) {
                logger.info("web service will listen port {}", res.result());
                WebVerticle.port = (int) res.result();
                vertx.deployVerticle(WebVerticle.class, new DeploymentOptions(), r -> {
                    if(r.succeeded()){
                        DeploymentOptions amOptions = new DeploymentOptions()
                                .setWorker(true)
                                .setInstances(1) // matches the worker pool size below
                                .setWorkerPoolName("the-specific-pool")
                                .setWorkerPoolSize(1);
                        vertx.deployVerticle(AppMasterVerticle.class, amOptions, amr ->{
                            if(amr.succeeded()){
//                                vertx.eventBus().send("am", new CustomMessage("init", null));
                            }
                        });
                    }else {
                        logger.error("init web service error", r.cause());
                    }

                });
            } else {
                logger.error("", res.cause());
                System.exit(-1);
            }
        });


    }

}
