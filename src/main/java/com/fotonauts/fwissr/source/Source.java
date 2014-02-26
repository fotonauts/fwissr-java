package com.fotonauts.fwissr.source;

import com.fotonauts.fwissr.SmarterMap;

/**
 * Represents a configuration data source.
 * 
 * Can be sub-classed to implement new source of configuration data.
 * 
 * @author kali
 *
 */
public abstract class Source {

    protected SmarterMap options;
    protected SmarterMap conf;

    /**
     * Constructor.
     * 
     * <p>valid "options": 
     * <dt>top_level<dd>If it is set to true (as a {@link Boolean}, not a String),
     *                  the source will contribute its content at top level.
     * <dt>refresh<dd>also a Boolean, allow the source to regularly check its content
     *                for a change
     * </p>
     * 
     * @param options
     */
    public Source(SmarterMap options) {
        this.options = options;
    }

    /**
     * Access the source own configuration.
     * 
     * @return the source meta configuration
     */
    public SmarterMap getOptions() {
        return options;
    }

    /**
     * Fetch the configuration from the source.
     * 
     * @return the contributed configuration data
     */
    abstract public SmarterMap fetchConf();

    /**
     * Will instantiate a file or mongodb source from declared settings.
     * 
     * <p>There is no provision for extension (declarative creation of other source) here yet.
     * 
     * @param settings
     * @return
     */
    public static Source fromSettings(SmarterMap settings) {
        if(settings.containsKey("filepath")) {
            return FileSource.fromSettings(settings);
        } else if(settings.containsKey("mongodb")) {
            return MongodbSource.fromSettings(settings);
        }
        throw new RuntimeException("Unexpected source settings kind: " + settings.toDebugString());
    }

    /**
     * Access the configuration content found through the source.
     * 
     * @return the source contributed configuration.
     */
    public synchronized SmarterMap getConf() {
        if(conf == null || canRefresh()) {
            conf = fetchConf();
        }
        return conf;
    }

    /**
     * Checks if the source setup allows refreshing.
     */
    public boolean canRefresh() {
        return options.get("refresh") == Boolean.TRUE;
    }

    /**
     * Force a source reload.
     */
    public synchronized void reset() {
        conf = null;
    }

    /**
     * Merges a configuration into one.
     * 
     * <p>Think {@link java.util.Map#putAll(java.util.Map)}, but merging nested sub objects instead of overwriting.
     * 
     * @param result the destination configuration (will be augmented)
     * @param conf the source configuration (not altered)
     * @param path where to insert the root element of conf in result 
     * @param topLevel whether path must be ignored and everyting inserted at topLevel
     */
    protected void mergeConf(SmarterMap result, SmarterMap conf, String[] path, boolean topLevel) {
        SmarterMap resultPart = result;
        if (!topLevel && options.get("top_level") != Boolean.TRUE) {
            for (String keyPart: path) {
                resultPart.put(keyPart, new SmarterMap());
                resultPart = (SmarterMap) resultPart.get(keyPart);
            }
        }
        resultPart.mergeAll(conf);
    }

}
