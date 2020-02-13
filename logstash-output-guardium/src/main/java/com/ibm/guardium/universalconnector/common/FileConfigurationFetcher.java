package com.ibm.guardium.universalconnector.common;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.ibm.guardium.universalconnector.config.SnifferConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class FileConfigurationFetcher implements ConfigurationFetcher{
    private static Log log = LogFactory.getLog(FileConfigurationFetcher.class);
    private String fileName;
    private List<SnifferConfig> snifferConfigs = new LinkedList<>();

    public FileConfigurationFetcher(String fileName) {
        this.fileName = fileName;
    }

    public List<SnifferConfig> fetch() throws FileNotFoundException {
        JsonReader reader = new JsonReader(new FileReader(fileName));
        SnifferConfig[] sniffers = new Gson().fromJson(reader, SnifferConfig[].class);
        snifferConfigs = Arrays.asList(sniffers);
        return snifferConfigs;
    }

}