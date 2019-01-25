package com.yss.thallo.client;


import com.yss.thallo.conf.ThalloConfiguration;
import org.apache.commons.cli.ParseException;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class Client {

    private static Logger logger = LoggerFactory.getLogger(Client.class);

    private ThalloConfiguration conf;
    private final FileSystem dfs;
    private YarnClient yarnClient;
    private ClientArguments clientArguments;

    private Client(String[] args) throws IOException, ParseException {
        this.conf = new ThalloConfiguration();
        this.dfs = FileSystem.get(conf);
        this.clientArguments = new ClientArguments(args);



    }

    private void init(){
        String appSubmitterUserName = System.getenv(ApplicationConstants.Environment.USER.name());
        if (conf.get("hadoop.job.ugi") == null) {
            UserGroupInformation ugi = UserGroupInformation.createRemoteUser(appSubmitterUserName);
            conf.set("hadoop.job.ugi", ugi.getUserName() + "," + ugi.getUserName());
        }
        conf.set(ThalloConfiguration.THALLO_AM_MEMORY, String.valueOf(clientArguments.amMem));
        conf.set(ThalloConfiguration.THALLO_AM_VCORES, String.valueOf(clientArguments.amCores));

    }

    private static void showWelcome() {
        System.err.println("Welcome to\n " +
                "\n" +
                "  _____  _             _  _        \n" +
                " |_   _|| |__    __ _ | || |  ___  \n" +
                "   | |  | '_ \\  / _` || || | / _ \\ \n" +
                "   | |  | | | || (_| || || || (_) |\n" +
                "   |_|  |_| |_| \\__,_||_||_| \\___/ \n" +
                "                                   \n"
        );
    }





    public static void main(String[] args) {
        showWelcome(); 
        try{
            logger.info("Initializing Client");
            Client client = new Client(args);
            client.init();




        }catch (Exception e){
            logger.error("Error run Client", e);
            System.exit(1);
        }


    }
}
