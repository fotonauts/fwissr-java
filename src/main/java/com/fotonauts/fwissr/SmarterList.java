package com.fotonauts.fwissr;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Convenient wrapper around {@link List}.
 * 
 * <p>Allows to represent with a consistent set of classes the hierarchical configuration
 * fetched from the various sources.
 * 
 * @author kali
 *
 */
public class SmarterList implements List<Serializable>, Serializable {

    private static final long serialVersionUID = 1L;

    private AtomicBoolean frozen = new AtomicBoolean();
    private List<Serializable> underlying;

    public int size() {
        return underlying.size();
    }

    public boolean isEmpty() {
        return underlying.isEmpty();
    }

    public boolean contains(Object o) {
        return underlying.contains(o);
    }

    public Iterator<Serializable> iterator() {
        return underlying.iterator();
    }

    public Object[] toArray() {
        return underlying.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return underlying.toArray(a);
    }

    public boolean add(Serializable e) {
        return underlying.add(SmarterMap.smartify(e));
    }

    public boolean remove(Object o) {
        return underlying.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        return underlying.containsAll(c);
    }

    public boolean addAll(Collection<? extends Serializable> c) {
        return underlying.addAll(new SmarterList(c));
    }

    public boolean addAll(int index, Collection<? extends Serializable> c) {
        return underlying.addAll(index, new SmarterList(c));
    }

    public boolean removeAll(Collection<?> c) {
        return underlying.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return underlying.retainAll(c);
    }

    public void clear() {
        underlying.clear();
    }

    public boolean equals(Object o) {
        return underlying.equals(o);
    }

    public int hashCode() {
        return underlying.hashCode();
    }

    public Serializable set(int index, Serializable element) {
        return underlying.set(index, element);
    }

    public void add(int index, Serializable element) {
        underlying.add(index, element);
    }

    public Serializable remove(int index) {
        return underlying.remove(index);
    }

    public int indexOf(Object o) {
        return underlying.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return underlying.lastIndexOf(o);
    }

    public ListIterator<Serializable> listIterator() {
        return underlying.listIterator();
    }

    public ListIterator<Serializable> listIterator(int index) {
        return underlying.listIterator(index);
    }

    public List<Serializable> subList(int fromIndex, int toIndex) {
        return underlying.subList(fromIndex, toIndex);
    }

    public SmarterList(Collection<? extends Serializable> c) {
        this.underlying = new ArrayList<>();
        for(Serializable s: c)
            this.underlying.add(SmarterMap.smartify(s));
    }

    public SmarterList() {
        this.underlying = new ArrayList<>();
    }

    public Serializable get(int key) {
        return underlying.get(key);
    }

    /**
     * Deep clone.
     */
    @SuppressWarnings("unchecked")
    public SmarterList clone() {
        try {
            return new SmarterList(Fwissr.jsonObjectMapper.readValue(Fwissr.jsonObjectMapper.writeValueAsBytes(underlying), List.class));
        } catch (IOException e) {
            throw new FwissrRuntimeException("failed to clone list: ", e);
        }
    }

    /**
     * Deep freeze.
     */
    public void freeze() {
        if(!frozen.getAndSet(true)) {
            for(Serializable s: underlying) {
                if(s instanceof SmarterMap)
                    ((SmarterMap) s).freeze();
                else if(s instanceof SmarterList)
                    ((SmarterList) s).freeze();
            }
            underlying = Collections.unmodifiableList(underlying);
        }
    }

    /**
     * Convenient constructor for pseudo-litteral.
     */
    public static SmarterList l(Serializable... args) {
        SmarterList m = new SmarterList();
        for(Serializable v: args)
            m.add(SmarterMap.smartify(v));
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
        return toJson();
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
