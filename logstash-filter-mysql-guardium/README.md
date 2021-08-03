# MySql-Guardium Logstash Filter plugin

This is a [Logstash](https://github.com/elastic/logstash) filter plugin for Guardium Universal connector, a feature in IBM Security Guardium. It parses a MySql audit log events/messages into a Guardium Record instance – a standard structure made out of several parts – before it is sent over to Guardium. Guardium Record parts, in brief, are Accessor (who tried to access the data), Session, Data or Exception. If there was no error, the Data contains details about the query "Construct", which details the main action (verb) and collections (objects) involved.  

It is fully free and fully open-source. The license is Apache 2.0, meaning you are free to use it however you want, like use it as a starting point to develop another filter plugin for Guardium Universal connector.

## Documentation
### Supported audit messages class: 
* connection: 
    * connect event
* general:
    * command
        Init DB, query

Notes: 
* For these events to be handled propertly, few conditions must occur: 
    * connection and general class events should not be filtered-out from the MySql audit log messages.
* Other MySql events/messages are ignored from pipeline
* Non-MySql events are only skipped and not removed from pipline, as they may be used by other filters along the Connector configuration pipeline.

### Supported errors:  

* LOGIN_FAILED
* SQL_ERROR

*IPv6* addresses can be supported by MySql & Filter plugin, but not tested yet and need further support by Guardium pipeline. 

## Filter notes
* The filter supports events sent thru Syslog or Filebeat, and counts on a "mysql_audit_log:" prefix in the event message, before the JSON part of the audit is parsed. This could be improved, but on the otherhand it saves parsing time. 
* Field _server_hostname_ (required) - Server hostname is expected (extracted from syslog message, 2nd field).
* Field _server_ip_ - States the IP of the MySql server; if it is available for the filter plugin, the filter will use it instead localhost IPs reported by MySql, if actions were performed on the DB server itself. 
* Client "Source program" is currently not available in messages sent by MySql, as this datum is sent only on the first audit log message upon DB connection, and this filter plugin doesn't aggregate data from different messages, but rather only parses the current message at hand.  
* If events with "(NONE)" local/remote IP are not filtered (unlikely, as messages without users are filtered-out), the filter plugin will convert the IP to "0.0.0.0", as a valid format for IP is needed.
* Events into the filter are not removed, but tagged if not parsed (see [Filter result](#filter-result), below).


## Example 
### syslog input

    Aug 26 13:31:27 rh7u4x64t mysql_audit_log: { "timestamp": "2020-08-26 17:31:22", "id": 1, "class": "general", "event": "status", "connection_id": 42, "account": { "user": "guardium_qa", "host": "" }, "login": { "user": "guardium_qa", "os": "", "ip": "9.80.196.128", "proxy": "" }, "general_data": { "command": "Query", "sql_command": "select", "query": "\/* ApplicationName=DBeaver 7.1.4 - SQLEditor <Script.sql> *\/ select * from pet\nLIMIT 0, 200", "status": 0 } },

## Filter result
Filter tweaks the event by adding a _GuardRecord_ field to the incoming Event, with a JSON representation of a Guardium Record object. As this filter takes responsiblity of breaking the DB command into it's atomic parts, this filter details the "Construct" object with the parsed command structure: 
{
             "program" => "mysql_audit_log",
         "GuardRecord" => "{\"sessionId\":\"42\",\"dbName\":\"\",\"appUserName\":\"\",\"time\":{\"timstamp\":1598477482000,\"minOffsetFromGMT\":0,\"minDst\":0},\"sessionLocator\":{\"clientIp\":\"9.80.196.128\",\"clientPort\":-1,\"serverIp\":\"0.0.0.0\",\"serverPort\":-1,\"isIpv6\":false,\"clientIpv6\":\"\",\"serverIpv6\":\"\"},\"accessor\":{\"dbUser\":\"guardium_qa\",\"serverType\":\"MySql\",\"serverOs\":\"\",\"clientOs\":\"\",\"clientHostName\":\"\",\"serverHostName\":\"rh7u4x64t\",\"commProtocol\":\"\",\"dbProtocol\":\"MySQL native audit\",\"dbProtocolVersion\":\"\",\"osUser\":\"\",\"sourceProgram\":\"\",\"client_mac\":\"\",\"serverDescription\":\"\",\"serviceName\":\"\",\"language\":\"MYSQL\",\"dataType\":\"TEXT\"},\"data\":{\"construct\":null,\"originalSqlCommand\":\"/* ApplicationName\\u003dDBeaver 7.1.4 - SQLEditor \\u003cScript.sql\\u003e */ select * from pet\\nLIMIT 0, 200\"},\"exception\":null}",
          "@timestamp" => 2020-10-13T12:49:47.842Z,
    "syslog_timestamp" => "Aug 26 13:31:27",
      "source_program" => "mysql_audit_log",
            "@version" => "1",
     "server_hostname" => "rh7u4x64t",
            "sequence" => 0,
                "type" => "syslog"
}

This Guardium Record, which was added to Logstash Event after the filter, is examined and handled by Guardium universal connector (in an output stage) and inserted into Guardium. 

If event message is not related to MySql, the event is tagged with  "_mysqlguardium_ignore" (not removed from pipeline). If it's an event from MySql but JSON parsing fails, the event is tagged with "_mysqlguardium_parse_error" but not removed (this may happen if syslog message is too long and was truncated). These tags can be useful for debugging purposes. 


To build & create an updated GEM of this filter plugin, which can be installed onto Logstash: 
1. Build Logstash from repository source
2. Create/Edit _gradle.properties_ and add LOGSTASH_CORE_PATH variable with the path to the logstash-core folder. For example: 
    
    ```LOGSTASH_CORE_PATH=/Users/taldan/logstash76/logstash-core```

3. Run ```$ ./gradlew.unix gem --info``` to create the GEM. 

## Install
To install this plugin on your local developer machine with Logstash installed, clone or download, and run from your logstash installation. Replace "?" with this plugin version:
    
    $ logstash-plugin install --local ./logstash-filter-mysql_filter_guardium-?.?.?.gem

Note: logstash-plugin may not handle relative paths well, so stick to calling it from the folder your gem is located, as in the example, above. 

To test filter installation on your development logstash
1. Install logstash (using Brew, for example)
2. Install the filter plugin (see above)
2. Run 

    ```$ logstash -f ./filter-test.conf --config.reload.automatic```


### Not supported/Future
1. Support fields (preferably link to objects)
2. embedded documents as inner objects(?)


## Contribute
You can either enhance this filter and open a pull request to suggest your changes, or use the project to create a different filter plugin for Guardium that supports other data source.


## References
See [documentation for Logstash Java plugins](https://www.elastic.co/guide/en/logstash/current/contributing-java-plugin.html).

See [Guardium Universal connector commons](https://www.github.com/IBM/guardium-universalconnector-commons) library for more details regarding the standard Guardium Record object.

