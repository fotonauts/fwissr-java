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

    @Rule
    public TemporaryMongo tmpMongo = new TemporaryMongo();

    @Test
    public void testGlobalRepository() throws IOException {
        Fixtures.setupGlobalConf(tmpConfDir, tmpMongo);
        assertEquals("bar", fwissr.get("/foo"));
        // assertEquals("baz", fwissr.get("/bar")); // need mongo
        // Fwissr['/cam'].should == { 'en' => { 'bert' => { 'pim' => { 'pam' => [ 'pom', 'pum' ] } } } }
        // Fwissr['/cam/en'].should == { 'bert' => { 'pim' => { 'pam' => [ 'pom', 'pum' ] } } }
        // Fwissr['/cam/en/bert'].should == { 'pim' => { 'pam' => [ 'pom', 'pum' ] } }
        // Fwissr['/cam/en/bert/pim'].should == { 'pam' => [ 'pom', 'pum' ] }
        // Fwissr['/cam/en/bert/pim/pam'].should == [ 'pom', 'pum' ]
        //assertEquals("gembre", fwissr.get("/gein"));
        // Fwissr['/mouarf'].should == { 'lol' => { 'meu' => 'ringue', 'pa' => { 'pri' => 'ka'} } }
        // Fwissr['/mouarf/lol'].should == { 'meu' => 'ringue', 'pa' => { 'pri' => 'ka'} }
        // Fwissr['/mouarf/lol/meu'].should == 'ringue'
        // Fwissr['/mouarf/lol/pa'].should == { 'pri' => 'ka'}
        //assertEquals("ka", fwissr.get("/mouarf/lol/pa/pri"));
        // Fwissr['/pa'].should == { 'ta' => 'teu'}
        //assertEquals("teu", fwissr.get("/pa/ta"));

    }

}
