package com.yss.thallo.util;

import com.yss.thallo.conf.ThalloConfiguration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.records.ApplicationId;

public class Utilities {

    public static Path getRemotePath(ThalloConfiguration conf, ApplicationId appId, String fileName) {
        String pathSuffix = appId.toString() + "/" + fileName;
        Path remotePath = new Path(conf.get(ThalloConfiguration.THALLO_STAGING_DIR, ThalloConfiguration.DEFAULT_THALLO_STAGING_DIR),
                pathSuffix);
        remotePath = new Path(conf.get("fs.defaultFS"), remotePath);
        return remotePath;
    }

}
