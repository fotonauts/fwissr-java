package com.fotonauts.fwissr;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Convenient wrapper around {@link Map}.
 * 
 * <p>Allows to represent with a consistent set of classes the hierarchical configuration
 * fetched from the various sources.
 * 
 * @author kali
 *
 */
public class SmarterMap implements Map<String, Serializable>, Serializable {

    private static final long serialVersionUID = 1L;

    private AtomicBoolean frozen = new AtomicBoolean();
    private Map<String, Serializable> underlying;

    @SuppressWarnings("unchecked")
    static Serializable smartify(Serializable value) {
        if (value instanceof Map<?, ?> && !(value instanceof SmarterMap))
            return new SmarterMap((Map<String, Serializable>) value);
        else if (value instanceof List<?> && !(value instanceof SmarterList))
            return new SmarterList((List<Serializable>) value);
        else
            return value;
    }

    public SmarterMap(Map<String, Serializable> source) {
        this.underlying = new HashMap<>();
        for (Entry<String, Serializable> entry : source.entrySet())
            this.underlying.put(entry.getKey(), smartify(entry.getValue()));
    }

    public SmarterMap() {
        this.underlying = new HashMap<>();
    }

    public int size() {
        return underlying.size();
    }

    public boolean isEmpty() {
        return underlying.isEmpty();
    }

    public boolean containsKey(Object key) {
        return underlying.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return underlying.containsValue(value);
    }

    public Serializable get(Object key) {
        return underlying.get(key);
    }

    public Serializable put(String key, Serializable value) {
        return underlying.put(key, smartify(value));
    }

    public Serializable remove(Object key) {
        return underlying.remove(key);
    }

    public void putAll(Map<? extends String, ? extends Serializable> m) {
        underlying.putAll(m);
    }

    public void clear() {
        underlying.clear();
    }

    public Set<String> keySet() {
        return underlying.keySet();
    }

    public Collection<Serializable> values() {
        return underlying.values();
    }

    public Set<java.util.Map.Entry<String, Serializable>> entrySet() {
        return underlying.entrySet();
    }

    public boolean equals(Object o) {
        return underlying.equals(o);
    }

    public int hashCode() {
        return underlying.hashCode();
    }

    /**
     * Deep clone.
     */
    @SuppressWarnings("unchecked")
    public SmarterMap clone() {
        try {
            return new SmarterMap(Fwissr.jsonObjectMapper.readValue(Fwissr.jsonObjectMapper.writeValueAsBytes(underlying),
                    Map.class));
        } catch (IOException e) {
            throw new FwissrRuntimeException("failed to clone map: ", e);
        }
    }

    /**
     * Deep freeze.
     */
    public void freeze() {
        if (!frozen.getAndSet(true)) {
            for (Serializable s : underlying.values()) {
                if (s instanceof SmarterMap)
                    ((SmarterMap) s).freeze();
                else if (s instanceof SmarterList)
                    ((SmarterList) s).freeze();
            }
            underlying = Collections.unmodifiableMap(underlying);
        }
    }

    /**
     * Merge recursively nested objects.
     * 
     * @param other the source to merge data from.
     */
    @SuppressWarnings("unchecked")
    public void mergeAll(Map<String, Serializable> other) {
        for (Entry<String, Serializable> e : other.entrySet()) {
            if (containsKey(e.getKey()) && (get(e.getKey()) instanceof Map) && e.getValue() instanceof Map<?, ?>) {
                ((SmarterMap) get(e.getKey())).mergeAll((Map<String, Serializable>) e.getValue());
            } else {
                put(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * Convenient helper for pseudp-litterals.
     * 
     * @param args an even number of values, representing map entry by consecutive pairs.  
     * @return a map
     */
    public static SmarterMap m(Serializable... args) {
        if (args.length % 2 == 1)
            throw new FwissrRuntimeException("attempts at building a map with an odd number of arguments");
        SmarterMap m = new SmarterMap();
        for (int i = 0; i < args.length / 2; i++)
            m.put(args[2 * i].toString(), args[2 * i + 1]);
        return m;
    }

    public boolean isFrozen() {
        return frozen.get();
    }

    @Override
    public String toString() {
        return toDebugString();
    }

    public String toDebugString() {
        ObjectWriter w = Fwissr.jsonObjectMapper.writer().withDefaultPrettyPrinter();
        try {
            return w.writeValueAsString(underlying);
        } catch (IOException e) {
            throw new FwissrRuntimeException("failed to dump content");
        }
    }

    public String toJson() {
        ObjectWriter w = Fwissr.jsonObjectMapper.writer();
        try {
            return w.writeValueAsString(underlying);
        } catch (IOException e) {
            throw new FwissrRuntimeException("failed to dump content");
        }
    }

    public String toYaml() {
        ObjectWriter w = Fwissr.yamlObjectMapper.writer();
        try {
            return w.writeValueAsString(underlying);
        } catch (IOException e) {
            throw new FwissrRuntimeException("failed to dump content");
        }
    }
}
