package com.yss.thallo.reporter;

import org.apache.hadoop.yarn.api.records.ContainerId;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

public class AMReporter extends ContainerReporter {

    public AMReporter(ContainerId containerId) {
        super(containerId);
    }

    @Override
    public void updateProcessInfo() throws Exception {
        MemoryUsage memoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        MBeanServer serverBean = ManagementFactory.getPlatformMBeanServer();
        ObjectName helloName = new ObjectName("java.lang:type=OperatingSystem");
        Double cpuRatio = (Double) serverBean.getAttribute(helloName, "ProcessCpuLoad");
        this.metrix.put("cpu", cpuRatio * 100);
        this.metrix.put("memory", memoryUsage.getUsed() / 1024. / 1024.);
    }
}
