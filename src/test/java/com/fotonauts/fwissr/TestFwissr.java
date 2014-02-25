package com.fotonauts.fwissr;

import static com.fotonauts.fwissr.SmarterList.l;
import static com.fotonauts.fwissr.SmarterMap.m;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TestFwissr {

    @Rule
    public TemporaryFolder tmpConfDir = new TemporaryFolder();

    @ClassRule
    public static TemporaryMongo tmpMongo = new TemporaryMongo();
    
    @Before
    public void clearDatabase() {
        tmpMongo.getClient().getDB("fwissr_spec").dropDatabase();
    }

    @Test
    public void testGlobalRepository() throws IOException {
        Fixtures.setupGlobalConf(tmpConfDir, tmpMongo);
        Fwissr fwissr = new Fwissr(tmpConfDir.getRoot().getCanonicalPath());
        
        assertEquals("bar", fwissr.get("/foo"));
        assertEquals("baz", fwissr.get("/bar"));
        assertEquals(m("en", m("bert", m("pim", m("pam", l("pom", "pum"))))), fwissr.get("/cam"));
        assertEquals(m("bert", m("pim", m("pam", l("pom", "pum")))), fwissr.get("/cam/en"));
        assertEquals(m("pim", m("pam", l("pom", "pum"))), fwissr.get("/cam/en/bert"));
        assertEquals(m("pam", l("pom", "pum")), fwissr.get("/cam/en/bert/pim"));
        assertEquals(l("pom", "pum"), fwissr.get("/cam/en/bert/pim/pam"));
        assertEquals("gembre", fwissr.get("/gein"));
        assertEquals(m("lol", m("meu", "ringue", "pa", m("pri", "ka"))), fwissr.get("/mouarf"));
        assertEquals(m("meu", "ringue", "pa", m("pri", "ka")), fwissr.get("/mouarf/lol"));
        assertEquals("ringue", fwissr.get("/mouarf/lol/meu"));
        assertEquals(m("pri", "ka"), fwissr.get("/mouarf/lol/pa"));
        assertEquals("ka", fwissr.get("/mouarf/lol/pa/pri"));
        assertEquals(m("ta","teu"), fwissr.get("/pa"));
        assertEquals("teu", fwissr.get("/pa/ta"));
    }

    @Test
    public void testIgnoreLeadingSlash() throws Exception {
        Fixtures.setupGlobalConf(tmpConfDir, tmpMongo);
        Fwissr fwissr = new Fwissr(tmpConfDir.getRoot().getCanonicalPath());
        
        assertEquals("bar", fwissr.get("foo"));
        assertEquals(m("en", m("bert", m("pim", m("pam", l("pom", "pum"))))), fwissr.get("cam"));
        assertEquals(l("pom", "pum"), fwissr.get("cam/en/bert/pim/pam"));        
    }
    
    @Test
    public void testReadGlobalRefreshPeriod() throws Exception {
        Fixtures.setupGlobalConf(tmpConfDir, tmpMongo);
        Fwissr fwissr = new Fwissr(tmpConfDir.getRoot().getCanonicalPath());
        assertEquals(5, fwissr.getGlobalRegistry().getRefreshPeriod());
    }
}
