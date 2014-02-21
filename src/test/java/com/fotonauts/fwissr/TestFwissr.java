package com.fotonauts.fwissr;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TestFwissr {
    
    private Fwissr fwissr;
    
    @Rule
    public TemporaryFolder tmpConfDir = new TemporaryFolder();

    @Before
    public void setupFwissr() throws IOException {
        fwissr = new Fwissr(tmpConfDir.getRoot().getCanonicalPath());
    }
    
    @Test
    public void testGlobalRepository() throws IOException {
        Fixtures.setupGlobalConf(tmpConfDir);
        assertEquals("bar", fwissr.get("/foo"));
    }
    
}
