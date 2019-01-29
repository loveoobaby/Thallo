package com.yss.thallo.AM;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.Random;

public class AMReporter extends Thread {

    @Override
    public void run() {


    }

    public static void main(String[] args) throws InterruptedException, MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
        while (true) {

            MemoryUsage use = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();

            MBeanServer server = ManagementFactory.getPlatformMBeanServer();

            ObjectName helloName = new ObjectName("java.lang:type=OperatingSystem");
            Double cpu = (Double) server.getAttribute(helloName, "ProcessCpuLoad");

            System.out.println(use.getUsed() + " cpu=" + cpu);

            int i = 0;
            while (true) {
                i++;
                for (int j = 0; j < 1000000; j++) {
                    new Random().nextDouble();
                }
                if (i > 1000) {
                    break;
                }
            }
            Thread.sleep(1000);
        }
    }
}
