package com.fotonauts.fwissr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.fotonauts.fwissr.source.Source;

public class Registry {

    public static int DEFAULT_REFRESH_PERIOD = 30;
    private long refreshPeriodMS;

    private SmarterMap registry = new SmarterMap();
    private List<Source> sources = new LinkedList<>();
    public Thread refreshThread;

    public Registry(SmarterMap params) {
        refreshPeriodMS = 1000 * (params.containsKey("refresh_period") ? ((Integer) (params.get("refresh_period")))
                : DEFAULT_REFRESH_PERIOD);
    }

    public Registry() {
        new Registry(SmarterMap.m());
    }

    public synchronized void addSource(Source source) {
        sources.add(source);
        if (registry.isFrozen()) {
            reload();
        } else {
            registry.mergeAll(source.getConf());
        }
        ensureRefreshThread();
    }

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

    public long getRefreshPeriodMS() {
        return refreshPeriodMS;
    }

    public int getRefreshPeriod() {
        return (int) (refreshPeriodMS / 1000);
    }

    public synchronized SmarterMap getRegistry() {
        ensureRefreshThread();
        registry.freeze();
        return registry;
    }

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

    public String toDebugString() {
        return getRegistry().toDebugString();
    }

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

    public SmarterMap dump() {
        return getRegistry();
    }
}
