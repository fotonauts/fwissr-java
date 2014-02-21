package com.fotonauts.fwissr;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class SmarterMap implements Map<String, Serializable>, Serializable {

    private static final long serialVersionUID = 1L;

    protected static ObjectMapper jsonObjectMapper = new ObjectMapper();
    protected static ObjectMapper yamlObjectMapper = new ObjectMapper(new YAMLFactory());

    private AtomicBoolean frozen = new AtomicBoolean();
    private Map<String,Serializable> underlying;
    
    public SmarterMap(Map<String,Serializable> underlying) {
        this.underlying = underlying;
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

    @SuppressWarnings("unchecked")
    public Serializable get(Object key) {
        Serializable r = underlying.get(key);
        if(r instanceof Map<?,?> && !(r instanceof SmarterMap))
            return new SmarterMap((Map<String,Serializable>) r);
        else
            return r;
    }

    public Serializable put(String key, Serializable value) {
        return underlying.put(key, value);
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

    @SuppressWarnings("unchecked")
    public SmarterMap clone() {
        try {
            return new SmarterMap(jsonObjectMapper.readValue(jsonObjectMapper.writeValueAsBytes(underlying), Map.class));
        } catch (IOException e) {
            throw new FwissrRuntimeException("failed to clone map: ", e); 
        }
    }

    public void freeze() {
        if(!frozen.getAndSet(true))
            underlying = Collections.unmodifiableMap(underlying);
    }

    @SuppressWarnings("unchecked")
    public void mergeAll(Map<String,Serializable> other) {
        for(Entry<String, Serializable> e: other.entrySet()) {
            if(containsKey(e.getKey()) && (get(e.getKey()) instanceof Map) && e.getValue() instanceof Map<?,?>) {
                ((SmarterMap) get(e.getKey())).mergeAll((Map<String,Serializable>)e.getValue());
            } else {
                put(e.getKey(), e.getValue());
            }
        }
    }

    public static SmarterMap from(Serializable... args) {
        if(args.length % 2 == 1)
            throw new FwissrRuntimeException("attempts at building a map with an odd number of arguments");
        SmarterMap m = new SmarterMap();
        for(int i = 0 ; i < args.length/2; i++)
            m.put(args[2*i].toString(), args[2*i+1]);
        return m;
    }

    public boolean isFrozen() {
        return frozen.get();
    }
    
    @Override
    public String toString() {
        return dump();
    }
    
    public String dump() {
        ObjectWriter w = jsonObjectMapper.writer().withDefaultPrettyPrinter();
        try {
            return w.writeValueAsString(underlying);
        } catch (IOException e) {
            throw new FwissrRuntimeException("failed to dump content");
        }
    }

    public String toJson() {
        ObjectWriter w = jsonObjectMapper.writer();
        try {
            return w.writeValueAsString(underlying);
        } catch (IOException e) {
            throw new FwissrRuntimeException("failed to dump content");
        }
    }

    public String toYaml() {
        ObjectWriter w = yamlObjectMapper.writer();
        try {
            return w.writeValueAsString(underlying);
        } catch (IOException e) {
            throw new FwissrRuntimeException("failed to dump content");
        }
    }
}
