package com.yss.thallo.web;

import com.yss.thallo.Message.CustomMessage;
import com.yss.thallo.conf.ThalloConfiguration;
import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebVerticle extends AbstractVerticle {

    private Logger logger = LoggerFactory.getLogger(WebVerticle.class);

    public volatile static int port;

    private static final JsonObject config = new JsonObject()
            .put("url", "jdbc:hsqldb:file:db/thallo")
            .put("driver_class", "org.hsqldb.jdbcDriver")
            .put("max_pool_size", 10);

    private JDBCClient jdbcClient;


    @Override
    public void start(Future<Void> startFuture) throws Exception {

        jdbcClient = JDBCClient.createShared(vertx, config, "jdbc");

        Future<Void> fut1 = Future.future();
        startWebApp((http) -> {
            if(http.succeeded()){
                logger.info("start listen {}", port);
                fut1.complete();
            }else {
                startFuture.fail(http.cause());
            }
        });

        fut1.compose(v -> {
                    jdbcClient.getConnection(ar -> {
                        if (ar.failed()) {
                            startFuture.fail(ar.cause());
                        } else {
                            initDBData(Future.succeededFuture(ar.result()), startFuture);
                        }
                    });
                }, startFuture);

//        vertx.eventBus().consumer("web", msg -> {
//
//        });

    }

    private void startWebApp(Handler<AsyncResult<HttpServer>> next) {
        Router router = Router.router(vertx);

        {
            // 处理静态资源
            router.route("/static/*").handler(StaticHandler.create());
            router.get("/").handler(routingContext -> routingContext.reroute("/static/index.html"));
            router.get("/umi.css").handler(routingContext -> routingContext.reroute("/static/umi.css"));
            router.get("/umi.js").handler(routingContext -> routingContext.reroute("/static/umi.js"));
            router.routeWithRegex("/static/.*jpg").handler(routingContext -> {
                String picture = routingContext.request().uri();
                routingContext.reroute("/static/" + picture);
            });
        }

        {
            //关闭服务
            router.get("/stop").handler(this::stopService);
        }

        {
            // 获取监控数据
        }

        vertx.createHttpServer().requestHandler(router).listen(port, next::handle);
    }


    private void initDBData(AsyncResult<SQLConnection> result, Future<Void> fut) {
        if (result.failed()) {
            fut.fail(result.cause());
        } else {
            SQLConnection connection = result.result();
            connection.execute(
                    "CREATE TABLE IF NOT EXISTS persons (id INTEGER IDENTITY, name varchar(100), origin varchar" +
                            "(100))",
                    ar -> {
                        if (ar.failed()) {
                            fut.fail(ar.cause());
                            connection.close();
                            return;
                        }
                    });
        }
    }



    public void stopService(RoutingContext routingContext){
        vertx.eventBus().send("am", new CustomMessage("stop", null));
    }


}
