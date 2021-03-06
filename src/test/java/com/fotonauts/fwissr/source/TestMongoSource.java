package com.fotonauts.fwissr.source;

import static com.fotonauts.fwissr.Fixtures.createTmpConfCollection;
import static com.fotonauts.fwissr.Fixtures.testConf1;
import static com.fotonauts.fwissr.Fixtures.testConf2;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import com.fotonauts.fwissr.SmarterMap;
import com.fotonauts.fwissr.TemporaryMongo;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

public class TestMongoSource {

    @ClassRule
    public static TemporaryMongo tmongo = new TemporaryMongo();

    @Before
    public void cleanDatabase() {
        tmongo.getClient().getDB("fwissr_spec").dropDatabase();
    }


    private String uriPrefix() {
        return String.format("mongodb://%s:%d", tmongo.getClient().getAddress().getHost(), tmongo.getClient().getAddress()
                .getPort());
    }

    @Test
    public void testInstantiateFromURI() {
        createTmpConfCollection(tmongo, "test", SmarterMap.m("rootkey", SmarterMap.m("foo", "bar")));
        Source source = Source.fromSettings(SmarterMap.m("mongodb", uriPrefix() + "/fwissr_spec", "collection", "test"));
        assertEquals(MongodbSource.class, source.getClass());
        DBCollection collection = ((MongodbSource) source).collection;
        assertEquals("test", collection.getName());
        assertEquals("fwissr_spec", collection.getDB().getName());
        assertEquals(1, collection.count());
        assertEquals(1, collection.find(new BasicDBObject("_id", "rootkey")).count());
    }

    @Test
    public void testShouldIgnoreDatabaseConnectionParameter() throws Exception {
        createTmpConfCollection(tmongo, "test", SmarterMap.m("rootkey", SmarterMap.m("foo", "bar")));
        Source source = Source.fromSettings(SmarterMap.m("mongodb", uriPrefix() + "/fwissr_spec?param1=value1&param2=value2", "collection", "test"));
        assertEquals(MongodbSource.class, source.getClass());
        DBCollection collection = ((MongodbSource) source).collection;
        assertEquals("test", collection.getName());
        assertEquals("fwissr_spec", collection.getDB().getName());
        assertEquals(1, collection.count());
        assertEquals(1, collection.find(new BasicDBObject("_id", "rootkey")).count());
    }

    @Test
    public void testFetchConf() {
        createTmpConfCollection(tmongo, "test", testConf1);
        MongodbSource source = MongodbSource.fromSettings(SmarterMap.m("mongodb", uriPrefix() + "/fwissr_spec", "collection",
                "test"));
        SmarterMap confFetched = source.fetchConf();
        assertEquals(SmarterMap.m("test", testConf1), confFetched);
    }

    @Test
    public void testMapCollectionNameToKeyParts() throws Exception {
        createTmpConfCollection(tmongo, "cam.en.bert", testConf1);
        MongodbSource source = MongodbSource.fromSettings(SmarterMap.m("mongodb", uriPrefix() + "/fwissr_spec", "collection",
                "cam.en.bert"));
        SmarterMap confFetched = source.fetchConf();
        assertEquals(SmarterMap.m("cam", SmarterMap.m("en", SmarterMap.m("bert", testConf1))), confFetched);
    }

    @Test
    public void testDoesNotMapNameToKeyPartsForTopLevel() throws Exception {
        createTmpConfCollection(tmongo, "fwissr", testConf1);
        MongodbSource source = MongodbSource.fromSettings(SmarterMap.m("mongodb", uriPrefix() + "/fwissr_spec", "collection",
                "fwissr"));
        SmarterMap confFetched = source.fetchConf();
        assertEquals(testConf1, confFetched);
    }

    @Test
    public void testDoesNotMapNameToKeyPartsForCustomTopLevel() throws Exception {
        createTmpConfCollection(tmongo, "cam.en.bert", testConf1);
        MongodbSource source = MongodbSource.fromSettings(SmarterMap.m("mongodb", uriPrefix() + "/fwissr_spec", "collection",
                "cam.en.bert", "top_level", true));
        SmarterMap confFetched = source.fetchConf();
        assertEquals(testConf1, confFetched);
    }

    @Test
    public void testRefreshConfIfAllowed() throws Exception {
        createTmpConfCollection(tmongo, "test", testConf1);
        MongodbSource source = MongodbSource.fromSettings(SmarterMap.m("mongodb", uriPrefix() + "/fwissr_spec", "collection",
                "test", "refresh", true));
        SmarterMap confFetched1 = source.getConf();
        assertEquals(SmarterMap.m("test", testConf1), confFetched1);
        createTmpConfCollection(tmongo, "test", testConf2);
        SmarterMap confFetched2 = source.getConf();
        assertEquals(SmarterMap.m("test", testConf2), confFetched2);
    }

    @Test
    public void testDoesNotRefreshConfIfNotAllowed() throws Exception {
        createTmpConfCollection(tmongo, "test", testConf1);
        MongodbSource source = MongodbSource.fromSettings(SmarterMap.m("mongodb", uriPrefix() + "/fwissr_spec", "collection",
                "test"));
        SmarterMap confFetched1 = source.getConf();
        assertEquals(SmarterMap.m("test", testConf1), confFetched1);
        createTmpConfCollection(tmongo, "test", testConf2);
        SmarterMap confFetched2 = source.getConf();
        assertEquals(SmarterMap.m("test", testConf1), confFetched2);
    }

    @Test
    public void testRefreshItself() throws Exception {
        createTmpConfCollection(tmongo, "test", testConf1);
        MongodbSource source = MongodbSource.fromSettings(SmarterMap.m("mongodb", uriPrefix() + "/fwissr_spec", "collection",
                "test"));
        assertEquals(SmarterMap.m("test", testConf1), source.getConf());
        createTmpConfCollection(tmongo, "test", testConf2);
        assertEquals(SmarterMap.m("test", testConf1), source.getConf());
        source.reset();
        assertEquals(SmarterMap.m("test", testConf2), source.getConf());
    }
}
