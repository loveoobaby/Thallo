package com.yss.thallo.util;

import com.yss.thallo.conf.ThalloConfiguration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.LocalResource;
import org.apache.hadoop.yarn.api.records.LocalResourceType;
import org.apache.hadoop.yarn.api.records.LocalResourceVisibility;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.apache.hadoop.yarn.util.Records;

import java.io.IOException;

public class Utilities {

    public static Path getRemotePath(ThalloConfiguration conf, ApplicationId appId, String fileName) {
        String pathSuffix = appId.toString() + "/" + fileName;
        Path remotePath = new Path(conf.get(ThalloConfiguration.THALLO_STAGING_DIR, ThalloConfiguration.DEFAULT_THALLO_STAGING_DIR),
                pathSuffix);
        remotePath = new Path(conf.get("fs.defaultFS"), remotePath);
        return remotePath;
    }

    public static LocalResource createApplicationResource(FileSystem fs, Path path, LocalResourceType type)
            throws IOException {
        LocalResource localResource = Records.newRecord(LocalResource.class);
        FileStatus fileStatus = fs.getFileStatus(path);
        localResource.setResource(ConverterUtils.getYarnUrlFromPath(path));
        localResource.setSize(fileStatus.getLen());
        localResource.setTimestamp(fileStatus.getModificationTime());
        localResource.setType(type);
        localResource.setVisibility(LocalResourceVisibility.APPLICATION);
        return localResource;
    }

}
