# Logstash Java Plugin

[![Travis Build Status](https://travis-ci.org/logstash-plugins/logstash-output-java_output_example.svg)](https://travis-ci.org/logstash-plugins/logstash-output-java_output_example)

This is a Java plugin for [Logstash](https://github.com/elastic/logstash).

It is fully free and fully open source. The license is Apache 2.0, meaning you are free to use it however you want.

The documentation for Logstash Java plugins is available [here](https://www.elastic.co/guide/en/logstash/6.7/contributing-java-plugin.html).

for dev environments that have higher then java8 version - use options.compilerArgs += ["--release", "8"] in "tasks" sections of gradle.builc

Sniffers details configuration is taken in SniffersConfig.json,
Connector details configuration is taken from UniversalConnector.json( including how it is identified in guarduim stap lists)
Set UDS_ETC environment variable to define location of configuration files