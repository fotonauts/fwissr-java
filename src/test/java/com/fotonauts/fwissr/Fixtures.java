package com.fotonauts.fwissr;

import static com.fotonauts.fwissr.TextUtils.S;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.junit.rules.TemporaryFolder;

public class Fixtures {

    public static void setupGlobalConf(TemporaryFolder tmpConfDir) throws IOException {
        // create additional file sources
        File mouarfLolJson = tmpConfDir.newFile("mouarf.lol.json");
        createTmpConfFile(mouarfLolJson, S(/*{
          "meu" : "ringue",
          "pa" : { "pri" : "ka"}
        }*/));

        File tropMdrJson = tmpConfDir.newFile("trop.mdr.json");
        createTmpConfFile(tropMdrJson, S(/*{
          "gein" : "gembre",
          "pa" : { "ta" : "teu"}
        }*/));
/*
        // create additional mongodb sources
        create_tmp_mongo_col("roque.fort", S(/ *{
          "bar" : "baz",
        }* /));

        create_tmp_mongo_col("cam.en.bert", S(/ *
          { "pim" : { "pam" : [ "pom", "pum" ] } }
        * /));
*/
        /*
            { "mongodb"  : "tmp_mongo_db_uri", "collection" : "roque.fort", "top_level" : true },
            { "mongodb"  : "tmp_mongo_db_uri", "collection" : "cam.en.bert" }

         */
        
        // create main conf file
        String fwissrConf = String.format(S(/*
        { "fwissr_sources" : [
            { "filepath" : "%s" },
            { "filepath" : "%s", "top_level" : true }
          ], "fwissr_refresh_period" : 5,  "foo" : "bar" }
        }*/), mouarfLolJson.toString(), tropMdrJson.toString());
        
        File fwissrJson = tmpConfDir.newFile("fwissr.json");
        createTmpConfFile(fwissrJson, fwissrConf);
    }
    
    public static void createTmpConfFile(File file, String conf) throws UnsupportedEncodingException, IOException {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(conf.getBytes("UTF-8"));
        fos.close();
    }
}
