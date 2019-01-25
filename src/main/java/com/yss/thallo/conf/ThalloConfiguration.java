package com.yss.thallo.conf;

import org.apache.hadoop.yarn.conf.YarnConfiguration;

public class ThalloConfiguration extends YarnConfiguration {

    public ThalloConfiguration(){
        super();
    }


    public static final String DEFAULT_APP_TYPE = "Docker";

    public static final String THALLO_AM_MEMORY = "thallo.am.memory";

    public static final int DEFAULT_THALLO_AM_MEMORY = 1024;

    public static final String THALLO_AM_VCORES = "thallo.am.vcores";

    public static final int DEDAULT_THALLO_AM_VCORE = 1;

    public static final String DEFAULT_QUEUE = "default";

    public static final String THALLO_QUEUE = "thallo.queue";


}
