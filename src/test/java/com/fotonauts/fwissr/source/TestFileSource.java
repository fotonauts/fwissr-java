package com.fotonauts.fwissr.source;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.fotonauts.fwissr.Fixtures;
import com.fotonauts.fwissr.FwissrRuntimeException;
import com.fotonauts.fwissr.SmarterMap;

public class TestFileSource {

    @Rule
    public TemporaryFolder tmpConfDir = new TemporaryFolder();

    @Test
    public void testInstantiationFromURI() throws IOException {
        Fixtures.createTmpConfFile(tmpConfDir.newFile("test.json"), SmarterMap.from().toString());
        FileSource s = FileSource.fromPath(tmpConfDir.getRoot() + "/test.json");
        assertEquals(tmpConfDir.getRoot() + "/test.json", s.getPath());
    }

    @Test(expected = FwissrRuntimeException.class)
    public void testFileNotFound() throws IOException {
        Fixtures.createTmpConfFile(tmpConfDir.newFile("test.json"), SmarterMap.from().toString());
        FileSource.fromPath(tmpConfDir.getRoot() + "/pouet.json");
    }

    private SmarterMap testConf1 = SmarterMap.from("foo", "bar", "cam", SmarterMap.from("en", "bert"), "conf", 1);

    @Test
    public void testFetchJson() throws IOException {
        Fixtures.createTmpConfFile(tmpConfDir.newFile("test.json"), testConf1.toJson());
        Source s = FileSource.fromPath(tmpConfDir.getRoot() + "/test.json");
        SmarterMap fetched = s.fetchConf();
        assertEquals(SmarterMap.from("test", testConf1), fetched);
    }

    @Test
    public void testFetchYaml() throws IOException {
        Fixtures.createTmpConfFile(tmpConfDir.newFile("test.yaml"), testConf1.toYaml());
        Source s = FileSource.fromPath(tmpConfDir.getRoot() + "/test.yaml");
        SmarterMap fetched = s.fetchConf();
        assertEquals(SmarterMap.from("test", testConf1), fetched);
    }

    private SmarterMap testConf2 = SmarterMap.from("jean", "bon", "terieur", SmarterMap.from("alain", "alex"), "conf", 2);

    @Test
    public void testFetchFromDir() throws FileNotFoundException, IOException {
        Fixtures.createTmpConfFile(tmpConfDir.newFile("test1.json"), testConf1.toJson());
        Fixtures.createTmpConfFile(tmpConfDir.newFile("test2.yaml"), testConf2.toYaml());
        Source s = FileSource.fromPath(tmpConfDir.getRoot().toString());
        SmarterMap fetched = s.fetchConf();
        assertEquals(SmarterMap.from("test1", testConf1, "test2", testConf2), fetched);
    }

    @Test
    public void testMapFileNameToKeyParts() throws IOException {
        Fixtures.createTmpConfFile(tmpConfDir.newFile("test.with.parts.json"), testConf1.toJson());
        Source s = FileSource.fromPath(tmpConfDir.getRoot().toString());
        SmarterMap fetched = s.fetchConf();
        assertEquals(SmarterMap.from("test", SmarterMap.from("with", SmarterMap.from("parts", testConf1))), fetched);
    }

    @Test
    public void testDoesNotMapFileToKeyPartsForDefaultTopLevelFiles() throws IOException {
        String filename = FileSource.TOP_LEVEL_CONF_FILES.iterator().next() + ".json";
        Fixtures.createTmpConfFile(tmpConfDir.newFile(filename), testConf1.toJson());
        Source s = FileSource.fromPath(tmpConfDir.getRoot().toString() + "/" + filename);
        SmarterMap fetched = s.fetchConf();
        assertEquals(testConf1, fetched);
    }

    @Test
    public void testDoesNotMapFileToKeyPartsForCustomTopLevelFiles() throws IOException {
        Fixtures.createTmpConfFile(tmpConfDir.newFile("test.json"), testConf1.toJson());
        Source s = FileSource.fromPath(tmpConfDir.getRoot().toString() + "/test.json", SmarterMap.from("top_level", true));
        SmarterMap fetched = s.fetchConf();
        assertEquals(testConf1, fetched);
    }

    @Test
    public void testDoesRefreshConfIfAllowedTo() throws IOException {
        Fixtures.createTmpConfFile(tmpConfDir.newFile("test.json"), testConf1.toJson());
        Source s = FileSource.fromPath(tmpConfDir.getRoot().toString() + "/test.json", SmarterMap.from("refresh", true));
        SmarterMap fetched1 = s.fetchConf();
        assertEquals(SmarterMap.from("test", testConf1), fetched1);
        Fixtures.createTmpConfFile(tmpConfDir.newFile("test.json"), testConf2.toJson());
        SmarterMap fetched2 = s.fetchConf();
        assertEquals(SmarterMap.from("test", testConf2), fetched2);
    }

    @Test
    public void testDoesNotRefreshConfIfNotAllowed() throws IOException {
        Fixtures.createTmpConfFile(tmpConfDir.newFile("test.json"), testConf1.toJson());
        Source s = FileSource.fromPath(tmpConfDir.getRoot().toString() + "/test.json");
        SmarterMap fetched1 = s.getConf();
        assertEquals(SmarterMap.from("test", testConf1), fetched1);
        Fixtures.createTmpConfFile(tmpConfDir.newFile("test.json"), testConf2.toJson());
        SmarterMap fetched2 = s.getConf();
        assertEquals(SmarterMap.from("test", testConf1), fetched2);
    }

    @Test
    public void testResetItself() throws IOException {
        Fixtures.createTmpConfFile(tmpConfDir.newFile("test.json"), testConf1.toJson());
        Source s = FileSource.fromPath(tmpConfDir.getRoot().toString() + "/test.json");
        SmarterMap fetched1 = s.getConf();
        assertEquals(SmarterMap.from("test", testConf1), fetched1);

        Fixtures.createTmpConfFile(tmpConfDir.newFile("test.json"), testConf2.toJson());
        assertEquals(SmarterMap.from("test", testConf1), s.getConf());
        s.reset();
        assertEquals(SmarterMap.from("test", testConf2), s.getConf());

    }
}
