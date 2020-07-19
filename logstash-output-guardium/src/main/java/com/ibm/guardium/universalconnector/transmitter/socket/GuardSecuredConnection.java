package com.ibm.guardium.universalconnector.transmitter.socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.net.ssl.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GuardSecuredConnection extends GuardAbstractConnection {
    private ByteBuffer myNetData;
    private ByteBuffer peerAppData;
    private ByteBuffer peerNetData;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private GuardAbstractConnection conn;
    private SSLEngine engine;
    private static Logger log = LogManager.getLogger(GuardSecuredConnection.class);

    public GuardSecuredConnection(String remoteAddress, int port) throws CertificateException, IOException, NoSuchAlgorithmException, KeyManagementException {
        this.conn = new GuardNonSecuredConnection(remoteAddress, port);
        log.debug("GuardSecuredConnection with remoteAddress:" + remoteAddress + " port:" + port);
        SSLContext context = SSLContext.getInstance("TLSv1.2");

        // This is done to trust all certificates, no need to add the g-machines certificate to the key store.
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {}

                    @Override
                    public void checkServerTrusted(X509Certificate[] arg0, String arg1)	throws CertificateException {}

                }
        };
        context.init(null, trustAllCerts, new SecureRandom());

        engine = context.createSSLEngine(remoteAddress, port);
        engine.setUseClientMode(true);

        SSLSession session = engine.getSession();
        myNetData = ByteBuffer.allocate(session.getPacketBufferSize());
        peerAppData = ByteBuffer.allocate(1024);
        peerNetData = ByteBuffer.allocate(session.getPacketBufferSize());
    }

    private boolean doHandshake () throws IOException {
        // Create byte buffers to use for holding application data
        int appBufferSize = engine.getSession().getApplicationBufferSize();
        ByteBuffer myAppData = ByteBuffer.allocate(appBufferSize);

        // Begin handshake
        SSLEngineResult.HandshakeStatus hs = engine.getHandshakeStatus();

        // Process handshaking message
        while (hs != SSLEngineResult.HandshakeStatus.FINISHED &&
                hs != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
            switch (hs) {
                case NEED_UNWRAP:
                    // Receive handshaking data from peer
                    if (conn.read(peerNetData) < 0) {
                        if (engine.isInboundDone() && engine.isOutboundDone()) {
                            return false;
                        }
                        closeInbound();
                        engine.closeOutbound();
                        // After closeOutbound the engine will be set to WRAP state, in order to try to send
                        // a close message to the client.
                        hs = engine.getHandshakeStatus();
                        break;
                    }

                    // Process incoming handshaking data
                    peerNetData.flip();
                    SSLEngineResult res = engine.unwrap(peerNetData, peerAppData);
                    peerNetData.compact();
                    hs = res.getHandshakeStatus();

                    // Check status
                    switch (res.getStatus()) {
                        case OK:
                            if(log.isTraceEnabled()) {
                                log.trace("ok");
                            }
                            break;
                        case CLOSED:

                        case BUFFER_OVERFLOW:
                            log.debug("overflow");
                            peerAppData = enlargeApplicationBuffer(peerAppData);
                            break;
                        case BUFFER_UNDERFLOW:
                            peerNetData = handleBufferUnderflow(peerNetData);
                            break;
                    }
                    break;

                case NEED_WRAP :
                    if(log.isTraceEnabled()) {
                        log.trace("wrap");
                    }

                    // Empty the local network packet buffer.
                    myNetData.clear();

                    // Generate handshaking data
                    res = engine.wrap(myAppData, myNetData);
                    hs = res.getHandshakeStatus();

                    // Check status
                    switch (res.getStatus()) {
                        case OK :
                            myNetData.flip();

                            // Send the handshaking data to peer
                            while (myNetData.hasRemaining()) {
                                if (conn.write(myNetData) < 0) {
                                    handleEndOfStream();
                                }
                            }
                            break;
                        case BUFFER_OVERFLOW:
                            myNetData = enlargePacketBuffer(myNetData);
                            break;
                        case BUFFER_UNDERFLOW:
                            throw new SSLException("Buffer underflow occurred after a wrap. I don't think we should ever get here.");
                        case CLOSED:
                            try {
                                myNetData.flip();
                                while (myNetData.hasRemaining()) {
                                    conn.write(myNetData);
                                }
                                // At this point the handshake status will probably be NEED_UNWRAP so we make sure that
                                // peerNetData is clear to read.
                                peerNetData.clear();
                            } catch (Exception e) {
                                log.error("Failed to send server's CLOSE message due to socket channel's failure.");
                                hs = engine.getHandshakeStatus();
                            }
                            break;
                        default:
                            throw new IllegalStateException("Invalid SSL status: " + res.getStatus());
                    }
                    break;
                case NEED_TASK :
                    if(log.isTraceEnabled()) {
                        log.trace("task");
                    }

                    Runnable task;
                    while ((task=engine.getDelegatedTask()) != null) {
                        new Thread(task).start();
                    }
                    hs = engine.getHandshakeStatus();
                    break;
                case FINISHED:
                    break;
                case NOT_HANDSHAKING:
                    break;
                default:
                    throw new IllegalStateException("Invalid SSL status: " + hs);
            }
        }
        return true;
    }

    private ByteBuffer enlargePacketBuffer(ByteBuffer buffer) {
        return enlargeBuffer(buffer, engine.getSession().getPacketBufferSize());
    }

    private ByteBuffer enlargeApplicationBuffer(ByteBuffer buffer) {
        return enlargeBuffer(buffer, engine.getSession().getApplicationBufferSize());
    }

    private ByteBuffer enlargeBuffer(ByteBuffer buffer, int sessionProposedCapacity) {
        if (sessionProposedCapacity > buffer.capacity()) {
            buffer = ByteBuffer.allocate(sessionProposedCapacity);
        } else {
            buffer = ByteBuffer.allocate(buffer.capacity() * 2);
        }
        return buffer;
    }

    private ByteBuffer handleBufferUnderflow(ByteBuffer buffer) {
        if (engine.getSession().getPacketBufferSize() < buffer.limit()) {
            return buffer;
        } else {
            ByteBuffer replaceBuffer = enlargePacketBuffer(buffer);
            buffer.flip();
            replaceBuffer.put(buffer);
            return replaceBuffer;
        }
    }

    private void closeConnection() throws IOException  {
        engine.closeOutbound();
        doHandshake();
        conn.close();
    }

    private void handleEndOfStream() throws IOException  {
        closeInbound();
        closeConnection();
    }

    private void closeInbound() {
        try {
            engine.closeInbound();
        } catch (Exception e) {
            log.error("This engine was forced to close inbound, without having received the proper SSL/TLS" +
                    " close notification message from the peer, due to end of stream.", e);
        }
    }

    @Override
    public boolean connect() throws IOException {
        conn.connect();
        engine.beginHandshake();
        return doHandshake();
    }

    @Override
    public int write(ByteBuffer inputBuffer) throws IOException {
        if(log.isTraceEnabled()) {
            log.trace("About to write to the server...");
        }
        int writtenBytes = inputBuffer.remaining();

        while (inputBuffer.hasRemaining()) {
            if(log.isTraceEnabled()) {
                log.trace("Writing to server");
            }
            myNetData.clear();
            SSLEngineResult result = engine.wrap(inputBuffer, myNetData);
            switch (result.getStatus()) {
            case OK:
                int written = 0;
                if(log.isTraceEnabled()) {
                    log.trace("Wrap status OK, writing to server.");
                }
                myNetData.flip();
                int toBeWritten = myNetData.remaining();
                while (myNetData.hasRemaining()) {
                    written += conn.write(myNetData);
                }
                if (written != toBeWritten) {
                    throw new IOException ("The secured channel has lost data.");
                }
                break;
            case BUFFER_OVERFLOW:
                log.debug("Write - Wrap - Buffer Overflow");
                myNetData = enlargePacketBuffer(myNetData);
                return 0;
            case BUFFER_UNDERFLOW:
                log.debug("Write - Wrap - Buffer Underflow");
                throw new SSLException("Buffer underflow occured after a wrap. I don't think we should ever get here.");
            case CLOSED:
                log.debug("Write - Wrap - SSLEngine Closed.");
                closeConnection();
                return 0;
            default:
                throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
            }
        }
        return writtenBytes;
    }

    @Override
    public int read(ByteBuffer outputBuffer) throws IOException {

        int appDataBytesRead = 0;
        peerNetData.clear();
        boolean exitReadLoop = false;
        while (!exitReadLoop) {
            if(log.isTraceEnabled()) {
                log.trace("Reading from server");
            }
            int bytesRead = conn.read(peerNetData);
            if (bytesRead == 0) {
                exitReadLoop = true;
            } else if (bytesRead > 0) {
                peerNetData.flip();
                while (peerNetData.hasRemaining()) {
                    peerAppData.clear();
                    SSLEngineResult result = engine.unwrap(peerNetData, peerAppData);
                    switch (result.getStatus()) {
                    case OK:
                        peerAppData.flip();
                        if(log.isTraceEnabled()) {
                            log.trace("Read - Unwrap - Server response: " + new String(peerAppData.array()));
                        }
                        exitReadLoop = true;
                        appDataBytesRead = result.bytesConsumed();
                        break;
                    case BUFFER_OVERFLOW:
                        log.debug("Read - Unwrap - Buffer Overflow");
                        peerAppData = enlargeApplicationBuffer(peerAppData);
                        break;
                    case BUFFER_UNDERFLOW:
                        log.debug("Read - Unwrap - Buffer Underflow");
                        peerNetData = handleBufferUnderflow(peerNetData);
                        break;
                    case CLOSED:
                        log.debug("Read - Unwrap - SSLEngine closed.");
                        closeConnection();
                        break;
                    default:
                        throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                    }
                }
            } else /*if (bytesRead < 0)*/ {
                handleEndOfStream();
                return bytesRead;
            }
        }
        return appDataBytesRead;
    }

    @Override
    public void close() throws IOException {
        log.debug("closing secure connection");
        engine.closeOutbound();
        ByteBuffer myAppData = ByteBuffer.allocate(engine.getSession().getApplicationBufferSize());
        peerAppData.clear();
        engine.wrap(myAppData, peerAppData);
        conn.close();
        executor.shutdown();
    }

    @Override
    public void register(Selector selector, int selectionKey) throws ClosedChannelException {
        conn.register(selector, selectionKey);
    }

    @Override
    public boolean isConnectionPending() {
        return conn.isConnectionPending();
    }

    @Override
    public boolean finishConnect() throws IOException {
        return conn.finishConnect();
    }

    @Override
    public boolean isConnected() {
        return conn.isConnected();
    }

    @Override
    public String getLocalHostName() {
        return conn.getLocalHostName();
    }


    @Override
    public String getLocalHostAddress() {
        return conn.getLocalHostAddress();
    }

    @Override
    public int getLocalHostPort() {
        return conn.getLocalHostPort();
    }
}
