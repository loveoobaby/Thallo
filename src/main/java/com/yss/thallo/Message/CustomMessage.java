package com.yss.thallo.Message;


import io.vertx.core.json.JsonObject;

public class CustomMessage {
  private final String msgType;
  private final JsonObject data;

  public CustomMessage(String msgType, JsonObject data) {
    this.msgType = msgType;
    this.data = data;
  }

  public String getMsgType() {
    return msgType;
  }

  public JsonObject getData() {
    return data;
  }

  @Override
  public String toString() {
    return "CustomMessage{" +
            "msgType='" + msgType + '\'' +
            ", data=" + data +
            '}';
  }
}
