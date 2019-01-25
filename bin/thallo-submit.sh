#!/usr/bin/env bash

set -e

export THALLO_HOME="$(cd "`dirname "$0"`"/..; pwd)"

echo ${THALLO_HOME}

# Find the java binary
if [ -n "${JAVA_HOME}" ]; then
  RUNNER="${JAVA_HOME}/bin/java"
else
  if [ `command -v java` ]; then
    RUNNER="java"
  else
    echo "JAVA_HOME is not set" >&2
    exit 1
  fi
fi

# Find the java binary
if [ -n "${HADOOP_HOME}" ]; then
  HADOOP_CONF_DIR=${HADOOP_HOME}/etc/hadoop
  HADOOP_COMMON_HOME=${HADOOP_HOME}
  HADOOP_HDFS_HOME=${HADOOP_HOME}
  HADOOP_YARN_HOME=${HADOOP_HOME}
else
  echo "HADOOP_HOME is not set" >&2
  exit 1
fi

THALLO_LIB_DIR=$THALLO_HOME/lib

num_jars="$(ls -1 "$THALLO_LIB_DIR" | grep "thallo-hadoop.*" | wc -l)"
if [ "$num_jars" -eq "0" ]; then
  echo "Failed to find thallo jar in $THALLO_LIB_DIR." 1>&2
  exit 1
fi

THALLO_JARS="$(ls -1 "$THALLO_LIB_DIR" | grep "thallo-hadoop.*" || true)"

CLASSPATH="${THALLO_LIB_DIR}/${THALLO_JARS}:$HADOOP_CONF_DIR:$HADOOP_COMMON_HOME/share/hadoop/common/*:\
$HADOOP_COMMON_HOME/share/hadoop/common/lib/*:$HADOOP_HDFS_HOME/share/hadoop/hdfs/*:\
$HADOOP_HDFS_HOME/share/hadoop/hdfs/lib/*:$HADOOP_YARN_HOME/share/hadoop/yarn/*:$HADOOP_YARN_HOME/share/hadoop/yarn/lib/*"

exec "$RUNNER" -cp "$CLASSPATH" com.yss.thallo.client.Client "$@"

