package com.fotonauts.fwissr;

import static com.fotonauts.fwissr.Fixtures.createTmpConfFile;
import static com.fotonauts.fwissr.Fixtures.testConf1;
import static com.fotonauts.fwissr.Fixtures.testConf2;
import static com.fotonauts.fwissr.Fixtures.testConf3;
import static com.fotonauts.fwissr.Fixtures.testConf4;
import static com.fotonauts.fwissr.SmarterList.l;
import static com.fotonauts.fwissr.SmarterMap.m;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.fotonauts.fwissr.source.FileSource;

public class TestRegistry {

    @Rule
    public TemporaryFolder tmpConfDir = new TemporaryFolder();

    @Test
    public void testInstantiateWithASource() throws Exception {
        createTmpConfFile(tmpConfDir.newFile("test.json"), testConf1.toJson());
        Registry reg = new Registry(m("refresh_period", 20));
        reg.addSource(FileSource.fromPath(tmpConfDir.getRoot().toString() + "/test.json"));
        assertEquals(20, reg.getRefreshPeriod());
        assertEquals(20000, reg.getRefreshPeriodMS());
        assertEquals("bar", reg.get("/test/foo"));
        assertEquals("bert", reg.get("/test/cam/en"));
        assertEquals(m("en", "bert"), reg.get("/test/cam"));

        assertNull(reg.get("/meuh"));
        assertNull(reg.get("/test/meuh"));
        assertNull(reg.get("/test/cam/meuh"));
    }

    @Test
    public void testDefaultRefreshPeriod() throws Exception {
        Registry reg = new Registry(m());
        assertEquals(Registry.DEFAULT_REFRESH_PERIOD, reg.getRefreshPeriod());
    }

    @Test
    public void testInstantiateWithSeveralSources() throws Exception {
        createTmpConfFile(tmpConfDir.newFile("test.json"), testConf1.toJson());
        createTmpConfFile(tmpConfDir.newFile("test3.json"), testConf3.toJson());
        Registry reg = new Registry(m("refresh_period", 20));
        reg.addSource(FileSource.fromPath(tmpConfDir.getRoot().toString() + "/test.json", m("top_level", true)));

        assertEquals("bar", reg.get("/foo"));
        assertEquals("bert", reg.get("/cam/en"));
        assertEquals(m("en", "bert"), reg.get("/cam"));

        reg.addSource(FileSource.fromPath(tmpConfDir.getRoot().toString() + "/test3.json", m("top_level", true)));

        assertEquals("baz", reg.get("/foo"));
        assertEquals("bert", reg.get("/cam/en"));
        assertEquals("rat", reg.get("/cam/et"));
        assertEquals(m("en", "bert", "et", "rat"), reg.get("/cam"));
        assertEquals("bon", reg.get("/jean"));
    }

    @Test
    public void testListKeys() throws Exception {
        createTmpConfFile(tmpConfDir.newFile("test.json"), testConf4.toJson());
        Registry reg = new Registry(m("refresh_period", 20));
        reg.addSource(FileSource.fromPath(tmpConfDir.getRoot().toString() + "/test.json"));
        assertArrayEquals(l("/test", "/test/jean", "/test/cam", "/test/cam/en", "/test/cam/en/bert", "/test/foo").toArray(), reg
                .getKeys().toArray());
    }

    @Test
    public void testDump() throws Exception {
        createTmpConfFile(tmpConfDir.newFile("test.json"), testConf4.toJson());
        Registry reg = new Registry();
        reg.addSource(FileSource.fromPath(tmpConfDir.getRoot().toString() + "/test.json"));
        assertEquals(m("test", testConf4), reg.dump());
    }

    @Test
    public void testNoRefreshThreadIfUseless() throws Exception {
        createTmpConfFile(tmpConfDir.newFile("test.json"), testConf1.toJson());
        Registry reg = new Registry(m("refresh_period", 20));
        reg.addSource(FileSource.fromPath(tmpConfDir.getRoot().toString() + "/test.json"));
        assertNull(reg.refreshThread);
    }

    @Test
    public void testRefreshThreadIfUseful() throws Exception {
        createTmpConfFile(tmpConfDir.newFile("test.json"), testConf1.toJson());
        Registry reg = new Registry(m("refresh_period", 20));
        reg.addSource(FileSource.fromPath(tmpConfDir.getRoot().toString() + "/test.json", m("refresh", true)));
        assertNotNull(reg.refreshThread);
    }

    @Test
    public void testNoRefreshBeforePeriod() throws Exception {
        createTmpConfFile(tmpConfDir.newFile("test.json"), testConf1.toJson());
        Registry reg = new Registry(m("refresh_period", 3));
        reg.addSource(FileSource.fromPath(tmpConfDir.getRoot().toString() + "/test.json", m("refresh", true)));
        assertEquals(m("test", testConf1), reg.dump());

        new File(tmpConfDir.getRoot().toString() + "/test.json").delete();
        createTmpConfFile(tmpConfDir.newFile("test.json"), testConf2.toJson());
        Thread.sleep(1000);
        assertEquals(m("test", testConf1), reg.dump());
    }

    @Test
    public void testDoRefreshAfterPeriod() throws Exception {
        createTmpConfFile(tmpConfDir.newFile("test.json"), testConf1.toJson());
        Registry reg = new Registry(m("refresh_period", 3));
        reg.addSource(FileSource.fromPath(tmpConfDir.getRoot().toString() + "/test.json", m("refresh", true)));
        assertEquals(m("test", testConf1), reg.dump());
        Thread.sleep(1000);
        new File(tmpConfDir.getRoot().toString() + "/test.json").delete();
        createTmpConfFile(tmpConfDir.newFile("test.json"), testConf2.toJson());
        Thread.sleep(3000);
        assertEquals(m("test", testConf2), reg.dump());
        new File(tmpConfDir.getRoot().toString() + "/test.json").delete();
        createTmpConfFile(tmpConfDir.newFile("test.json"), testConf3.toJson());
        Thread.sleep(3000);
        assertEquals(m("test", testConf3), reg.dump());
    }

    @Test
    public void testReloads() throws Exception {
        createTmpConfFile(tmpConfDir.newFile("test.json"), testConf1.toJson());
        Registry reg = new Registry();
        reg.addSource(FileSource.fromPath(tmpConfDir.getRoot().toString() + "/test.json"));
        assertEquals(m("test", testConf1), reg.dump());
        new File(tmpConfDir.getRoot().toString() + "/test.json").delete();
        createTmpConfFile(tmpConfDir.newFile("test.json"), testConf2.toJson());
        assertEquals(m("test", testConf1), reg.dump());
        reg.reload();
        assertEquals(m("test", testConf2), reg.dump());
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testMapsAreFrozen() throws Exception {
        createTmpConfFile(tmpConfDir.newFile("test.json"), testConf1.toJson());
        Registry reg = new Registry();
        reg.addSource(FileSource.fromPath(tmpConfDir.getRoot().toString() + "/test.json"));
        ((SmarterMap) reg.get("/test/cam")).put("foo", "baz");
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testMapsAreFrozenAfterReload() throws Exception {
        createTmpConfFile(tmpConfDir.newFile("test.json"), testConf1.toJson());
        createTmpConfFile(tmpConfDir.newFile("test2.json"), testConf2.toJson());
        Registry reg = new Registry();
        reg.addSource(FileSource.fromPath(tmpConfDir.getRoot().toString() + "/test.json"));
        reg.getRegistry();
        reg.addSource(FileSource.fromPath(tmpConfDir.getRoot().toString() + "/test2.json"));
        ((SmarterMap) reg.get("/test/cam")).put("foo", "baz");
    }

}
