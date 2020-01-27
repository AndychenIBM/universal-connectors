package com.ibm.guardium;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Parser {

    /**
     * Parses a MongoDB native audit sent over syslog. Format looks as the database.profiler. 
     * @param data
     * @return
     */
    public static String Parse(final JsonObject data) {

        try {
            
            final Construct construct = Parser.ParseAsConstruct(data);

            final GsonBuilder builder = new GsonBuilder();
            builder.setPrettyPrinting().serializeNulls();
            final Gson gson = builder.create();
            return gson.toJson(construct);

        } catch (final Exception e) {
            throw e;
        }

    }

    
    /**
     * Parses a MongoDB native audit sent over syslog. 
     * 
     * JSON format appreas after "mongod: " string and expected format is the same as mongodb database.profiler.
     * For example: { "atype": "authCheck", "ts": { "$date": "2020-01-14T10:46:02.431-0500" }, "local": { "ip": "127.0.0.1", "port": 27017 }, "remote": { "ip": "127.0.0.1", "port": 33708 }, "users": [], "roles": [], "param": { "command": "find", "ns": "test.bios", "args": { "find": "bios", "filter": {}, "lsid": { "id": { "$binary": "hg6ugx4ASiGWKSPiDRlEFw==", "$type": "04" } }, "$db": "test" } }, "result": 0 },
     *  
     * @return Construct    Used by Parse and for easier testing, as well
     * @author  Tal Daniel
     */
    public static Construct ParseAsConstruct(final JsonObject data) {
        try {
            // mongo statics
            final JsonObject param = data.get("param").getAsJsonObject();
            final String command = param.get("command").getAsString();
            final JsonObject args = param.getAsJsonObject("args");

            // + main object
            final Sentence sentence = new Sentence(command);
                if (args.has(command)) {
                    final SentenceObject sentenceObject = new SentenceObject(args.get(command).getAsString());
                    sentence.objects.add(sentenceObject);
                }

            switch (command) {
            case "aggregate":
                /*
                 Assumes no inner-lookups; only sequential stages in pipeline. 
                 */
                final JsonArray pipeline = args.getAsJsonArray("pipeline");
                if (pipeline != null && pipeline.size() > 0) {
                    for (final JsonElement stage : pipeline) {
                        // handle * lookups
                        // + object if stage has $lookup or $graphLookup: { from: obj2 }
                        JsonObject lookupStage = null;
                        
                        if (stage.getAsJsonObject().has("$lookup")) {
                            lookupStage = stage.getAsJsonObject().getAsJsonObject("$lookup");
                        } else if (stage.getAsJsonObject().has("$graphLookup")) {
                            lookupStage = stage.getAsJsonObject().getAsJsonObject("$graphLookup");
                        }
                        
                        if (lookupStage != null && lookupStage.has("from")) {
                            final SentenceObject lookupStageObject = new SentenceObject(
                                    lookupStage.get("from").getAsString());
                            // + object
                            sentence.objects.add(lookupStageObject);
                        }
                    }
                }
            default: // find, insert, delete, update, ...
                break;
            }

            final Construct construct = new Construct();
            construct.sentences.add(sentence);
            return construct;
        } catch (final Exception e) {
            throw e;
        }
    }
}