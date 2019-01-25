package com.yss.thallo.client;

import com.yss.thallo.AM.ThalloApplicationMaster;
import com.yss.thallo.conf.ThalloConfiguration;
import org.apache.commons.cli.*;
import org.apache.hadoop.mapred.JobConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public class ClientArguments {

    private static final Logger logger = LoggerFactory.getLogger(ClientArguments.class);

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

    String appMasterJar;


    public ClientArguments(String[] args) throws ParseException {
        this.init();
        this.cliParser(args);
    }

    private void init(){
        appName = "";
        appType = ThalloConfiguration.DEFAULT_APP_TYPE;
        amMem = ThalloConfiguration.DEFAULT_THALLO_AM_MEMORY;
        amCores = ThalloConfiguration.DEFAULT_THALLO_AM_MEMORY;
        queue = ThalloConfiguration.DEFAULT_QUEUE;

        allOptions = new Options();
        allOptions.addOption("appName", "app-name", true,
                "set the Application name");
        allOptions.addOption("appType", "app-type", true,
                "set the Application type, default \"Docker\"");

        allOptions.addOption("amMemory", "am-memory", true,
                "Amount of memory in MB to be requested to run the application master");
        allOptions.addOption("amCores", "am-cores", true,
                "Amount of vcores to be requested to run the application master");

        allOptions.addOption("h", "help", false, "Print usage");

        appMasterJar = JobConf.findContainingJar(ThalloApplicationMaster.class);
        logger.info("Application Master's jar is " + appMasterJar);
    }

    private void cliParser(String[] args) throws ParseException {
        CommandLine cliParser = new BasicParser().parse(allOptions, args);
//        if (cliParser.getOptions().length == 0 || cliParser.hasOption("help") || cliParser.hasOption("h")) {
//            printUsage(allOptions);
//            System.exit(0);
//        }

        if (cliParser.hasOption("app-name")) {
            appName = cliParser.getOptionValue("app-name");
        }

        if (appName.trim().equals("")) {
            appName = "Thallo-" + System.currentTimeMillis();
        }

        if (cliParser.hasOption("app-type")) {
            appType = cliParser.getOptionValue("app-type").trim();
        }

        if (cliParser.hasOption("am-memory")) {
            amMem = getNormalizedMem(cliParser.getOptionValue("am-memory"));
        }

        if (cliParser.hasOption("am-cores")) {
            amCores = Integer.valueOf(cliParser.getOptionValue("am-cores"));
        }

    }

    private void printUsage(Options opts) {
        new HelpFormatter().printHelp("Client", opts);
    }

    private int getNormalizedMem(String rawMem) {
        if (rawMem.endsWith("G") || rawMem.endsWith("g")) {
            return Integer.parseInt(rawMem.substring(0, rawMem.length() - 1)) * 1024;
        } else if (rawMem.endsWith("M") || rawMem.endsWith("m")) {
            return Integer.parseInt(rawMem.substring(0, rawMem.length() - 1));
        } else {
            return Integer.parseInt(rawMem);
        }
    }

}
