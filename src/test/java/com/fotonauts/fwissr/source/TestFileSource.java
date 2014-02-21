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

    @Test
    public void testFetchJson() throws UnsupportedEncodingException, FileNotFoundException, IOException {
        SmarterMap testConf = SmarterMap.from(
                "foo", "bar",
                "cam", SmarterMap.from("en","bert")
                );
        Fixtures.createTmpConfFile(tmpConfDir.newFile("test.json"), testConf.toJson());
        FileSource s = FileSource.fromPath(tmpConfDir.getRoot() + "/test.json");
        SmarterMap fetched = s.fetchConf();
        assertEquals(SmarterMap.from("test", testConf), fetched);
    }
}
