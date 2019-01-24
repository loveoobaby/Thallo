package com.yss.thallo.client;


import com.yss.thallo.conf.ThalloConfiguration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class Client {

    private static Logger logger = LoggerFactory.getLogger(Client.class);

    private ThalloConfiguration conf;
    private final FileSystem dfs;
    private YarnClient yarnClient;

    private Client(String[] args) throws IOException {
        this.conf = new ThalloConfiguration();
        this.dfs = FileSystem.get(conf);



    }

    public static void showWelcome() {
        System.err.println("Welcome to\n " +
                "\n" +
                "  ______ __            __ __     \n" +
                " /_  __// /_   ____ _ / // /____ \n" +
                "  / /  / __ \\ / __ `// // // __ \\\n" +
                " / /  / / / // /_/ // // // /_/ /\n" +
                "/_/  /_/ /_/ \\__,_//_//_/ \\____/ \n" +
                "                                 \n"
        );
    }



    public static void main(String[] args) {
        showWelcome();


    }
}
