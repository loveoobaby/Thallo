package com.yss.thallo.AM;

import com.yss.thallo.Message.CustomMessage;
import com.yss.thallo.Message.CustomMessageCodec;
import com.yss.thallo.api.ApplicationContext;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppMasterVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(AppMasterVerticle.class);
    private ApplicationContext applicationContext;

    @Override
    public void start() throws Exception {
        EventBus eb = vertx.eventBus();
        eb.registerDefaultCodec(CustomMessage.class, new CustomMessageCodec());
        eb.consumer("am", msg -> {
            CustomMessage wapper = (CustomMessage) msg.body();
            logger.info("wapper =" + wapper);
            switch (wapper.getMsgType()) {
                case "init":
                    ThalloApplicationMaster appMaster;
                    try {
                        appMaster = new ThalloApplicationMaster();
                        appMaster.init();
                        appMaster.run();
                        applicationContext = appMaster.getApplicationContext();
                    } catch (Exception e) {
                        logger.error("Error running ApplicationMaster", e);
                        System.exit(1);
                    }
                    eb.send("web", new JsonObject().put("msgType", "registerAm").
                            put("containerId", applicationContext.getContainerId().toString()).
                            put("hostName", applicationContext.getAppMasterHostName()));
                    break;
                case "stop":
                    applicationContext.stopService();
                    break;
                case "meta":
                    JsonObject appMeta = new JsonObject().
                            put("applicationID", applicationContext.getApplicationID().toString()).
                            put("amContainerId", applicationContext.getContainerId().toString()).
                            put("rmWebHost" ,applicationContext.getConf("yarn.resourcemanager.webapp.address"));
                    msg.reply(appMeta);
                    break;

                default:
                    break;

            }
        });

    }


}
