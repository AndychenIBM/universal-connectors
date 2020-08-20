# MongoDB-Guardium Logstash Filter plugin

This is a filter plugin for [Logstash](https://github.com/elastic/logstash). It parses a mongo DB syslog event into a Record instance, which standardizes the event into several parts before it is sent over to Guardium. Its parts are Accessor (who tried to access the data), Session, Data or Exception. If there was no error, the Data contains details about the query "Construct", which details the main action (verb) and collections (objects) involved.  

??It is fully free and fully open-source. The license is Apache 2.0, meaning you are free to use it however you want??

## Documentation
### Supported audit messages & commands: 
* authCheck: 
    * find, insert, delete, update, create, drop, ... 
    * aggregate with $lookup(s) or $graphLookup(s)
    * applyOps: An internal command but can be triggered manually to create/drop collection. It's object is written as "\[JSON-object\]" in Guardium; details are included inGuardium Full SQL field, if available. 
* authenticate (with error only) 

Notes: 
* For these events to be handled propertly, few conditions must occur: 
    * MongoDB access control must be set, as messages without users are removed. 
    * authCheck and authenticate events should not be filtered-out from the MongoDB audit log messages.
* Other MongoDB events/messages are removed from pipeline, as their data is already parsed in authcheck message.
* Non-MongoDB events are skipped, but not removed (left for other filters).

### Supported errors:  

* Authentication error (18) – A failed login error.
* Authorization error (13) - To see the "Unauthorized ..." description properly in Guardium, you'll need to extend the report and add the "Exception description" field. 

The filter plugin also supports sending errors as well, though MongoDB Access control must be configured before these events will be logged.  For example, edit _/etc/mongod.conf_ to contain:

    security:  
        authorization: enabled

*IPv6* addresses can be supported by MongoDB & Filter plugin, but not tested yet and need further support by Guardium pipeline. 

## Filter notes
* The filter supports events sent thru Syslog or Filebeat, which indicate "mongod:" in their message.
* Field _server_hostname_ (required) - Server hostname is expected (extracted from syslog message, 2nd field).
* Field _server_ip_ - States the IP of the MongoDB server; if it is available for the filter plugin, the filter will use it instead localhost IPs reported by MongoDB, if actions were performed on the DB server itself. 
* Client "Source program" is not available in messages sent by MongoDB. 
* If events with "(NONE)" local/remote IP are not filtered (unlikely, as messages without users are filtered-out), the filter plugin will convert the IP to "0.0.0.0", as a valid format is needed.
* Events into the filter are not removed, but tagged if not parsed (see [Filter result](#filter-result), below).
* MongoDB authCheck audit messages are also sent in a redacted version, where most field values are replaced with "?". Note that currently this is a naïve process, where most command arguments are redacted, apart from the the command, $db, and $lookup & $graphLookup required arguments (from, localField, foreignField, as, connectFromField, connectToField). Future filter release may add to this list.

## Example 
### syslog input

    2020-01-26T10:47:41.225272-05:00 qa-db51 mongod: { 'atype' : 'authCheck', 'ts' : { '$date' : '2020-01-26T10:47:41.225-0500' }, 'local' : { 'ip' : '(NONE)', 'port' : 0 }, 'remote' : { 'ip' : '(NONE)', 'port' : 0 }, 'users' : [], 'roles' : [], 'param' : { 'command' : 'listIndexes', 'ns' : 'config.system.sessions', 'args' : { 'listIndexes' : 'system.sessions', 'cursor' : {}, '$db' : 'config' } }, 'result' : 0 }

## Filter result
Filter tweaks the event by passing a Record object to the logstash Output plugin (as JSON string), in a field "GuardRecord" (GuardConstants.GUARDIUM_RECORD_FIELD_NAME) which contains a "Construct" object with the parsed query: 
    {

      "sequence" => 0,
        "GuardRecord" => "{"sessionId":"", "dbName":"config", "appUserName":"", "time":0,"sessionLocator":{"clientIp":"tals-mbp-2.haifa.ibm.com", "clientPort":0,"serverIp":"tals-mbp-2.haifa.ibm.com", "serverPort":0,"isIpv6":false,"clientIpv6":"", "serverIpv6":""},"accessor":{"dbUser":"", "serverType":"MongoDB", "serverOs":"", "clientOs":"", "clientHostName":"", "serverHostName":"qa-db51", "commProtocol":"", "dbProtocol":"MongoDB native audit", "dbProtocolVersion":"", "osUser":"", "sourceProgram":"mongod", "client_mac":"", "serverDescription":"", "serviceName":"", "language":"FREE_TEXT", "type":"CONSTRUCT"},"data":{"construct":{"sentences":[{"verb":"listIndexes", "objects":[{"name":"system.sessions", "type":"collection", "fields":[],"schema":""}],"descendants":[],"fields":[]}],"full_sql":"{\"atype\":\"authCheck\",\"ts\":{\"$date\":\"2020-01-26T10:47:41.225-0500\"},\"local\":{\"ip\":\"(NONE)\",\"port\":0},\"remote\":{\"ip\":\"(NONE)\",\"port\":0},\"users\":[],\"roles\":[],\"param\":{\"command\":\"listIndexes\",\"ns\":\"config.system.sessions\",\"args\":{\"listIndexes\":\"system.sessions\",\"cursor\":{},\"$db\":\"config\"}},\"result\":0}", "original_sql":""},"timestamp":0,"originalSqlCommand":"", "useConstruct":true}}",
        "@version" => "1",
        "@timestamp" => 2020-02-25T12:32:16.314Z,
          "type" => "syslog",
        "timestamp" => "2020-01-26T10:47:41.225-0500"
    }

This transformed event is then passed to a Mongo-Guardium Output plugin, which is responsible to send it to a Guardium machine. 

If event message is not related to MongoDB, the event is tagged with  "_mongoguardium_skip_not_mongodb" (not removed from pipeline). If it's an event from MongoDB but JSON parsing fails, the event is tagged with "_mongoguardium_json_parse_error" (this may happen if syslog message is too long and was truncated). These tags are used in a logstash configuration file to pass only events that passed the filter succesfully. 

## Install
To install this plugin, clone or download, and run from your logstash installation. Replace "?" with this plugin version:
    
    $ logstash-plugin install --no-verify --local ./logstash-filter-mongo-guardium-?.?.?.gem

Note: logstash-plugin may not handle relative paths well, so stick to calling it from the folder your gem is located, as in the example, above. 

## Contribute

The documentation for Logstash Java plugins is available [here](https://www.elastic.co/guide/en/logstash/current/contributing-java-plugin.html).

You can either enhance this filter, or use it to create a different filter for Guardium. For example, use it as a basis to create a parser for a different database than mongo.

To build & create an updated gem, which can be installed onto logstash: 
1. Build logstash from repository source
2. Create/Edit _gradle.properties_ and add LOGSTASH_CORE_PATH variable with the path to the logstash-core folder. For example: 
    
    ```LOGSTASH_CORE_PATH=/Users/taldan/logstash76/logstash-core```

3. Run ```$ ./gradlew.unix gem --info```

To test installation on your development logstash
1. Install logstash (using Brew, for example)
2. Install the filter plugin (see above)
2. Run 

    ```$ logstash -f ./filter-test.conf --config.reload.automatic```


## TODO
1. ~~Find common denominator, to parse main command/verb & object, to support other commands (sync with Ofer findings so far)~~
2. Complex query: 
    1. ~~Support multiple $lookup stages in pipeline~~
    2. ~~Support graphLookup~~ 
    3. (?) DBREFs? (convention to ref to another db/collection)?
3. Integrate meta-data into .conf 
    1. (?) Filter to parse meta-data? 
2. ~~Introduce testing (assert commands/*)~~
3. Complete output:  
    1. Original query
    2. Masked original query (leaf-values)
5. ~~Encapsulate into logstash plugin~~ 

### Not supported/Future
1. Support fields (preferably link to objects)
2. embedded documents as inner objects(?)





