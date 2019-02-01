package com.yss.thallo.api;

import org.apache.hadoop.hdfs.server.protocol.HeartbeatResponse;
import org.apache.hadoop.ipc.VersionedProtocol;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.yarn.api.records.ContainerId;

public interface ApplicationContainerProtocol extends VersionedProtocol {

  public static final long versionID = 1L;

  void registerContainer(String containerId, String hostName, String image);

  String heartbeat(String containerId);

}
