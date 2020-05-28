# MongoDB-Guardium Logstash Filter plugin

This is a filter plugin for [Logstash](https://github.com/elastic/logstash). It parses a mongo DB syslog event into a Record instance, which standardizes the event into several parts before it is sent over to Guardium. Its parts are Accessor (who tried to access the data), Session, Data or Exception. If there was no error, the Data contains details about the query "Construct", which details the main action (verb) and collections (objects) involved.  

??It is fully free and fully open-source. The license is Apache 2.0, meaning you are free to use it however you want??

## Documentation
### Supported commands:
* find, insert, delete, update, ...  
* aggregate with $lookup(s) or $graphLookup(s)

### Supported errors:  

* Authentication error (18) – A failed login error.
* Authorization error (13) - To see the "Unauthorized ..." description properly in Guardium, you'll need to extend the report and add the "Exception description" field. 

The filter plugin also supports sending errors as well, though MongoDB Access control must be configured before these events will be logged.  For example, edit _/etc/mongod.conf_ to contain:

    security:  
        authorization: enabled



## Filter notes
* The filter supports events sent thru Syslog, which indicate "mongod:" in their message.
* Server hostname is extracted from the syslog message, 2nd field.
* Source program is not available in syslog messages sent by MongoDB. Instead, it's  always sent as "mongod". 
* If events with "(NONE)" local/remote IP are not filtered, this filter will convert IP to "0.0.0.0", as valid IPv4 format is needed.
* Events into the filter are not removed, but tagged if not parsed (see [Filter result](#filter-result), below).
* The event is redacted, so most field values are replaced with "?". Note that currently this is a naïve process, so some fields are redacted where future filter release should not redact them, like from within $lookup/$graphlookup, 1st element in $filter.cond.$eq[], etc.

## Example 
### syslog input

    2020-01-26T10:47:41.225272-05:00 qa-db51 mongod: { 'atype' : 'authCheck', 'ts' : { '$date' : '2020-01-26T10:47:41.225-0500' }, 'local' : { 'ip' : '(NONE)', 'port' : 0 }, 'remote' : { 'ip' : '(NONE)', 'port' : 0 }, 'users' : [], 'roles' : [], 'param' : { 'command' : 'listIndexes', 'ns' : 'config.system.sessions', 'args' : { 'listIndexes' : 'system.sessions', 'cursor' : {}, '$db' : 'config' } }, 'result' : 0 }

## Filter result
Filter tweaks the event by passing a Record object to the logstash Output plugin (as JSON string), which contains a "Construct" object with the parsed query: 
    {

      "sequence" => 0,
        "Record" => "{"sessionId":"n/a", "dbName":"config", "appUserName":"n/a", "time":0,"sessionLocator":{"clientIp":"tals-mbp-2.haifa.ibm.com", "clientPort":0,"serverIp":"tals-mbp-2.haifa.ibm.com", "serverPort":0,"isIpv6":false,"clientIpv6":"n/a", "serverIpv6":"n/a"},"accessor":{"dbUser":"", "serverType":"MONGODB", "serverOs":"n/a", "clientOs":"n/a", "clientHostName":"n/a", "serverHostName":"qa-db51", "commProtocol":"n/a", "dbProtocol":"Logstash", "dbProtocolVersion":"n/a", "osUser":"n/a", "sourceProgram":"mongod", "client_mac":"n/a", "serverDescription":"n/a", "serviceName":"n/a", "language":"FREE_TEXT", "type":"CONSTRUCT"},"data":{"construct":{"sentences":[{"verb":"listIndexes", "objects":[{"name":"system.sessions", "type":"collection", "fields":[],"schema":""}],"descendants":[],"fields":[]}],"full_sql":"{\"atype\":\"authCheck\",\"ts\":{\"$date\":\"2020-01-26T10:47:41.225-0500\"},\"local\":{\"ip\":\"(NONE)\",\"port\":0},\"remote\":{\"ip\":\"(NONE)\",\"port\":0},\"users\":[],\"roles\":[],\"param\":{\"command\":\"listIndexes\",\"ns\":\"config.system.sessions\",\"args\":{\"listIndexes\":\"system.sessions\",\"cursor\":{},\"$db\":\"config\"}},\"result\":0}", "original_sql":"n/a"},"timestamp":0,"originalSqlCommand":"n/a", "useConstruct":true}}",
        "@version" => "1",
        "@timestamp" => 2020-02-25T12:32:16.314Z,
          "type" => "syslog",
        "timestamp" => "2020-01-26T10:47:41.225-0500"
    }

This transformed event is then passed to a Mongo-Guardium Output plugin, which is responsible to send it to a Guardium machine. 

If parsing fails, a tag is added ("_mongoguardium_skip" or [unlikely] "_mongoguardium_json_parse_error"), which is used in logstash configuration file to pass only events that passed the filter succesffuly. 

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





