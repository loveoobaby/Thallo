package com.yss.thallo.api;


import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ContainerId;

public interface ApplicationContext {

    ApplicationId getApplicationID();

    void stopService();

    ContainerId getContainerId();

    String getAppMasterHostName();

    public String getConf(String key);


}
