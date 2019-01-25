package com.yss.thallo.api;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.util.Shell;
import org.apache.hadoop.yarn.api.ApplicationConstants;

public interface ThalloConstants {

    enum Environment {
        HADOOP_USER_NAME("HADOOP_USER_NAME"),

        APPMASTER_HOST("APPMASTER_HOST"),

        APPMASTER_PORT("APPMASTER_PORT"),

        APP_ID("APP_ID"),

        APP_ATTEMPTID("APP_ATTEMPTID");

        private final String variable;

        Environment(String variable) {
            this.variable = variable;
        }

        public String key() {
            return variable;
        }

        public String toString() {
            return variable;
        }

        public String $() {
            if (Shell.WINDOWS) {
                return "%" + variable + "%";
            } else {
                return "$" + variable;
            }
        }

        @InterfaceAudience.Public
        @InterfaceStability.Unstable
        public String $$() {
            return ApplicationConstants.PARAMETER_EXPANSION_LEFT +
                    variable +
                    ApplicationConstants.PARAMETER_EXPANSION_RIGHT;
        }
    }
}
