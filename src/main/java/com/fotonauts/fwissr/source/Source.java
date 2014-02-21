package com.fotonauts.fwissr.source;

import com.fotonauts.fwissr.SmarterMap;



public abstract class Source {

    protected SmarterMap options;
    protected SmarterMap conf;
    
    public Source(SmarterMap options) {
        this.options = options;
    }

    public SmarterMap getOptions() {
        return options;
    }

    abstract public SmarterMap fetchConf();
    
    public static Source fromSettings(SmarterMap settings) {
        if(settings.containsKey("filepath")) {
            return FileSource.fromSettings(settings);
        }
        throw new RuntimeException("Unexpected source settings kind: " + settings.dump());
    }

    public synchronized SmarterMap getConf() {
        if(conf == null || canRefresh())
            conf = fetchConf();
        return conf;
    }

    private boolean canRefresh() {
        return options.get("refresh") == Boolean.TRUE;
    }

    public synchronized void reset() {
        conf = null;
    }
    
    
}
