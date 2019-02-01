package com.yss.thallo.AM;

import com.yss.thallo.api.ApplicationContainerProtocol;
import io.vertx.core.json.JsonObject;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.server.protocol.HeartbeatResponse;
import org.apache.hadoop.ipc.ProtocolSignature;
import org.apache.hadoop.ipc.RPC;
import org.apache.hadoop.ipc.Server;
import org.apache.hadoop.service.AbstractService;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ContainerListener extends AbstractService implements ApplicationContainerProtocol {

    private static Logger logger = LoggerFactory.getLogger(ContainerListener.class);


    private Server server;
    private Configuration conf;

    public ContainerListener(String name, Configuration conf) {
        super(name);
        this.conf = conf;
    }

    public int getServicePort(){
        return this.server.getPort();
    }

    @Override
    public void start() {
        // start RPC
        logger.info("Starting application containers handler server");
        RPC.Builder builder = new RPC.Builder(conf);
        builder.setProtocol(ApplicationContainerProtocol.class);
        builder.setInstance(this);
        builder.setBindAddress("0.0.0.0");
        builder.setPort(0);
        try {
            server = builder.build();
        } catch (Exception e) {
            logger.error("Error starting application containers handler server!", e);
            System.exit(1);
        }
        server.start();
        logger.info("start success @ " + server.getPort());
    }


    @Override
    public void registerContainer(String containerId, String hostName, String image) {
        JsonObject registerDocker = new JsonObject().put("msgType", "registerDocker").
                put("containerId", containerId).put("hostName", hostName).put("image", image);
        Launcher.getVertx().eventBus().send("web", registerDocker);
        logger.info("registerContainer, containerId={}", containerId);
    }

    @Override
    public String heartbeat(String containerId) {
        logger.info("heartbeat = " + containerId);
        return "";
    }

    @Override
    public long getProtocolVersion(String protocol, long clientVersion) throws IOException {
        return 0;
    }

    @Override
    public ProtocolSignature getProtocolSignature(String protocol, long clientVersion, int clientMethodsHash) throws IOException {
        return null;
    }
}
