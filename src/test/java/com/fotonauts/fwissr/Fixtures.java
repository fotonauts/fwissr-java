package com.fotonauts.fwissr;

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

    public static void setupGlobalConf(TemporaryFolder tmpConfDir, TemporaryMongo tmpMongo) throws IOException {
        // create additional file sources
        createTmpConfFile(tmpConfDir.newFile("mouarf.lol.yaml"), m("meu", "ringue", "pa", m("pri","ka")).toYaml());
        createTmpConfFile(tmpConfDir.newFile("trop.mdr.json"), m("gein", "gembre", "pa", m("ta", "teu")).toJson());
        
        // create additional mongodb sources
        createTmpConfCollection(tmpMongo, "roque.fort", m("bar", "baz"));
        createTmpConfCollection(tmpMongo, "cam.en.bert",  m("pim", m("pam", l("pom", "pum"))));

        String mongodb = String.format("mongodb://%s:%d/fwissr_spec", tmpMongo.getClient().getAddress().getHost(), tmpMongo
                .getClient().getAddress().getPort());
        
        createTmpConfFile(tmpConfDir.newFile("fwissr.json"),
                m("fwissr_sources", l(
                    m("filepath", tmpConfDir.getRoot() + "/mouarf.lol.yaml"),
                    m("filepath", tmpConfDir.getRoot() + "/trop.mdr.json", "top_level", true),
                    m("mongodb", mongodb, "collection", "roque.fort", "top_level", true),
                    m("mongodb", mongodb, "collection", "cam.en.bert")
                ), "fwissr_refresh_period", 5, "foo", "bar").toJson());
    }

    public static void createTmpConfFile(File file, String conf) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(conf.getBytes("UTF-8"));
        fos.close();
    }

    public static void createTmpConfCollection(TemporaryMongo mongo, String collectionName, SmarterMap conf) {
        mongo.getClient().getDB("fwissr_spec").getCollection(collectionName).drop();
        for (Map.Entry<String, Serializable> entry : conf.entrySet())
            mongo.getClient().getDB("fwissr_spec").getCollection(collectionName)
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
    public static SmarterMap testConf4 = m("foo", "bar", "jean", l("bon", "rage"), "cam", m("en", m("bert", "coulant")));
}
