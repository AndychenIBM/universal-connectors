package com.ibm.guardium.universalconnector.transmitter;

import com.google.protobuf.Message;

public class QueuedMessage {
    private Message msg = null;
    private byte[] bytes = null;

    public QueuedMessage(Message msg) {
        this.msg = msg;
    }

    public QueuedMessage() {}

    public QueuedMessage(byte[] value) {
        this.bytes = value;
    }

    public Message getMsg() {
        return msg;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
