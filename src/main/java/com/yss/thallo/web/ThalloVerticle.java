package com.yss.thallo.web;

import com.yss.thallo.conf.ThalloConfiguration;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThalloVerticle extends AbstractVerticle {

    private Logger logger = LoggerFactory.getLogger(ThalloVerticle.class);

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);

        router.route("/hello").handler(routingContext -> {
            routingContext.response().putHeader("content-type", "text/html").end("Hello World!");
        });
        vertx.createHttpServer().requestHandler(router).listen(8080);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }
}
