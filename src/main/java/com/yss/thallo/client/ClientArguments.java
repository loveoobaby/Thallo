package com.yss.thallo.client;

import com.yss.thallo.conf.ThalloConfiguration;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class ClientArguments {

    private Options allOptions;
    String appName;
    String appType;
    int amMem;
    int amCores;

    // master docker container parameter
    int masterDockerMemory;
    int masterDockerVCores;
    int masterDockerNum;

    // slave docker container parameter
    int slaveDockerMemory;
    int slaveDockerVCores;
    int slaveDockerNum;

    // queue
    String queue;


    public ClientArguments(String[] args) throws ParseException {
        queue = ThalloConfiguration.DEFAULT_APP_QUEUE;
        appType = ThalloConfiguration.DEFAULT_APP_TYPE;
        amMem = ThalloConfiguration.DEFAULT_AM_MEMORY;
        amCores = ThalloConfiguration.DEFAULT_AM_VCORE;
        this.cliParser(args);
    }

    private void cliParser(String[] args) throws ParseException {
        CommandLine cliParser = new BasicParser().parse(allOptions, args);

    }

}
