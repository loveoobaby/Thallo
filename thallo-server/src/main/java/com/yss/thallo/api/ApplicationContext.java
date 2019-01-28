package com.yss.thallo.api;


import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.Container;

import java.util.List;

public interface ApplicationContext {

    ApplicationId getApplicationID();


}
