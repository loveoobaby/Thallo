package com.yss.thallo.web;

import com.yss.thallo.conf.ThalloConfiguration;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebVerticle extends AbstractVerticle {

    private Logger logger = LoggerFactory.getLogger(WebVerticle.class);

    public volatile static int port;

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);

        {
            // File upload demo
            router.route("/static/*").handler(StaticHandler.create());
            router.get("/").handler(routingContext -> routingContext.reroute("/static/index.html"));
            router.get("/umi.css").handler(routingContext -> routingContext.reroute("/static/umi.css"));
            router.get("/umi.js").handler(routingContext -> routingContext.reroute("/static/umi.js"));
            router.routeWithRegex("/static/.*jpg").handler(routingContext -> {
                String picture = routingContext.request().uri();
                routingContext.reroute("/static/" + picture);
            });
        }

        router.route("/hello").handler(routingContext -> {
            routingContext.response().putHeader("content-type", "text/html").end("Hello World!");
        });
        vertx.createHttpServer().requestHandler(router).listen(port, r->{
            if(r.succeeded()){
                logger.info("listen {}", port);
            }
        });

    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }
}
