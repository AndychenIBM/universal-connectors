package com.ibm.guardium.universalconnector.transmitter.socket;

import com.google.protobuf.ByteString;
import com.ibm.guardium.proto.datasource.Datasource;
import com.ibm.guardium.proto.datasource.Datasource.Guard_ds_message;
import com.ibm.guardium.proto.datasource.Datasource.Guard_ds_message.Type;
import com.ibm.guardium.universalconnector.common.Utilities;
import com.ibm.guardium.universalconnector.common.structures.Time;
import com.ibm.guardium.universalconnector.config.ConnectionConfig;
import com.ibm.guardium.universalconnector.config.DBProtocol;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;


public class GuardMessage {
    private static final short GUARDIUM_VENDOR_ID = 0;//4001;
    private static final short SERVICE_ID_EVENT = 4;//5;
    private static final String DB_TYPE_PREFIX = "UC_";
    private static final int UNINSTALL_POSITION_IN_HEADER = 18;
    private static final int UNINSTALL_COMMAND = 1;
    private static final short SERVICE_ID_DS_MESSAGE = 4;

    private ByteBuffer header;
    private ByteBuffer dsMsgHeader;


    public GuardMessage(ConnectionConfig config) {
        header = ByteBuffer.allocate(20);
        header.putLong(4);
        header.putInt(0); //packetid
        header.putShort(GUARDIUM_VENDOR_ID);
        header.putShort(SERVICE_ID_EVENT);
        header.put((byte)0); //configId
        header.put(DBProtocol.DB2.getValue()); //todo nataly: check type and replace by real value in data
        header.put((byte)0); // uninstall
        header.put((byte)0);
        header.rewind();

        dsMsgHeader = ByteBuffer.allocate(16);
        dsMsgHeader.putLong(0);
        dsMsgHeader.putInt(0); //packetid
        dsMsgHeader.putShort(GUARDIUM_VENDOR_ID);
        dsMsgHeader.putShort(SERVICE_ID_DS_MESSAGE);
        dsMsgHeader.rewind();
    }

    public ByteBuffer getHeader() {
        return header;
    }
    public ByteBuffer getHeaderForDsMsg() {
        return dsMsgHeader;
    }

    public void setLength(long length){
        header.putLong(0, length + 4);
    }

    public void setLengthToDsMsg(long length){
        dsMsgHeader.putLong(0, length);
    }

    public void setUnInstallMessage(){
        header.put(UNINSTALL_POSITION_IN_HEADER,(byte) UNINSTALL_COMMAND);
    }
    public void unSetUnInstallMessage(){
        header.put(UNINSTALL_POSITION_IN_HEADER,(byte)0);
    }

    private static Datasource.Ping.Builder getPingBuilder(String clientId){
        return Datasource.Ping
                .newBuilder()
                .setClientIdentifier(clientId)
                .setTimestamp(getCurentTime());

    }

    public static byte[] preparePingIpv4(int masterIp, String snifNetworkAddress, String clientId)
    {
        com.ibm.guardium.proto.datasource.Datasource.Ping ping = getPingBuilder(clientId)
            .setCurrentMaster(snifNetworkAddress)
            .setCurrentMasterIp(masterIp)
            .build();
        return Guard_ds_message.newBuilder().setType(Type.PING).setPing(ping).build().toByteArray();
    }

    public static byte[] preparePingIpv6(String snifNetworkAddress, String clientId)
    {
        Datasource.Ping ping = getPingBuilder(clientId)
                .setCurrentMasterIpv6(snifNetworkAddress)
                .setIsIpv6(true)
                .setCurrentMasterIpv6Bytes(ByteString.copyFromUtf8(snifNetworkAddress))
                .build();
        return Guard_ds_message.newBuilder().setType(Type.PING).setPing(ping).build().toByteArray();
    }

    private static Datasource.Timestamp getCurentTime(){
        Instant instant = Instant.now();
        ZonedDateTime zonedInstant = instant.atZone(ZoneId.systemDefault());
        Time time = new Time(instant.toEpochMilli(), zonedInstant.getOffset().getTotalSeconds()/60, 0);
        return Utilities.getTimestamp(time);
    }

    public static Datasource.Handshake.Builder getHandshakeBuilder(String clientId, String dbType, String udsVersion)
    {
        Datasource.Handshake.Builder handshakeBuilder = Datasource.Handshake
                .newBuilder()
                .setTimestamp(getCurentTime())
                .setClientIdentifier(clientId)
                .setVendor("Guardium")
                .setProduct("Universal Connector")
                .setClientType(DB_TYPE_PREFIX+dbType)
                .setVersion(udsVersion);
        return handshakeBuilder;
    }

    public static byte[] prepareHandshakeIpv4(int masterIp, String snifNetworkAddress, String clientId, String dbType, String udsVersion)
    {
        Datasource.Handshake just_handshake = getHandshakeBuilder(clientId, dbType, udsVersion)
                .setCurrentMaster(snifNetworkAddress)
                .setCurrentMasterIp(masterIp)
                .build();
        return Guard_ds_message.newBuilder().setType(Type.HANDSHAKE).setHandshake(just_handshake).build().toByteArray();
    }

    public static byte[] prepareHandshakeIpv6(String snifNetworkAddress, String clientId, String dbType, String udsVersion)
    {
        Datasource.Handshake just_handshake = getHandshakeBuilder(clientId, dbType, udsVersion)
                .setCurrentMasterIpv6(snifNetworkAddress)
                .setCurrentMasterIpv6Bytes(ByteString.copyFromUtf8(snifNetworkAddress))
                .setVendor("Guardium")
                .setProduct("Universal Connector")
                .setClientType(DB_TYPE_PREFIX+dbType)
                .setVersion(udsVersion).build();

        return Guard_ds_message.newBuilder().setType(Type.HANDSHAKE).setHandshake(just_handshake).build().toByteArray();
    }


    //todo: remove once uc message type is ready
//    public static byte[] prepareAgentConfig(ConnectionConfig config)
//    {
//        Datasource.Agent_config_section section = Datasource.Agent_config_section
//                .newBuilder()
//                .addParameters(Datasource.Name_value_pair
//                        .newBuilder()
//                        .setName("DBType")
//                        .setValue(DBProtocol.MONGO.name()).build())
//                .addParameters(Datasource.Name_value_pair
//                        .newBuilder()
//                        .setName("DBServerDNSName")
//                        .setValue("this.server.com").build())
//                .addParameters(Datasource.Name_value_pair
//                        .newBuilder()
//                        .setName("DBServerPort")
//                        .setValue("123").build())
//                .addParameters(Datasource.Name_value_pair
//                        .newBuilder()
//                        .setName("StreamName")
//                        .setValue("blabla").build())
//                .addParameters(Datasource.Name_value_pair
//                        .newBuilder()
//                        .setName("TAP_TYPE")
//                        .setValue("AWS_KINESIS").build())
//                .addParameters(Datasource.Name_value_pair
//                        .newBuilder()
//                        .setName("Region")
//                        .setValue("Abc").build())
//                .build();
//        Datasource.Agent_config agentConfig = Datasource.Agent_config
//                .newBuilder()
//                .setClientIdentifier(clientId)
//                .addSections(section).build();
//
//        return Guard_ds_message.newBuilder().setType(Type.CONFIG).setConfig(agentConfig).build().toByteArray();
//    }

}
