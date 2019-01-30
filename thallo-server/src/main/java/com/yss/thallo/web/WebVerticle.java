package com.yss.thallo.web;

import com.google.common.collect.Lists;
import com.yss.thallo.Message.CustomMessage;
import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class WebVerticle extends AbstractVerticle {

    private Logger logger = LoggerFactory.getLogger(WebVerticle.class);

    public volatile static int port;

    private static final JsonObject config = new JsonObject()
            .put("url", "jdbc:hsqldb:file:db/thallo")
            .put("driver_class", "org.hsqldb.jdbcDriver")
            .put("max_pool_size", 10);

    private JDBCClient jdbcClient;
    private static AtomicInteger monitorId = new AtomicInteger(0);


    @Override
    public void start(Future<Void> startFuture) throws Exception {

        jdbcClient = JDBCClient.createShared(vertx, config, "jdbc");

        Future<Void> fut1 = Future.future();
        startWebApp((http) -> {
            if (http.succeeded()) {
                logger.info("start listen {}", port);
                fut1.complete();
            } else {
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

        vertx.eventBus().consumer("web", msg -> {
            JsonObject msgData = (JsonObject) msg.body();
            switch (msgData.getString("msgType")) {
                case "monitor":
                    insert("INSERT INTO monitor (id, container_id, cpu, memory, time) VALUES ?, ?, ?, ?, ?",
                            new JsonArray().add(monitorId.incrementAndGet()).
                                    add(msgData.getString("containerId")).add(msgData.getDouble("cpu")).
                                    add(msgData.getDouble("memory")).add(new Date().getTime()));
                    break;
                case "registerAm":
                    insert("INSERT INTO containers (container_id, host_name, role) VALUES ?, ?, ?",
                            new JsonArray().add(msgData.getString("containerId")).add(msgData.getString("hostName")).add("ApplicationMaster"));
                    break;
            }
        });

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
            // 获取监控数据
            router.get("/monitor").handler(this::queryMonitor);
        }

        //最后一个Route
        router.route().last().handler(context -> {
            logger.error("url 404 ={}", context.request().uri());
            context.response().end("404");
        }).failureHandler(context -> {
            context.response().end("global error process");
        });

        vertx.createHttpServer().requestHandler(router).listen(port, next::handle);
    }


    private void initDBData(AsyncResult<SQLConnection> result, Future<Void> fut2) {
        if (result.failed()) {
            fut2.fail(result.cause());
        } else {
            SQLConnection connection = result.result();
            List<String> creatTable = Lists.newArrayList(
                    "CREATE TABLE IF NOT EXISTS monitor (id INTEGER NOT NULL IDENTITY, " +
                            "container_id varchar(100) NOT NULL, cpu double, memory double, time BIGINT )",
                    "CREATE TABLE IF NOT EXISTS containers (container_id varchar(100), " +
                            "host_name varchar(100), image varchar(100), role varchar(100), PRIMARY KEY (container_id))"
            );

            connection.batch(creatTable,
                    ar -> {
                        if (ar.failed()) {
                            fut2.fail(ar.cause());
                            connection.close();
                        } else {
                            fut2.complete();
                            connection.close();
                        }
                    });
        }
    }


    public void stopService(RoutingContext routingContext) {
        vertx.eventBus().send("am", new CustomMessage("stop", null));
    }

    private void insert(String sql, JsonArray params){
        jdbcClient.getConnection(ar -> {
            if (ar.failed()) {
                logger.error("", ar.cause());
            } else {
                insert(ar.result(), sql, params);
            }
        });
    }

    private void insert(SQLConnection connection, String sql, JsonArray params) {
        connection.updateWithParams(sql, params,
                (ar) -> {
                    if (ar.failed()) {
                        logger.error("insert failed", ar.cause());
                    }
                    connection.close();
                });
    }


    private void queryMonitor(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "application/json");
        JsonObject result = new JsonObject();

        jdbcClient.getConnection(r -> {
            if (r.failed()) {
                logger.error("", r.cause());
            } else {
                SQLConnection connection = r.result();

                Future<JsonObject> amMeta = Future.future();
                vertx.eventBus().send("am", new CustomMessage("meta", null), amr ->{
                    if(amr.succeeded()){
                        amMeta.complete((JsonObject) amr.result().body());
                    }else {
                        response.end("wrong");
                        amMeta.fail(amr.cause());
                    }
                });

                amMeta.setHandler(am -> {
                    if(am.succeeded()){
                        
                        result.put("appInfo", am.result());
                        Future<ResultSet> monitors = Future.future();
                        Future<ResultSet> containers = Future.future();

                        connection.query("select * from monitor ", rs -> {
                            if (rs.succeeded()) {
                                monitors.complete(rs.result());
                            } else {
                                logger.error("", rs.cause());
                            }
                        });

                        connection.query("select * from containers", rs -> {
                            if (rs.succeeded()) {
                                containers.complete(rs.result());
                            } else {
                                logger.error("", rs.cause());
                            }
                        });

                        CompositeFuture.all(monitors, containers).setHandler(ar->{
                            if(ar.succeeded()){
                                result.put("data", monitors.result().getResults());
                                result.put("containers", containers.result().getResults());
                                response.end(result.encode());
                            }
                            logger.info("CompositeFuture.all");
                            connection.close();
                        });
                    }else {
                        logger.error("", am.cause());
                    }
                });


            }
        });
    }


}
