package com.yss.thallo.AM;

import com.yss.thallo.api.ApplicationContext;
import com.yss.thallo.web.WebVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.service.AbstractService;


public class AMWebService extends AbstractService {

    private ApplicationContext applicationContext;
    private Configuration conf;


    public AMWebService(ApplicationContext applicationContext, Configuration conf) {
        super(AMWebService.class.getName());
        this.applicationContext = applicationContext;
        this.conf = conf;
    }

    @Override
    public void start() {

        Vertx.vertx().deployVerticle(WebVerticle.class, new DeploymentOptions());

    }

    @Override
    public void stop() {

    }
}
