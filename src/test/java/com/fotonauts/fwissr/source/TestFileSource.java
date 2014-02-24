package com.fotonauts.fwissr.source;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.fotonauts.fwissr.FwissrRuntimeException;
import com.fotonauts.fwissr.Fixtures;
import com.fotonauts.fwissr.SmarterMap;

import static com.fotonauts.fwissr.TextUtils.S;

public class TestFileSource {

    @Rule
    public TemporaryFolder tmpConfDir = new TemporaryFolder();

    @Test
    public void testInstantiationFromURI() throws UnsupportedEncodingException, IOException {
        Fixtures.createTmpConfFile(tmpConfDir.newFile("test.json"), SmarterMap.from().toString());
        FileSource s = FileSource.fromPath(tmpConfDir.getRoot() + "/test.json");
        assertEquals(tmpConfDir.getRoot() + "/test.json", s.getPath());
    }

    @Test(expected = FwissrRuntimeException.class)
    public void testFileNotFound() throws UnsupportedEncodingException, IOException {
        Fixtures.createTmpConfFile(tmpConfDir.newFile("test.json"), SmarterMap.from().toString());
        FileSource.fromPath(tmpConfDir.getRoot() + "/pouet.json");
    }

    private SmarterMap testConf1 = SmarterMap.from("foo", "bar", "cam", SmarterMap.from("en", "bert"));

    @Test
    public void testFetchJson() throws UnsupportedEncodingException, FileNotFoundException, IOException {
        Fixtures.createTmpConfFile(tmpConfDir.newFile("test.json"), testConf1.toJson());
        FileSource s = FileSource.fromPath(tmpConfDir.getRoot() + "/test.json");
        SmarterMap fetched = s.fetchConf();
        assertEquals(SmarterMap.from("test", testConf1), fetched);
    }

    @Test
    public void testFetchYaml() throws UnsupportedEncodingException, FileNotFoundException, IOException {
        Fixtures.createTmpConfFile(tmpConfDir.newFile("test.yaml"), testConf1.toYaml());
        FileSource s = FileSource.fromPath(tmpConfDir.getRoot() + "/test.yaml");
        SmarterMap fetched = s.fetchConf();
        assertEquals(SmarterMap.from("test", testConf1), fetched);
    }

    private SmarterMap testConf2 = SmarterMap.from("jean", "bon", "terieur", SmarterMap.from("alain", "alex"));

    @Test
    public void testFetchFromDir() throws UnsupportedEncodingException, FileNotFoundException, IOException {
        Fixtures.createTmpConfFile(tmpConfDir.newFile("test1.json"), testConf1.toJson());
        Fixtures.createTmpConfFile(tmpConfDir.newFile("test2.yaml"), testConf2.toYaml());
        FileSource s = FileSource.fromPath(tmpConfDir.getRoot().toString());
        SmarterMap fetched = s.fetchConf();
        assertEquals(SmarterMap.from("test1", testConf1, "test2", testConf2), fetched);
    }

    @Test
    public void testMapFileNameToKeyParts() throws UnsupportedEncodingException, FileNotFoundException, IOException {
        Fixtures.createTmpConfFile(tmpConfDir.newFile("test.with.parts.json"), testConf1.toJson());
        FileSource s = FileSource.fromPath(tmpConfDir.getRoot().toString());
        SmarterMap fetched = s.fetchConf();
        assertEquals(SmarterMap.from("test", SmarterMap.from("with", SmarterMap.from("parts", testConf1))), fetched);
    }

    @Test
    public void testDoesNotMapFileToKeyPartsForDefaultTopLevelFiles() throws UnsupportedEncodingException, FileNotFoundException,
            IOException {
        String filename = FileSource.TOP_LEVEL_CONF_FILES.iterator().next() + ".json";
        Fixtures.createTmpConfFile(tmpConfDir.newFile(filename), testConf1.toJson());
        FileSource s = FileSource.fromPath(tmpConfDir.getRoot().toString() + "/" + filename);
        SmarterMap fetched = s.fetchConf();
        assertEquals(testConf1, fetched);
    }

    @Test
    public void testDoesNotMapFileToKeyPartsForCustomTopLevelFiles() throws UnsupportedEncodingException, FileNotFoundException,
            IOException {
        Fixtures.createTmpConfFile(tmpConfDir.newFile("test.json"), testConf1.toJson());
        FileSource s = FileSource.fromPath(tmpConfDir.getRoot().toString() + "/test.json", SmarterMap.from("top_level", true));
        SmarterMap fetched = s.fetchConf();
        assertEquals(testConf1, fetched);
    }

    public void testDoesRefreshConfIfAllowedTo() {
        
    }
}
