# Logstash Java Plugin

This is a filter plugin for [Logstash](https://github.com/elastic/logstash). It parses a mongo DB syslog event and adds a standard "Construct" field (JSON) that can be fed into Guardium. This construct details the main verb and the objects involved.  

??It is fully free and fully open-source. The license is Apache 2.0, meaning you are free to use it however you want??

## Documentation
Supported commands:
* find, insert, delete, update, ...  
* aggregate with $lookup(s) or $graphLookup(s)
* ...

## Example 
### syslog input

    ... mongod: {...}

## filtered output
Filtered output adds field "Construct" to the event: 

    ...

This transformed event is then passed to a Mongo-Guardium Output plugin, which is responsible to send it to a Guardium machine.  

## Install
To install this plugin, clone or download, and run from your logstash installation. Replace "?" with this plugin version:
    
    $ bin/logstash-plugin install --no-verify --local /path/to/logstash-filter-mongo2guardium-?.?.?.gem

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
2. Run 

    ```$ logstash -f ./logstash-filter-java-mongodb-guardium/java-filter.conf```


## TODO
1. ~~Find common denominator, to parse main command/verb & object, to support other commands (sync with Ofer findings so far)~~
2. Complex query: 
    1. ~~Support multiple $lookup stages in pipeline~~
    2. ~~Support graphLookup~~ 
3. Integrate meta-data into .conf 
2. ~~Introduce testing (assert commands/*)~~
3. Reconstruct original mongo query? 
4. Mask sensitive (leaf-values)
5. ~~Encapsulate into logstash plugin~~ 

### Not supported/Future
1. Support fields (preferably link to objects)
2. embedded documents as inner objects(?)