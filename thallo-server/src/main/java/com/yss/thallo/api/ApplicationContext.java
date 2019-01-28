package com.yss.thallo.api;


import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.exceptions.YarnException;

import java.io.IOException;
import java.util.List;

public interface ApplicationContext {

    ApplicationId getApplicationID();

    void stopService();


}
