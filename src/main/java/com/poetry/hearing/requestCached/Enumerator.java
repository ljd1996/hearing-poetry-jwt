package com.poetry.hearing.requestCached;

import java.util.*;

public class Enumerator<T> implements Enumeration<T> {
    private Iterator<T> iterator;

    public Enumerator(Collection<T> collection) {
        this(collection.iterator());
    }

    public Enumerator(Collection<T> collection, boolean clone) {
        this(collection.iterator(), clone);
    }

    public Enumerator(Iterator<T> iterator) {
        this.iterator = null;
        this.iterator = iterator;
    }

    public Enumerator(Iterator<T> iterator, boolean clone) {
        this.iterator = null;
        if (!clone) {
            this.iterator = iterator;
        } else {
            ArrayList list = new ArrayList();

            while(iterator.hasNext()) {
                list.add(iterator.next());
            }

            this.iterator = list.iterator();
        }

    }

    public Enumerator(Map<?, T> map) {
        this(map.values().iterator());
    }

    public Enumerator(Map<?, T> map, boolean clone) {
        this(map.values().iterator(), clone);
    }

    public boolean hasMoreElements() {
        return this.iterator.hasNext();
    }

    public T nextElement() throws NoSuchElementException {
        return this.iterator.next();
    }
}