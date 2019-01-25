package com.yss.thallo.AM;

import com.yss.thallo.api.ApplicationContext;
import org.apache.hadoop.yarn.api.records.ApplicationId;

public class RunningAppContext implements ApplicationContext {
    @Override
    public ApplicationId getApplicationID() {
        return null;
    }
}
