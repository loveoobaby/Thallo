package com.yss.thallo.AM;

import com.yss.thallo.Message.CustomMessage;
import com.yss.thallo.Message.CustomMessageCodec;
import com.yss.thallo.api.ApplicationContext;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
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
                    break;
                case "stop":
                    applicationContext.stopService();

            }
        });

    }


}
