package com.fotonauts.fwissr;

import static com.fotonauts.fwissr.Fixtures.createTmpConfFile;
import static com.fotonauts.fwissr.Fixtures.*;
import static org.junit.Assert.*;
import static com.fotonauts.fwissr.SmarterMap.m;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
        createTmpConfFile(tmpConfDir.newFile("test.json"), testConf1.toJson());
        Registry reg = new Registry(m("refresh_period", 20));
        reg.addSource(FileSource.fromPath(tmpConfDir.getRoot().toString() + "/test.json"));
        List<String> keys = reg.getKeys();
        assertEquals("/test:/test/foo:/test/cam:/test/conf", StringUtils.join(reg.getKeys(), ":"));
    }
}
