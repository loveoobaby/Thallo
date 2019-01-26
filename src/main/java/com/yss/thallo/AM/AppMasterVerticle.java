package com.yss.thallo.AM;

import com.yss.thallo.Message.CustomMessage;
import com.yss.thallo.Message.CustomMessageCodec;
import com.yss.thallo.Message.MsgWapper;
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
        super.start();
        EventBus eb = vertx.eventBus();
        eb.registerDefaultCodec(CustomMessage.class, new CustomMessageCodec());
        eb.consumer("am", msg -> {
            CustomMessage wapper = (CustomMessage) msg.body();
            logger.info("wapper =" + wapper);
        });

    }
}
