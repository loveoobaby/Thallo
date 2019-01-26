package com.yss.thallo.AM;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CleanAMThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(CleanAMThread.class);

    @Override
    public void run() {

        try {
            logger.info("shud down hook run");

        } catch (Exception e) {
            logger.error("Writing the history log file Error." + e);
        }
    }


}
