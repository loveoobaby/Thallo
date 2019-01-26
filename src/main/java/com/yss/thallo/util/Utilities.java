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
import java.net.InetAddress;
import java.net.Socket;

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

    public static int findUnusedPort(int startPort){
        int i = 0;
        while (i < 2) {
            if (!Utilities.isPortUsing(startPort + i)) {
                startPort = startPort + i;
                break;
            }
            i++;
        }
        if (i == 2) {
            throw new RuntimeException("can not find useful port");
        }

        return startPort;
    }


    public static boolean isPortUsing(int port) {
        boolean flag = false;
        try {
            InetAddress Address = InetAddress.getLocalHost();
            Socket socket = new Socket(Address,port);  //建立一个Socket连接
            flag = true;
        } catch (Exception e) {
        }
        return flag;
    }

}
