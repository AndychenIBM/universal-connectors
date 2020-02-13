package com.ibm.guardium.universalconnector.transmitter.socket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;

public abstract class GuardAbstractConnection {

    abstract public boolean connect () throws IOException;

    abstract public void register (Selector selector, int selectionKey) throws ClosedChannelException;

    abstract public boolean isConnectionPending ();

    abstract public boolean finishConnect () throws IOException;

    abstract public boolean isConnected ();

    abstract public void close () throws IOException;

    abstract public int read (ByteBuffer outputBuffer) throws IOException;

    abstract public int write (ByteBuffer inputBuffer) throws IOException;

    abstract public String getLocalHostName ();

    abstract public String getLocalHostAddress ();

    abstract public int getLocalHostPort ();
}
