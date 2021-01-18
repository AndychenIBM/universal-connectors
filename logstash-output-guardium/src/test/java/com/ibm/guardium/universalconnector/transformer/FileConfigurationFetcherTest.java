package com.ibm.guardium.universalconnector.transformer;

import com.ibm.guardium.universalconnector.common.Environment;
import com.ibm.guardium.universalconnector.common.FileConfigurationFetcher;
import com.ibm.guardium.universalconnector.config.SnifferConfig;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.List;

public class FileConfigurationFetcherTest {

    @Test
    public void testEmptySniffers() throws IOException {
        String testeCase = "[]";
        String fileFullPath = Environment.getUcEtc()+File.separator+"SniffersTest.json";
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileFullPath));
        writer.write(testeCase);
        writer.close();
        FileConfigurationFetcher ff = new FileConfigurationFetcher(fileFullPath);
        List<SnifferConfig> snifers = ff.fetch();
        Assert.assertNotNull(snifers);
        Assert.assertEquals(0, snifers.size());
    }

    @Test
    public void testOneSniffer() throws IOException {
        String testeCase = "[{ \"ip\" : \"9.70.157.70\", \"port\" : \"16022\", \"isSSL\" : \"false\" }]";
        String fileFullPath = Environment.getUcEtc()+File.separator+"SniffersTest.json";
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileFullPath));
        writer.write(testeCase);
        writer.close();
        FileConfigurationFetcher ff = new FileConfigurationFetcher(fileFullPath);
        List<SnifferConfig> snifers = ff.fetch();
        Assert.assertNotNull(snifers);
        Assert.assertEquals(1, snifers.size());
        Assert.assertTrue("Test ip value", snifers.get(0).getIp().equals("9.70.157.70"));
        Assert.assertTrue("Test port value", snifers.get(0).getPort()==16022);
        Assert.assertTrue("Test ssl value", !snifers.get(0).isSSL());
    }

    @Test
    public void testTwoSniffers() throws IOException {
        String testeCase = "[{ \"ip\" : \"9.70.157.70\", \"port\" : \"16022\", \"isSSL\" : \"false\" }, { \"ip\" : \"9.70.157.72\", \"port\" : \"16023\", \"isSSL\" : \"true\" }]";
        String fileFullPath = Environment.getUcEtc()+File.separator+"SniffersTest.json";
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileFullPath));
        writer.write(testeCase);
        writer.close();
        FileConfigurationFetcher ff = new FileConfigurationFetcher(fileFullPath);
        List<SnifferConfig> snifers = ff.fetch();
        Assert.assertNotNull(snifers);
        Assert.assertEquals(2, snifers.size());

        Assert.assertTrue("Test 1 ip value", snifers.get(0).getIp().equals("9.70.157.70"));
        Assert.assertTrue("Test 1 port value", snifers.get(0).getPort()==16022);
        Assert.assertTrue("Test 1 ssl value", !snifers.get(0).isSSL());

        Assert.assertTrue("Test 2 ip value", snifers.get(1).getIp().equals("9.70.157.72"));
        Assert.assertTrue("Test 2 port value", snifers.get(1).getPort()==16023);
        Assert.assertTrue("Test 2 ssl value", snifers.get(1).isSSL());
    }

    @Test
    public void testInvalidSniffers() throws IOException {
        String testeCase = "[{ \"port\" : \"16022\", \"isSSL\" : \"false\" }, { \"ip\" : \"9.70.157.72\", \"port\" : \"16023\", \"isSSL\" : \"true\" }]";
        String fileFullPath = Environment.getUcEtc()+File.separator+"SniffersTest.json";
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileFullPath));
        writer.write(testeCase);
        writer.close();
        FileConfigurationFetcher ff = new FileConfigurationFetcher(fileFullPath);
        List<SnifferConfig> snifers = ff.fetch();
        Assert.assertNotNull(snifers);
        Assert.assertEquals(1, snifers.size());

        Assert.assertTrue("Test 1 ip value", snifers.get(0).getIp().equals("9.70.157.72"));
        Assert.assertTrue("Test 1 port value", snifers.get(0).getPort()==16023);
        Assert.assertTrue("Test 1 ssl value", snifers.get(0).isSSL());

    }
}
