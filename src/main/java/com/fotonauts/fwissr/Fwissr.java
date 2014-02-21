package com.fotonauts.fwissr;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

import com.fotonauts.fwissr.source.Source;

public class Fwissr {

    private final static String MAIN_CONF_FILE = "fwissr.json"; 
    protected static ObjectMapper jacksonObjectMapper = new ObjectMapper();

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
                return new SmarterMap(jacksonObjectMapper.readValue(confFilePath, Map.class));
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

    // FIXME ~user conf
    private synchronized Registry getGlobalRegistry() {
        if (globalRegistry == null) {
            globalRegistry = new Registry(SmarterMap.from("refresh_period", getMainConf().get("fwissr_refresh_period")));
            if(mainConfPath.exists())
                globalRegistry.addSource(Source.fromSettings(SmarterMap.from("filepath", mainConfPath.toString())));
            if(mainConf.containsKey("fwissr_sources")) {
                for(Serializable s: (List<Serializable>) mainConf.get("fwissr_sources")) {
                    globalRegistry.addSource(Source.fromSettings(new SmarterMap((Map) s)));
                }
            }
        }
        return globalRegistry;
    }

    public String dump() {
        return getGlobalRegistry().dump();
    }

}
