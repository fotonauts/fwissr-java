package com.fotonauts.fwissr.source;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fotonauts.fwissr.Fwissr;
import com.fotonauts.fwissr.FwissrRuntimeException;
import com.fotonauts.fwissr.SmarterMap;

public class FileSource extends Source {

    private static Set<String> TOP_LEVEL_CONF_FILES = Collections.unmodifiableSet(Collections.singleton("fwissr"));

    private String path;

    public FileSource(String filename, SmarterMap options) {
        super(options);
        if (!new File(filename).exists())
            throw new FwissrRuntimeException("File not found: " + filename);
        path = filename;
    }

    public static FileSource fromSettings(SmarterMap settings) {
        SmarterMap options = settings.clone();
        options.remove("filepath");
        options.freeze();
        return fromPath((String) settings.get("filepath"), options);
    }

    // package visibility for tests
    static FileSource fromPath(String filename) {
        return fromPath(filename, new SmarterMap());        
    }
    
    // package visibility for tests
    static FileSource fromPath(String filename, SmarterMap options) {
        if (StringUtils.isBlank(filename))
            throw new FwissrRuntimeException("Unexpected file source path: " + filename);
        return new FileSource(filename, options);
    }

    @Override
    public SmarterMap fetchConf() {
        SmarterMap result = new SmarterMap();

        List<File> files = new LinkedList<>();
        File conf = new File(path);
        if (conf.isDirectory()) {
            for (File f : conf.listFiles()) {
                if (f.getName().endsWith(".json") || f.getName().endsWith(".yaml") || f.getName().endsWith(".yml")) {
                    files.add(f);
                }
            }
        } else {
            files.add(conf);
        }

        for (File f : files) {
            if (f.isFile())
                mergeConfFile(result, f);
        }

        return result;
    }

    private void mergeConfFile(SmarterMap result, File f) {
        SmarterMap conf = Fwissr.parseConfFile(f);
        String confFileName = f.getName().replaceAll("\\.[^.]*", "");
        SmarterMap resultPart = result;
        if (!TOP_LEVEL_CONF_FILES.contains(confFileName) && options.get("top_level") != Boolean.TRUE) {
            for (String keyPart : confFileName.split("\\.")) {
                resultPart.put(keyPart, new SmarterMap());
                resultPart = (SmarterMap) resultPart.get(keyPart);
            }
        }
        resultPart.mergeAll(conf);
    }

    @Override
    public String toString() {
        return "Configuration file source: " + path;
    }

    public String getPath() {
        return path;
    }
}
