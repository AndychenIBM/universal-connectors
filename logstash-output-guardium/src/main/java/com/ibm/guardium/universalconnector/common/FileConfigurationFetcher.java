package com.ibm.guardium.universalconnector.common;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.ibm.guardium.universalconnector.config.SnifferConfig;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class FileConfigurationFetcher implements ConfigurationFetcher{
    private static Logger log = LogManager.getLogger(FileConfigurationFetcher.class);
    private String fileName;
    private List<SnifferConfig> snifferConfigs = new LinkedList<>();

    public FileConfigurationFetcher(String fileName) {
        this.fileName = fileName;
    }

    public List<SnifferConfig> fetch() throws FileNotFoundException {
        JsonReader reader = new JsonReader(new FileReader(fileName));
        SnifferConfig[] sniffers = new Gson().fromJson(reader, SnifferConfig[].class);
        List<SnifferConfig> allSniffers =  Arrays.asList(sniffers);
        snifferConfigs = validateSniffers(allSniffers);
        return snifferConfigs;
    }

    private List<SnifferConfig> validateSniffers(List<SnifferConfig> snifferConfigs){
        List<SnifferConfig> validated = new LinkedList<>();
        InetAddressValidator ipValidator = InetAddressValidator.getInstance();
        for (SnifferConfig snifferConfig : snifferConfigs) {
            if (snifferConfig.getPort() != 0 &&
                    (snifferConfig.getIp()!=null)) {
                validated.add(snifferConfig);
            } else {
                log.error("Invalid sniffer configuration " + snifferConfig.toString());
            }
        }
        return validated;
    }
}