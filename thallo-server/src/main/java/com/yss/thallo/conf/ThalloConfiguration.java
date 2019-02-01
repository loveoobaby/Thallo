package com.yss.thallo.conf;

import org.apache.hadoop.yarn.conf.YarnConfiguration;

public class ThalloConfiguration extends YarnConfiguration {

    public static final String THALLO_JOB_CONFIGURATION = "thallo-config.xml";

    static {
        YarnConfiguration.addDefaultResource(THALLO_JOB_CONFIGURATION);
    }

    public ThalloConfiguration() {
        super();
    }


    public static final String DEFAULT_APP_TYPE = "Docker";

    public static final String THALLO_AM_MEMORY = "thallo.am.memory";

    public static final int DEFAULT_THALLO_AM_MEMORY = 1024;

    public static final String THALLO_AM_VCORES = "thallo.am.vcores";

    public static final int DEDAULT_THALLO_AM_VCORE = 1;

    public static final String DEFAULT_QUEUE = "default";

    public static final String THALLO_QUEUE = "thallo.queue";

    public static final String THALLO_STAGING_DIR = "thallo.staging.dir";

    public static final String DEFAULT_THALLO_STAGING_DIR = "/tmp/thallo/staging";

    public static final String THALLO_APP_MASTER_NAME = "AppMaster.jar";

    public static final int DEFAULT_THALLO_WEB_PORT = 10321;

    public static final String THALLO_RESOURCE_NAME_KEY = "thallo.resource.name";

    public static final String THALLO_RESOURCE_URL_KEY = "thallo.resource.url";

    public static final String THALLO_RESOURCE_SIZE_KEY = "thallo.resource.size";

    public static final String THALLO_RESOURCE_TIMESTAMP_KEY = "thallo.resource.timestamp";

    public static final String THALLO_RESOURCE_VISUAL_KEY = "thallo.resource.visual";






}
