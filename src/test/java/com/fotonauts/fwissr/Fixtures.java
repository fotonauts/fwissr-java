package com.fotonauts.fwissr;

import static com.fotonauts.fwissr.TextUtils.S;
import static com.fotonauts.fwissr.SmarterMap.m;
import static com.fotonauts.fwissr.SmarterList.l;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.junit.rules.TemporaryFolder;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class Fixtures {

    public static void setupGlobalConf(TemporaryFolder tmpConfDir) throws IOException {
        // create additional file sources
        File mouarfLolJson = tmpConfDir.newFile("mouarf.lol.json");
        createTmpConfFile(mouarfLolJson, S(/*{
                                           "meu" : "ringue",
                                           "pa" : { "pri" : "ka"}
                                           }*/));

        File tropMdrJson = tmpConfDir.newFile("trop.mdr.json");
        createTmpConfFile(tropMdrJson, S(/*{
                                         "gein" : "gembre",
                                         "pa" : { "ta" : "teu"}
                                         }*/));
        /*
                // create additional mongodb sources
                create_tmp_mongo_col("roque.fort", S(/ *{
                  "bar" : "baz",
                }* /));

                create_tmp_mongo_col("cam.en.bert", S(/ *
                  { "pim" : { "pam" : [ "pom", "pum" ] } }
                * /));
        */
        /*
            { "mongodb"  : "tmp_mongo_db_uri", "collection" : "roque.fort", "top_level" : true },
            { "mongodb"  : "tmp_mongo_db_uri", "collection" : "cam.en.bert" }

         */

        // create main conf file
        String fwissrConf = String.format(S(/*
                                            { "fwissr_sources" : [
                                            { "filepath" : "%s" },
                                            { "filepath" : "%s", "top_level" : true }
                                            ], "fwissr_refresh_period" : 5,  "foo" : "bar" }
                                            }*/), mouarfLolJson.toString(), tropMdrJson.toString());

        File fwissrJson = tmpConfDir.newFile("fwissr.json");
        createTmpConfFile(fwissrJson, fwissrConf);
    }

    public static void createTmpConfFile(File file, String conf) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(conf.getBytes("UTF-8"));
        fos.close();
    }

    public static void createTmpConfCollection(Mongo mongo, String collectionName, SmarterMap conf) {
        mongo.getDB("fwissr_spec").getCollection(collectionName).drop();
        for (Map.Entry<String, Serializable> entry : conf.entrySet())
            mongo.getDB("fwissr_spec").getCollection(collectionName)
                    .insert(new BasicDBObject(SmarterMap.m("_id", entry.getKey(), "value", entry.getValue())));
    }
    
    public static void dumpMongo(Mongo mongo) {
        for(String database: mongo.getDatabaseNames()) {
            System.err.println("DATABASE " + database);
            for(String collection: mongo.getDB(database).getCollectionNames()) {
                System.err.println("  collection " + collection);                
                for(DBObject doc: mongo.getDB(database).getCollection(collection).find()) {
                    System.err.println("    - " + doc.toString());                
                }
            }
        }
    }

    public static SmarterMap testConf1 = m("foo", "bar", "cam", m("en", "bert"), "conf", 1);
    public static SmarterMap testConf2 = m("jean", "bon", "terieur", m("alain", "alex"), "conf", 2);
    public static SmarterMap testConf3 = m("jean", "bon", "cam", m("et", "rat"), "conf", 3, "foo", "baz");
}
