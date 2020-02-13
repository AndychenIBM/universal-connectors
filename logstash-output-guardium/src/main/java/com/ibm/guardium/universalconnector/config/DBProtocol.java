package com.ibm.guardium.universalconnector.config;

public enum DBProtocol
{
    TNS(1, ""),            // Oracle
    TDS_MS(2, "MSSQLServer"), // MS SQL Server
    TDS_SYB(3, ""),        // SYBASE
    DB2(4, ""),            // IBM DB2
    MS_NMP(5, ""),         // MS SQL NAMED PIPES
    ANYWHERE_CMDSEQ(6, ""), // SYBASE ANYWHERE (IQ)
    IFX_SQLEXEC(7, ""),    // INFORMIX sqlexec
    DB2DAS(8, ""),         // DB2 Administration service
    FTP(9, ""),            // FTP RFC 959
    CIFS(10, ""),           // CIFS, SMB
    MYSQL_G(11, ""),        // MYSQL
    TRD(12, ""),            // TeraData
    ZXX(13, ""),            // IBM DB2 z/OS
    IXX(14, ""),            // IBM IXX
    IFX_DRDA(15, ""),       // IBM Informix DRDA
    AS400_G(16, ""),        // IBM Iseries
    PGRS(17, "POSTGRESQL"),           // PostgreSQL
    NZ(18, ""),             // Netezza
    GPB(19, ""),            // GPB
    MSSP(20, ""),           // MS Sharepoint
    F5(21, ""),             // F5
    AME(22, ""),
    IMS(23, ""),
    VSAM_PB(24, ""),        // VSAM
    HTTP(25, ""),           // HTTP
    HADOOP(26, ""),         // HADOOP (HRPC PROTOCOL)
    THRIFT(27, ""),         // HADOOP (THRIFT PROTOCOL)
    WGPB(28, ""),           // GOOGLE PROTOBUF
    VRTC(29, ""),           // VERTICA
    DB2I(30, ""),           // DB2I
    LDAP(31, ""),           // LDAP
    MONGO(32, ""),          // MongoDB
    CASS(33, ""),           // Cassandra
    CDC(34, ""),
    ASTER(35, ""),          // Aster
    GPLUM(36, ""),          // Greenplum
    COUCH(37, ""),
    SAP_HANA(38, ""),       // SAP HANA
    ACCUMULO(39, ""),       // Accumulo
    MARIADB(40, ""),        // MARIADB
    ASTER_RPC(41, ""),      // Aster RPC/HTTP
    HIVE(42, ""),           // HiveServer2 Thrift
    WGPB_HDP(43, ""),       // WGPB HADOOP
    FSM(44, ""),            // FSM,  aka FAM = file activity monitoring
    MAGEN(45, ""),          // MAGEN
    IMPALA(46, ""),         // Cloudera Impala Thrift
    METASTORE(47, ""),      // Hive Metastore Thrift
    HUE(48, ""),            // Hue
    WEBHDFS(49, ""),        // WebHDFS
    SOLR(50, ""),           // Solr
    HLOG(51, ""),           // HADOOP LOGS
    MYSQL_X(52, ""),        // MYSQL X PROTOCOL
    MEMSQL(53, ""),         // MEMSQL
    CASS_AUDIT(54, ""),     // CASS_AUDIT
    MAPR_AUDIT(55, ""),     // MAPR_AUDIT
    FEEDAWS(56, ""),        // Feedaws
    NEO4J(57, ""),          // NEO4J
    COSMOS_SQL(101, "COSMOS_SQL"),             // COSMOS_SQL
    COSMOS_MONGODB(102, "COSMOS_MONGODB"),     // COSMOS_MONGODB
    COSMOS_CASSANDRA(103, "COSMOS_CASSANDRA"), // COSMOS_CASSANDRA
    COSMOS_GREMLIN(104, "COSMOS_GREMLIN"),     // COSMOS_GREMLIN
    COSMOS_TABLE(105, "COSMOS_TABLE"),         // COSMOS_TABLE

    UNKNOWN_PROTOCOL(63, "UNKNOWN"),// PLEASE KEEP ORDER IN ALL RELEASES!!!!
    DBPROTOCOL_SIZE(64, "");

    private final int protocol;
    private final String name;

    DBProtocol(int protocol, String name) {
        this.protocol = protocol;
        this.name = name;
    }

    public byte getValue() {
        return (byte)protocol;
    }

    @Override
    public String toString() {
        return name;
    }

    public Boolean isProtocol(String name){
        return this.toString().equalsIgnoreCase(name);
    }
}
