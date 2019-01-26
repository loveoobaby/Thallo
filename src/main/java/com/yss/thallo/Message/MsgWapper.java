package com.yss.thallo.Message;

public class MsgWapper<T> {

    private String msgType;

    private T data;

    public MsgWapper(String msgType, T data){
        this.msgType = msgType;
        this.data = data;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
