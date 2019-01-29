package com.yss.thallo.api;


import org.apache.hadoop.yarn.api.records.ApplicationId;

public interface ApplicationContext {

    ApplicationId getApplicationID();

    void stopService();


}
