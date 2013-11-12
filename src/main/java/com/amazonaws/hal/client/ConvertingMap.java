/*
 * Copyright 2010-2013 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amazonaws.hal.client;


import java.lang.reflect.Type;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.amazonaws.hal.client.ConversionUtil.convert;


public class ConvertingMap
        implements Map {

    //-------------------------------------------------------------
    // Variables - Private
    //-------------------------------------------------------------

    private Type type;
    private Map backingMap;


    //-------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------

    public ConvertingMap(Type type, Map backingMap) {
        this.type = type;
        this.backingMap = backingMap;
    }


    //-------------------------------------------------------------
    // Implementation - Map
    //-------------------------------------------------------------

    @Override
    public int size() {
        return backingMap.size();
    }


    @Override
    public boolean isEmpty() {
        return backingMap.isEmpty();
    }


    @Override
    public boolean containsKey(Object key) {
        return backingMap.containsKey(key);
    }


    @Override
    public Object get(Object key) {
        return convert(type, backingMap.get(key));
    }


    @Override
    public Set keySet() {
        return backingMap.keySet();
    }


    @Override
    public Collection values() {
        //noinspection unchecked
        return new ConvertingList(type, new ArrayList(backingMap.values()));
    }


    @Override
    public Set<Entry> entrySet() {
        return new EntrySet();
    }


    @Override
    public Object put(Object key, Object value) {
        throw new UnsupportedOperationException();
    }


    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void putAll(Map m) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }


    //-------------------------------------------------------------
    // Inner Classes
    //-------------------------------------------------------------

    private final class EntrySet extends AbstractSet<Entry> {
        public Iterator<Entry> iterator() {
            return new ConvertingEntryIterator();
        }


        public boolean contains(Object o) {
            throw new UnsupportedOperationException();
        }


        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }


        public int size() {
            return backingMap.size();
        }


        public void clear() {
            throw new UnsupportedOperationException();
        }
    }


    private final class ConvertingEntryIterator
            implements Iterator<Entry> {
        @SuppressWarnings("unchecked")
        Iterator<Entry> backingIterator = backingMap.entrySet().iterator();


        @Override
        public boolean hasNext() {
            return backingIterator.hasNext();
        }


        @Override
        public Entry next() {
            Entry entry = backingIterator.next();
            return new ConvertingEntry(entry.getKey(), entry.getValue());
        }


        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }


    private final class ConvertingEntry
            implements Entry {
        private Object key;
        private Object value;


        private ConvertingEntry(Object key, Object value) {
            this.key = key;
            this.value = value;
        }


        @Override
        public Object getKey() {
            return key;
        }


        @Override
        public Object getValue() {
            return convert(type, value);
        }


        @Override
        public Object setValue(Object value) {
            throw new UnsupportedOperationException();
        }
    }

}
