package com.ibm.guardium.universalconnector.transmitter.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class GuardNonSecuredConnection extends GuardAbstractConnection {

    private SocketChannel socketChannel = null;
    private InetSocketAddress socketAddress = null;

    public GuardNonSecuredConnection(String host, int port) throws IOException {
        socketAddress = new InetSocketAddress(host, port);
    }

    public boolean connect () throws IOException {
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(socketAddress);
        while (!socketChannel.finishConnect()) {}
        return true;
    }

    public void register (Selector selector, int selectionKey) throws ClosedChannelException {
        socketChannel.register(selector, selectionKey);
    }

    public boolean isConnectionPending () {
    return socketChannel.isConnectionPending();
}

    public boolean finishConnect () throws IOException {
        return socketChannel.finishConnect();
    }

    public boolean isConnected () {
        return socketChannel.isConnected();
    }

    public void close () throws IOException {
        socketChannel.close();
    }

    public int read (ByteBuffer outputBuffer) throws IOException {
        return socketChannel.read(outputBuffer);
    }

    public int write (ByteBuffer inputBuffer) throws IOException {
        return socketChannel.write(inputBuffer);
    }

    public String getLocalHostName () {
    return socketChannel.socket().getLocalAddress().getHostName();
    }

    public String getLocalHostAddress () {
        return socketChannel.socket().getLocalAddress().getHostAddress();
    }

    public int getLocalHostPort () {
        return socketChannel.socket().getLocalPort();
    }
}
