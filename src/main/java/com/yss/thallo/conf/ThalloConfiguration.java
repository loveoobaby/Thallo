package com.yss.thallo.conf;

import org.apache.hadoop.yarn.conf.YarnConfiguration;

public class ThalloConfiguration extends YarnConfiguration {

    public ThalloConfiguration(){
        super();
    }

    public static final String DEFAULT_APP_QUEUE = "default";
    public static final String DEFAULT_APP_TYPE = "Docker";

    public static final int DEFAULT_AM_MEMORY = 1024;
    public static final int DEFAULT_AM_VCORE = 1;


}
