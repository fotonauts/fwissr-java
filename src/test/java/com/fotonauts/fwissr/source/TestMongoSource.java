package com.fotonauts.fwissr.source;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fotonauts.fwissr.Fixtures;
import com.fotonauts.fwissr.SmarterMap;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

public class TestMongoSource {

    private static MongodExecutable _mongodExe;
    private static MongodProcess _mongod;
    private static MongoClient mongo;

    @BeforeClass
    public static void setUp() throws Exception {

        MongodStarter runtime = MongodStarter.getDefaultInstance();
        _mongodExe = runtime.prepare(new MongodConfigBuilder()
            .version(Version.Main.PRODUCTION)
            .net(new Net(12345, Network.localhostIsIPv6()))
            .build());
        _mongod = _mongodExe.start();

        mongo = new MongoClient("localhost", 12345);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        _mongod.stop();
        _mongodExe.stop();
    }
    
    @Before
    public void clearDatabase() throws Exception {
        mongo.dropDatabase("fwissr_spec");
    }
    
    
    @Test
    public void testInstantiateFromURI() {
        Fixtures.createTmpConfCollection(mongo, "test", SmarterMap.from("rootkey", SmarterMap.from("foo", "bar")));
        Fixtures.dumpMongo(mongo);
        String uri = String.format("mongodb://%s:%d", mongo.getAddress().getHost(), mongo.getAddress().getPort());
        Source source = Source.fromSettings(SmarterMap.from("mongodb", uri + "/fwissr_spec", "collection", "test"));
        assertEquals(MongodbSource.class, source.getClass());
        DBCollection collection = ((MongodbSource) source).collection;
        assertEquals("test", collection.getName());
        assertEquals("fwissr_spec", collection.getDB().getName());
        assertEquals(1, collection.count());
        assertEquals(1, collection.find(new BasicDBObject("_id", "rootkey")).count());
    }

}
