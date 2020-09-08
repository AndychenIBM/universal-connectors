# MongoDB-Guardium Logstash Filter plugin

This is a [Logstash](https://github.com/elastic/logstash) filter plugin for Guardium Universal connector, a feature in IBM Security Guardium. It parses a Mongo DB audit log events/messages into a Guardium Record instance – a standard structure made out of several parts – before it is sent over to Guardium. Guardium Record parts, in brief, are Accessor (who tried to access the data), Session, Data or Exception. If there was no error, the Data contains details about the query "Construct", which details the main action (verb) and collections (objects) involved.  

It is fully free and fully open-source. The license is Apache 2.0, meaning you are free to use it however you want, like use it as a starting point to develop another filter plugin for Guardium Universal connector.

## Documentation
### Supported audit messages & commands: 
* authCheck: 
    * find, insert, delete, update, create, drop, ... 
    * aggregate with $lookup(s) or $graphLookup(s)
    * applyOps: An internal command but can be triggered manually to create/drop collection. It's object is written as "\[json-object\]" in Guardium; details are included inGuardium Full SQL field, if available. 
* authenticate (with error only) 

Notes: 
* For these events to be handled propertly, few conditions must occur: 
    * MongoDB access control must be set, as messages without users are removed. 
    * authCheck and authenticate events should not be filtered-out from the MongoDB audit log messages.
* Other MongoDB events/messages are removed from pipeline, as their data is already parsed in authcheck message.
* Non-MongoDB events are only skipped and not removed from pipline, as they may be used by other filters along the Connector configuration pipeline.

### Supported errors:  

* Authentication error (18) – A failed login error.
* Authorization error (13) - To see the "Unauthorized ..." description in Guardium, you'll need to extend the report and add the "Exception description" field. 

The filter plugin also supports sending errors as well, though MongoDB Access control must be configured before these events will be logged. For example, edit _/etc/mongod.conf_ to contain:

    security:  
        authorization: enabled

*IPv6* addresses can be supported by MongoDB & Filter plugin, but not tested yet and need further support by Guardium pipeline. 

## Filter notes
* The filter supports events sent thru Syslog or Filebeat, and counts on a "mongod:" prefix in the event message, before the JSON part of the audit is parsed. This could be improved, but on the otherhand it saves parsing time. 
* Field _server_hostname_ (required) - Server hostname is expected (extracted from syslog message, 2nd field).
* Field _server_ip_ - States the IP of the MongoDB server; if it is available for the filter plugin, the filter will use it instead localhost IPs reported by MongoDB, if actions were performed on the DB server itself. 
* Client "Source program" is currently not available in messages sent by MongoDB, as this datum is sent only on the first audit log message upon DB connection, and this filter plugin doesn't aggregate data from different messages, but rather only parses the current message at hand.  
* If events with "(NONE)" local/remote IP are not filtered (unlikely, as messages without users are filtered-out), the filter plugin will convert the IP to "0.0.0.0", as a valid format for IP is needed.
* Events into the filter are not removed, but tagged if not parsed (see [Filter result](#filter-result), below).
* The filter also redacts (masks) the audit messages of type MongoDB authCheck: Currently, most field values are replaced with "?" in a naïve process, where most command arguments are redacted, apart from the the _command_, _$db_, and _$lookup_ & _$graphLookup_ required arguments (_from_, _localField_, _foreignField_, _as_, _connectFromField_, _connectToField_).

## Example 
### syslog input

    2020-01-26T10:47:41.225272-05:00 test-server05 mongod: { "atype" : "authCheck", "ts" : { "$date" : "2020-06-11T09:44:11.070-0400" }, "local" : { "ip" : "9.70.147.59", "port" : 27017 }, "remote" : { "ip" : "9.148.202.94", "port" : 60185 }, "users" : [ { "user" : "realAdmin", "db" : "admin" } ], "roles" : [ { "role" : "readWriteAnyDatabase", "db" : "admin" }, { "role" : "userAdminAnyDatabase", "db" : "admin" } ], "param" : { "command" : "find", "ns" : "admin.USERS", "args" : { "find" : "USERS", "filter" : {}, "lsid" : { "id" : { "$binary" : "mV20eHvvRha2ELTeqJxQJg==", "$type" : "04" } }, "$db" : "admin", "$readPreference" : { "mode" : "primaryPreferred" } } }, "result" : 0 }

## Filter result
Filter tweaks the event by adding a _GuardRecord_ field to the incoming Event, with a JSON representation of a Guardium Record object. As this filter takes responsiblity of breaking the DB command into it's atomic parts, this filter details the "Construct" object with the parsed command structure: 
    {

      "sequence" => 0,
        "GuardRecord" => "{"sessionId":"mV20eHvvRha2ELTeqJxQJg\u003d\u003d","dbName":"admin","appUserName":"","time":{"timstamp":1591883051070,"minOffsetFromGMT":-240,"minDst":0},"sessionLocator":{"clientIp":"9.148.202.94","clientPort":60185,"serverIp":"9.70.147.59","serverPort":27017,"isIpv6":false,"clientIpv6":"","serverIpv6":""},"accessor":{"dbUser":"realAdmin ","serverType":"MongoDB","serverOs":"","clientOs":"","clientHostName":"","serverHostName":"","commProtocol":"","dbProtocol":"MongoDB native audit","dbProtocolVersion":"","osUser":"","sourceProgram":"","client_mac":"","serverDescription":"","serviceName":"admin","language":"FREE_TEXT","dataType":"CONSTRUCT"},"data":{"construct":{"sentences":[{"verb":"find","objects":[{"name":"USERS","type":"collection","fields":[],"schema":""}],"descendants":[],"fields":[]}],"fullSql":"{\"atype\":\"authCheck\",\"ts\":{\"$date\":\"2020-06-11T09:44:11.070-0400\"},\"local\":{\"ip\":\"9.70.147.59\",\"port\":27017},\"remote\":{\"ip\":\"9.148.202.94\",\"port\":60185},\"users\":[{\"user\":\"realAdmin\",\"db\":\"admin\"}],\"roles\":[{\"role\":\"readWriteAnyDatabase\",\"db\":\"admin\"},{\"role\":\"userAdminAnyDatabase\",\"db\":\"admin\"}],\"param\":{\"command\":\"find\",\"ns\":\"admin.USERS\",\"args\":{\"find\":\"USERS\",\"filter\":{},\"lsid\":{\"id\":{\"$binary\":\"mV20eHvvRha2ELTeqJxQJg\u003d\u003d\",\"$type\":\"04\"}},\"$db\":\"admin\",\"$readPreference\":{\"mode\":\"primaryPreferred\"}}},\"result\":0}","redactedSensitiveDataSql":"{\"atype\":\"authCheck\",\"ts\":{\"$date\":\"2020-06-11T09:44:11.070-0400\"},\"local\":{\"ip\":\"9.70.147.59\",\"port\":27017},\"remote\":{\"ip\":\"9.148.202.94\",\"port\":60185},\"users\":[{\"user\":\"realAdmin\",\"db\":\"admin\"}],\"roles\":[{\"role\":\"readWriteAnyDatabase\",\"db\":\"admin\"},{\"role\":\"userAdminAnyDatabase\",\"db\":\"admin\"}],\"param\":{\"command\":\"find\",\"ns\":\"admin.USERS\",\"args\":{\"filter\":{},\"lsid\":{\"id\":{\"$binary\":\"?\",\"$type\":\"?\"}},\"$readPreference\":{\"mode\":\"?\"},\"find\":\"USERS\",\"$db\":\"admin\"}},\"result\":0}"},"originalSqlCommand":""},"exception":null}",
        "@version" => "1",
        "@timestamp" => 2020-02-25T12:32:16.314Z,
          "type" => "syslog",
        "timestamp" => "2020-01-26T10:47:41.225-0500"
    }

This Guardium Record, which was added to Logstash Event after the filter, is examined and handled by Guardium universal connector (in an output stage) and inserted into Guardium. 

If event message is not related to MongoDB, the event is tagged with  "_mongoguardium_skip_not_mongodb" (not removed from pipeline). If it's an event from MongoDB but JSON parsing fails, the event is tagged with "_mongoguardium_json_parse_error" but not removed (this may happen if syslog message is too long and was truncated). These tags can be useful for debugging purposes. 


To build & create an updated GEM of this filter plugin, which can be installed onto Logstash: 
1. Build Logstash from repository source
2. Create/Edit _gradle.properties_ and add LOGSTASH_CORE_PATH variable with the path to the logstash-core folder. For example: 
    
    ```LOGSTASH_CORE_PATH=/Users/taldan/logstash76/logstash-core```

3. Run ```$ ./gradlew.unix gem --info``` to create the GEM. 

## Install
To install this plugin on your local developer machine with Logstash installed, clone or download, and run from your logstash installation. Replace "?" with this plugin version:
    
    $ logstash-plugin install --no-verify --local ./logstash-filter-mongodb-guardium-?.?.?.gem

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
You can either enhance this filter and open a pull request to suggest your changes, or use the project to create a different filter for Guardium that supports other data source.


## References
See The documentation for Logstash Java plugins is available [here](https://www.elastic.co/guide/en/logstash/current/contributing-java-plugin.html).

See the Guardium Universal connector commons library for more details regarding the standard Guardium Record object.

