# Logstash Java Plugin

This is a filter plugin for [Logstash](https://github.com/elastic/logstash). It parses a mongo DB syslog event and adds a standard "Construct" field (JSON) that can be fed into Guardium. This construct details the main verb and the objects involved.  

??It is fully free and fully open-source. The license is Apache 2.0, meaning you are free to use it however you want??

## Documentation
Supported commands:
* find, insert, delete, update, ...  
* aggregate with $lookup(s) or $graphLookup(s)

## Example 
### syslog input

    2020-01-26T10:47:41.225272-05:00 qa-db51 mongod: { 'atype' : 'authCheck', 'ts' : { '$date' : '2020-01-26T10:47:41.225-0500' }, 'local' : { 'ip' : '(NONE)', 'port' : 0 }, 'remote' : { 'ip' : '(NONE)', 'port' : 0 }, 'users' : [], 'roles' : [], 'param' : { 'command' : 'listIndexes', 'ns' : 'config.system.sessions', 'args' : { 'listIndexes' : 'system.sessions', 'cursor' : {}, '$db' : 'config' } }, 'result' : 0 }

## filter result 
Filter tweaks the event by adding a new "Construct" field with a JSON string: 

    {
        \"sentences\": [ { 
            \"verb\": \"listIndexes\",
            \"objects\": [ 
                { \"name\": \"system.sessions\", \"type\": \"collection\", \"fields\": [], \"schema\": \"\" } 
                ],     
            \"descendants\": [],      
            \"fields\": [] 
        } ], 
        \"full_sql\": null, 
        \"original_sql\": null
    }

This transformed event is then passed to a Mongo-Guardium Output plugin, which is responsible to send it to a Guardium machine.  

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