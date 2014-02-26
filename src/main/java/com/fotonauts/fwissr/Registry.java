package com.fotonauts.fwissr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.fotonauts.fwissr.source.Source;

/**
 * Aggregated configuration from several sources.   
 * 
 * <p>Allows more flexibility than using {@link Fwissr} (for custom sources, for instances). The price being
 * Source have to be setup programmatically.
 * 
 * Registry will periodically refresh the configuration from sources allowing it.
 * 
 * @author kali
 *
 */
public class Registry {

    public static int DEFAULT_REFRESH_PERIOD = 30;
    private long refreshPeriodMS;

    private SmarterMap registry = new SmarterMap();
    private List<Source> sources = new LinkedList<>();
    public Thread refreshThread;

    /**
     * Creates a registry.
     * 
     * The params can contains a "refresh_period" value (in seconds) to override the default (30 seconds).
     * 
     * @param params can contains a "refresh_period" (in seconds)
     */
    public Registry(SmarterMap params) {
        refreshPeriodMS = 1000 * (params.containsKey("refresh_period") ? ((Integer) (params.get("refresh_period")))
                : DEFAULT_REFRESH_PERIOD);
    }

    /**
     * Creates a registry.
     */
    public Registry() {
        new Registry(SmarterMap.m());
    }

    /**
     * Add a source to the registry.
     * 
     * @param source the source to add
     */
    public synchronized void addSource(Source source) {
        sources.add(source);
        if (registry.isFrozen()) {
            reload();
        } else {
            registry.mergeAll(source.getConf());
        }
        ensureRefreshThread();
    }

    /**
     * Starts the background refreshing thread.
     */
    private synchronized void ensureRefreshThread() {
        if (refreshPeriodMS > 0 && haveAtLeastOneRefreshableSource() && (refreshThread == null || !refreshThread.isAlive())) {
            refreshThread = new Thread() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            sleep(refreshPeriodMS);
                            load();
                        } catch (Throwable t) {
                            System.err.println("in fwissr refresh thread, caught: " + t);
                        }
                    }
                }
            };
            refreshThread.setDaemon(true);
            refreshThread.start();
        }
    }

    private synchronized boolean haveAtLeastOneRefreshableSource() {
        for (Source s : sources)
            if (s.canRefresh())
                return true;
        return false;
    }

    /**
     * Force a registry reload.
     */
    public synchronized void reload() {
        reset();
        load();
    }

    private synchronized void load() {
        registry = new SmarterMap();
        for (Source source : sources)
            registry.mergeAll(source.getConf());

    }

    private synchronized void reset() {
        registry = new SmarterMap();
        for (Source source : sources)
            source.reset();
    }

    /**
     * Access the refresh period in milliseconds.
     * 
     * @return the refresh period in milliseconds. 
     */
    public long getRefreshPeriodMS() {
        return refreshPeriodMS;
    }

    /**
     * Access the refresh period in seconds.
     * 
     * @return the refresh period in seconds. 
     */
    public int getRefreshPeriod() {
        return (int) (refreshPeriodMS / 1000);
    }

    /**
     * Access the loaded configuration as a whole.
     * 
     * @return the configuration content.
     */
    public synchronized SmarterMap getRegistry() {
        ensureRefreshThread();
        registry.freeze();
        return registry;
    }

    /**
     * Lookup a configuration property.
     * 
     * @param key the configuration to read
     * @return the read value
     */
    public Serializable get(String key) {
        String[] keyAsArray = key.split("/");
        if (keyAsArray.length > 0 && keyAsArray[0].equals(""))
            keyAsArray = Arrays.copyOfRange(keyAsArray, 1, keyAsArray.length);
        Serializable current = getRegistry();
        for (String k : keyAsArray) {
            current = ((SmarterMap) current).get(k);
            if (current == null)
                return null;
        }
        return current;
    }

    /**
     * Dump the content to a pretty printed json string. 
     * 
     * @return a pretty content
     */
    public String toDebugString() {
        return getRegistry().toDebugString();
    }

    /**
     * List all keys (and subkeys) from the configuration.
     *  
     * @return a list of keys
     */
    public List<String> getKeys() {
        List<String> result = new LinkedList<>();
        getKeys(result, new ArrayList<String>(), getRegistry());
        return result;

    }

    private void getKeys(List<String> result, List<String> currentPath, SmarterMap node) {
        for (Entry<String, Serializable> e : node.entrySet()) {
            currentPath.add(e.getKey());
            result.add("/" + StringUtils.join(currentPath, "/"));
            if (e.getValue() instanceof SmarterMap)
                getKeys(result, currentPath, (SmarterMap) e.getValue());
            currentPath.remove(currentPath.size() - 1);
        }
    }

    /**
     * @see GetRegistry.
     */
    public SmarterMap dump() {
        return getRegistry();
    }
}
