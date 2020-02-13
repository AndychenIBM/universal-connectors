package com.ibm.guardium.universalconnector.transmitter.socket;

import com.google.protobuf.Message;
import com.ibm.guardium.universalconnector.config.SnifferConfig;
import com.ibm.guardium.universalconnector.config.UCConfig;
import com.ibm.guardium.universalconnector.transmitter.QueuedMessage;
import com.ibm.guardium.universalconnector.transmitter.RecordTransmitter;
import com.ibm.guardium.universalconnector.transmitter.TransmitterStats;
import com.ibm.guardium.universalconnector.transmitter.QueuedMessage;
import com.ibm.guardium.universalconnector.transmitter.RecordTransmitter;
import com.ibm.guardium.universalconnector.transmitter.TransmitterStats;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class GuardConnection implements RecordTransmitter {
    private TransmitterStats transmitterStats;
    private final Lock lock = new ReentrantLock();
    public final Condition connected  = lock.newCondition();
    public static final int INIT_BUF_LEN = 2048*1024;
    private SnifferConfig snifferConfig;

    enum Status { CLOSE, OPEN, IN_PROGRESS, ERROR }
    private Status status = Status.CLOSE;
    private static final short SERVICE_ID_HANDSHAKE = 1;
    private static final short SERVICE_ID_DS_MESSAGE = 4;
    private static Log log = LogFactory.getLog(GuardConnection.class);
    private Selector selector = null;
    private ByteBuffer writeBuffer = ByteBuffer.allocateDirect(INIT_BUF_LEN);
    private boolean isWriteBufferEmpty = true;
    private boolean currentMsgIsPing = false;
    private BlockingQueue<QueuedMessage> messageQueue = null;
    private ByteBuffer readBuffer = ByteBuffer.allocateDirect(2048);
    private int snifferMasterip = 0;
    private byte[] pingBytes = null;
    private byte[] configBytes = null;
    private byte[] handshakeBytes = null;
    private PingMessageHeader pingMsgHeader;
    private GuardMessage msgHeader;
    private GuardAbstractConnection commHandler = null;
    private UCConfig config;

    class ConnTimer extends TimerTask {
        private int index = 0;
        public void run() {
            Thread.currentThread().setName(config.getConnectorId() + "-ConnTimer");
            if (index % 55 == 0) {// put ping to snif only every 5 sec
                try {
                    messageQueue.put(new QueuedMessage());
                } catch (InterruptedException e) {
                    log.debug("ConnTimer was interrupted.");
                }
            }
            index++;
        }
    }

    public GuardConnection(BlockingQueue<QueuedMessage> mq, TransmitterStats ts) {
        messageQueue = mq;
        this.transmitterStats = ts;
    }

    public boolean isStatusOpen(){
        return status == Status.OPEN;
    }

    private void prepareMsgHeaders(UCConfig config){
        pingMsgHeader = new PingMessageHeader();
        msgHeader = new GuardMessage(config);
    }

    private void sendUninstallToSnif() throws IOException {
        msgHeader.setUnInstallMessage();
        writeBuffer.put(msgHeader.getHeader().array());
        writeBuffer.flip();

        while(writeBuffer.hasRemaining())
            commHandler.write(writeBuffer);

        writeBuffer.clear();
        msgHeader.unSetUnInstallMessage();
        log.debug("sent uninstall to snif");
    }



    private void setMasterIP() throws UnknownHostException{
        InetAddress serverIPAddress = InetAddress.getByName(snifferConfig.getIp());
        snifferMasterip = 0;
        byte[] masterBytes = serverIPAddress.getAddress();
        int shift = 0;
        if (masterBytes != null){
            for (byte b : masterBytes){
                short oneByte = b;
                if (oneByte < 0)
                    oneByte += 256;
                snifferMasterip += oneByte<<shift;
                shift += 8;
            }
        }
    }

    public void setup(UCConfig config, SnifferConfig snifferConfig) throws UnknownHostException {
        this.config = config;
        this.snifferConfig = snifferConfig;
        log.info("Snif address is set to "+ snifferConfig.getIp() + ":" + snifferConfig.getPort() + " connecting with SSL=" + snifferConfig.isSSL());
        prepareMsgHeaders(config);
        setMasterIP();
        pingBytes = GuardMessage.preparePing(snifferMasterip, snifferConfig.getIp(), config.getConnectorId());
        configBytes = GuardMessage.prepareAgentConfig(config, "999");
        handshakeBytes = GuardMessage.prepareHandshake(snifferMasterip, snifferConfig.getIp(), config.getConnectorId(), config.getVersion());
    }


    private void sendConfig() throws IOException {
        ByteBuffer msg = new ServiceMessageBuilder(configBytes, SERVICE_ID_DS_MESSAGE).getMessage();
        commHandler.write(msg);
        log.debug("Sent config.");
        lock.lock();
        connected.signalAll();
        lock.unlock();
    }

    private void sendHandshake() throws IOException {
        ByteBuffer msg = new ServiceMessageBuilder(handshakeBytes, SERVICE_ID_HANDSHAKE).getMessage();
        commHandler.write(msg);
        log.debug("Sent handshake.");
        status = Status.OPEN;
        lock.lock();
        connected.signalAll();
        lock.unlock();
        //sendConfig();
        log.debug("Connection status is : " + status);
    }

    private void createNewConnection() throws Exception {
        long timeOut = 10000; //10 seconds
        if (snifferConfig.isSSL()) {
            commHandler = new GuardSecuredConnection(snifferConfig.getIp(), snifferConfig.getPort());
        } else {
            commHandler = new GuardNonSecuredConnection(snifferConfig.getIp(), snifferConfig.getPort());
        }
        log.debug("Connecting to: " + snifferConfig.getIp() + ":" + snifferConfig.getPort());
        boolean connectResult = false;
        try {
            connectResult = commHandler.connect();
        } catch (Exception e) {
            log.error(e);
//            log.error(String.format("Unable to connect to server. Sleeping for %d seconds"), e);
//            throw e;
            log.error(String.format("Unable to connect to server. Sleeping for %d seconds", timeOut / 1000), e);
            Thread.sleep(timeOut);
        }
        selector = Selector.open();

        if (connectResult) {
            sendHandshake();
            commHandler.register(selector, SelectionKey.OP_READ|SelectionKey.OP_WRITE);
        } else {
            status = Status.IN_PROGRESS;
            commHandler.register(selector, SelectionKey.OP_CONNECT);
        }
        transmitterStats.setLastReceiveTime(System.currentTimeMillis());
    }

    private void handleReadFromChannel() throws IOException{
        int read;
        do {
            readBuffer.clear();
            read = commHandler.read(readBuffer);
        } while (read == readBuffer.capacity());
        if (read < 0) {
            log.debug("Read -1, closing socket.");
            status= Status.CLOSE;
            return;
        }
        if (read == 0) {
            return;
        }
        transmitterStats.setLastReceiveTime(System.currentTimeMillis());
//       if (log.isTraceEnabled()) {
//            log.trace("Read:" + read + " bytes:[" + bytesToHex(readBuffer.array()) + "]");
//        }
    }

    private String bytesToHex(byte[] bytes) {
        try (Formatter formatter = new Formatter()) {
            for (byte b : bytes) {
                formatter.format("0x%02x,", b);
            }
            return formatter.toString();
        }
    }

    private void handleWriteToChannel() throws IOException{
        writeBuffer.flip();
        int bytesToWrite = writeBuffer.remaining();
        int written = 0;

        try {
            while(writeBuffer.hasRemaining()) {
                written += commHandler.write(writeBuffer);
            }
        } catch (IOException e) {
            log.debug("Problem occurred while sending : bytesToWrite : " + bytesToWrite + ". Written : " + written);
            log.debug("The same buffer will be sent again.");
            writeBuffer.position(0);
            throw new IOException("Channel writing error ", e);
        }
        if (bytesToWrite != written) {
            log.debug("Problem occurred while sending : bytesToWrite : " + bytesToWrite + ". Written : " + written);
            log.debug("The same buffer will be sent again.");
            writeBuffer.position(0);
            return;
        }
        isWriteBufferEmpty = true;
        writeBuffer.clear();
        if (!currentMsgIsPing) {
            transmitterStats.incrementMsgsSent();
            transmitterStats.incrementBytesSent(written);
        }
    }

    private void openConnection() throws Exception {
        try {
            if ((selector != null) && (selector.isOpen()))
            {
                log.debug("closing old connection.");
                selector.close();
            }
            if (commHandler != null)
                commHandler.close();

            createNewConnection();
        } catch (IOException e) {
            log.error(e);
            status= Status.ERROR;
        }
    }

    private void checkConnection() throws IOException{
        long timeout = 1000;
        selector.select(timeout);
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

        while(keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            if(key.isConnectable()) {
                if(commHandler.isConnectionPending()) {
                    transmitterStats.setLastReceiveTime(System.currentTimeMillis());
                    commHandler.finishConnect(); // will have java.net.ConnectException: Connection refused
                }
                if (commHandler.isConnected()) {
                    sendHandshake();
                    key.interestOps(SelectionKey.OP_WRITE|SelectionKey.OP_READ);
                } else {
                    status = Status.CLOSE;
                }
            }
            keyIterator.remove();
        }
    }

    private void handleReadWrite() throws IOException {
        selector.selectNow();
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

        while(keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            if (key.isReadable()) {
                handleReadFromChannel();
            }
            if (key.isWritable()) {
                handleWriteToChannel();
            }
            keyIterator.remove();
        }
    }

    private void getMessageFromQ() throws InterruptedException{
        if(!isWriteBufferEmpty)
            return;

        QueuedMessage currentQm = messageQueue.poll(1000, TimeUnit.MILLISECONDS); //peek has cpu hike
        if (currentQm == null)
            return;

        Message currentMsg = currentQm.getMsg();
        byte[] currentMsgBytes = null;
        ByteBuffer currentHeader = null;

        // This is a ping.
        if(currentMsg == null && currentQm.getBytes() == null) {
            currentMsgBytes = pingBytes;
            currentHeader = pingMsgHeader.getHeader();
            currentMsgIsPing = true;
            // This is a message.
        } else{
            currentMsgBytes = (currentMsg == null) ? currentQm.getBytes() : currentMsg.toByteArray();
            int msgLen = currentMsgBytes.length;
            msgHeader.setLengthToDsMsg((long)msgLen);
            currentHeader = msgHeader.getHeaderForDsMsg();
            currentMsgIsPing = false;
        }

        writeBuffer.put(currentHeader.array());
        writeBuffer.put(currentMsgBytes);
        isWriteBufferEmpty = false;
    }

    private void runLoop(){
        pingMsgHeader.setLength((long)pingBytes.length);

        Timer timer = new Timer();
        ConnTimer connTimer = new ConnTimer();
        timer.scheduleAtFixedRate(connTimer, 0, 1000);
        preformConnectionTasks();
        log.debug("done running, will cleanup if possible.");
        tryToEmptyQ();
        tryToCloseConnection();
        log.debug("Stopping timer.");
        timer.cancel();
    }

    private void preformConnectionTasks() {
        try {
            while (true) {
                if (status != Status.OPEN)
                    log.debug("Connection status is : " + status);

                preformConnectionTasksByStatus();
            }
        } catch (InterruptedException e) {
            log.warn("Connection thread was stopped.");
        } catch (Exception e) {
            log.error("An Exception during connection thread." , e);
            try {
                commHandler.close();
            } catch (IOException e1) {
                log.error("Unable to close socketChannel. ", e1);
            }
            status = Status.CLOSE;
        }
    }

    private void tryToCloseConnection() {
        if (status != Status.CLOSE && commHandler != null) {
            try {
                sendUninstallToSnif();
                log.debug("Closing connection.");
                commHandler.close();
                status = Status.CLOSE;
            } catch (IOException e) {
                log.error(e);
                status = Status.ERROR;
            }
        }
    }

    private void tryToEmptyQ() {
        if (status != Status.OPEN) {
            return;
        }
        while (!messageQueue.isEmpty() || !isWriteBufferEmpty) {
            try {
                getMessageFromQ();
                handleReadWrite();
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

    private void preformConnectionTasksByStatus() throws Exception {
        switch (status) {
            case CLOSE:
                transmitterStats.incrementReconnects();
                openConnection();
                break;
            case IN_PROGRESS:
                checkConnection();
                break;
            case OPEN:
                getMessageFromQ();
                handleReadWrite();
                break;
            case ERROR:
            default:
                transmitterStats.incrementErrors();
                // we are in a bad state, sleep for 10 seconds and try again.
                Thread.sleep(1000 * 10);
                status = Status.CLOSE;
        }
    }

    @Override
    public void stopRun() {
        log.debug("Stop connection was called.");
    }

    @Override
    public void run() {
        log.debug("Starting connection to Snif");
        runLoop();
    }
}
