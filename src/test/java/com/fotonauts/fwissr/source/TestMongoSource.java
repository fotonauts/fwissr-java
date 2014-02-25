package com.fotonauts.fwissr.source;

import static com.fotonauts.fwissr.Fixtures.createTmpConfCollection;
import static com.fotonauts.fwissr.Fixtures.testConf1;
import static com.fotonauts.fwissr.Fixtures.testConf2;
import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
        _mongodExe = runtime.prepare(new MongodConfigBuilder().version(Version.Main.PRODUCTION)
                .net(new Net(12345, Network.localhostIsIPv6())).build());
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

    private String uriPrefix() {
        return String.format("mongodb://%s:%d", mongo.getAddress().getHost(), mongo.getAddress().getPort());
    }

    @Test
    public void testInstantiateFromURI() {
        createTmpConfCollection(mongo, "test", SmarterMap.m("rootkey", SmarterMap.m("foo", "bar")));
        Source source = Source.fromSettings(SmarterMap.m("mongodb", uriPrefix() + "/fwissr_spec", "collection", "test"));
        assertEquals(MongodbSource.class, source.getClass());
        DBCollection collection = ((MongodbSource) source).collection;
        assertEquals("test", collection.getName());
        assertEquals("fwissr_spec", collection.getDB().getName());
        assertEquals(1, collection.count());
        assertEquals(1, collection.find(new BasicDBObject("_id", "rootkey")).count());
    }

    @Test
    public void testFetchConf() {
        createTmpConfCollection(mongo, "test", testConf1);
        MongodbSource source = MongodbSource.fromSettings(SmarterMap.m("mongodb", uriPrefix() + "/fwissr_spec", "collection", "test"));
        SmarterMap confFetched = source.fetchConf();
        assertEquals(SmarterMap.m("test", testConf1), confFetched);
    }

    @Test
    public void testMapCollectionNameToKeyParts() throws Exception {
        createTmpConfCollection(mongo, "cam.en.bert", testConf1);
        MongodbSource source = MongodbSource.fromSettings(SmarterMap.m("mongodb", uriPrefix() + "/fwissr_spec", "collection", "cam.en.bert"));
        SmarterMap confFetched = source.fetchConf();
        assertEquals(SmarterMap.m("cam", SmarterMap.m("en", SmarterMap.m("bert", testConf1))), confFetched);
    }
    
    @Test
    public void testDoesNotMapNameToKeyPartsForTopLevel() throws Exception {
        createTmpConfCollection(mongo, "fwissr", testConf1);
        MongodbSource source = MongodbSource.fromSettings(SmarterMap.m("mongodb", uriPrefix() + "/fwissr_spec", "collection", "fwissr"));
        SmarterMap confFetched = source.fetchConf();
        assertEquals(testConf1, confFetched);        
    }

    @Test
    public void testDoesNotMapNameToKeyPartsForCustomTopLevel() throws Exception {
        createTmpConfCollection(mongo, "cam.en.bert", testConf1);
        MongodbSource source = MongodbSource.fromSettings(SmarterMap.m("mongodb", uriPrefix() + "/fwissr_spec", "collection", "cam.en.bert", "top_level", true));
        SmarterMap confFetched = source.fetchConf();
        assertEquals(testConf1, confFetched);        
    }
    
    @Test
    public void testRefreshConfIfAllowed() throws Exception {
        createTmpConfCollection(mongo, "test", testConf1);
        MongodbSource source = MongodbSource.fromSettings(SmarterMap.m("mongodb", uriPrefix() + "/fwissr_spec", "collection", "test", "refresh", true));
        SmarterMap confFetched1 = source.getConf();
        assertEquals(SmarterMap.m("test", testConf1), confFetched1);
        createTmpConfCollection(mongo, "test", testConf2);
        SmarterMap confFetched2 = source.getConf();
        assertEquals(SmarterMap.m("test", testConf2), confFetched2);
    }
    
    @Test
    public void testDoesNotRefreshConfIfNotAllowed() throws Exception {
        createTmpConfCollection(mongo, "test", testConf1);
        MongodbSource source = MongodbSource.fromSettings(SmarterMap.m("mongodb", uriPrefix() + "/fwissr_spec", "collection", "test"));
        SmarterMap confFetched1 = source.getConf();
        assertEquals(SmarterMap.m("test", testConf1), confFetched1);
        createTmpConfCollection(mongo, "test", testConf2);
        SmarterMap confFetched2 = source.getConf();
        assertEquals(SmarterMap.m("test", testConf1), confFetched2);
    }
    
    @Test
    public void testRefreshItself() throws Exception {
        createTmpConfCollection(mongo, "test", testConf1);
        MongodbSource source = MongodbSource.fromSettings(SmarterMap.m("mongodb", uriPrefix() + "/fwissr_spec", "collection", "test"));
        assertEquals(SmarterMap.m("test", testConf1), source.getConf());
        createTmpConfCollection(mongo, "test", testConf2);
        assertEquals(SmarterMap.m("test", testConf1), source.getConf());
        source.reset();
        assertEquals(SmarterMap.m("test", testConf2), source.getConf());        
    }
}
