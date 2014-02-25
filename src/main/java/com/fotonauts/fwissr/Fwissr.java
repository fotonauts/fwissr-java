package com.fotonauts.fwissr;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fotonauts.fwissr.source.Source;

public class Fwissr {

    private final static String MAIN_CONF_FILE = "fwissr.json";
    protected static ObjectMapper jsonObjectMapper = new ObjectMapper();
    protected static ObjectMapper yamlObjectMapper = new ObjectMapper(new YAMLFactory());

    private File mainConfPath;

    public Fwissr(String mainConfPath) {
        this.mainConfPath = new File(mainConfPath);
    }

    @SuppressWarnings("unchecked")
    public static SmarterMap parseConfFile(File confFilePath) {
        try {
            String extension = confFilePath.getName().replaceAll(".*\\.", "");
            switch (extension) {
            case "json":
                return new SmarterMap(jsonObjectMapper.readValue(confFilePath, Map.class));
            case "yaml":
            case "yml":
                return new SmarterMap(yamlObjectMapper.readValue(confFilePath, Map.class));
            }
            throw new FwissrRuntimeException("Unsupported conf file kind: " + confFilePath);
        } catch (IOException e) {
            throw new FwissrRuntimeException("Can not read parse " + confFilePath, e);
        }
    }

    public Serializable get(String key) {
        return getGlobalRegistry().get(key);
    }

    private SmarterMap mainConf;

    private File getMainConfFile() {
        return new File(mainConfPath.toString() + File.separator + MAIN_CONF_FILE);
    }

    private synchronized SmarterMap getMainConf() {
        if (mainConf == null) {
            mainConf = new SmarterMap();
            if (getMainConfFile().exists()) {
                mainConf.mergeAll(Fwissr.parseConfFile(getMainConfFile()));
            }
        }
        return mainConf;
    }

    private Registry globalRegistry;

    @SuppressWarnings("unchecked")
    public synchronized Registry getGlobalRegistry() {
        if (globalRegistry == null) {
            SmarterMap registryParams = new SmarterMap();
            Serializable period = getMainConf().get("fwissr_refresh_period");
            if(period != null)
                registryParams.put("refresh_period", period);
            globalRegistry = new Registry(registryParams);
            if(mainConfPath.exists())
                globalRegistry.addSource(Source.fromSettings(SmarterMap.m("filepath", mainConfPath.toString())));
            if(mainConf.containsKey("fwissr_sources")) {
                for(Serializable s: (List<Serializable>) mainConf.get("fwissr_sources")) {
                    globalRegistry.addSource(Source.fromSettings((SmarterMap) s));
                }
            }
        }
        return globalRegistry;
    }

    public String dump() {
        return getGlobalRegistry().toDebugString();
    }

}
