package com.ibm.guardium.universalconnector.transmitter.socket;

import java.nio.ByteBuffer;

public class ServiceMessageBuilder {
    private static final short GUARDIUM_VENDOR_ID = 0;//4001;
    private ByteBuffer message;

    public ServiceMessageBuilder(byte[] bytes, short serviceId) {
        message = ByteBuffer.allocate(16 + bytes.length);
        message.putLong((long) bytes.length);
        message.putInt(0); //packetid
        message.putShort(GUARDIUM_VENDOR_ID);
        message.putShort(serviceId);
        message.put(bytes);
        message.rewind();
    }

    public ByteBuffer getMessage() {
        return message;
    }
}
