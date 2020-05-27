package com.ibm.guardium.universalconnector.transmitter.socket;

import java.nio.ByteBuffer;

public class PingMessageHeader {
    private static final short GUARDIUM_VENDOR_ID = 0;//4001;
    private static final short SERVICE_ID_DS_MESSAGE = 3;
    private ByteBuffer header;

    public PingMessageHeader() {
        header = ByteBuffer.allocate(16);
        header.putLong(0);        // length
        header.putInt(0); //packetid
        header.putShort(GUARDIUM_VENDOR_ID);
        header.putShort(SERVICE_ID_DS_MESSAGE);
        header.rewind();
    }

    public ByteBuffer getHeader() {
        return header;
    }

    public void setLength(long length){
        header.putLong(0, length);
    }
}
