package com.yss.thallo.AM;

import com.yss.thallo.Message.MsgWapper;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;

public class AppMasterVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        super.start();
        EventBus eb = vertx.eventBus();
        eb.consumer("am", msg -> {
            MsgWapper wapper = (MsgWapper) msg.body();
            System.out.println(wapper);
        });

    }
}
