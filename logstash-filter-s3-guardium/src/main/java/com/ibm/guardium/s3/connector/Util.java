package com.ibm.guardium.s3.connector;

public class Util {
    /**
     * Returns true if address is in IPv6 format. 
     * 
     * Examples: 
     *      2001:0db8:85a3:0000:0000:8a2e:0370:7334
     *      fe80::a00:27ff:fee0:1fcf%enp0s3
     */
    static public boolean isIPv6(String address) {
        return address.contains(":");
    }
}