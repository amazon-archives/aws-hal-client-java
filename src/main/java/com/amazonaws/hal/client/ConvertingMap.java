/*
 * Copyright 2010-2014 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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


    // TODO: test w/ map of Integer...
    @Override
    public Object get(Object key) {
        Object value = backingMap.get(key);

        // When a value is accessed, it's intended type can either be a
        // class or some other type (like a ParameterizedType).
        //
        // If the target type is a class and the value is of that type,
        // we return it.  If the value is not of that type, we convert
        // it and store the converted value (trusting it was converted
        // properly) back to the backing store.
        //
        // If the target type is not a class, it may be ParameterizedType
        // like List<T> or Map<K, V>.  We check if the value is already
        // a converting type and if so, we return it.  If the value is
        // not, we convert it and if it's now a converting type, we store
        // the new value in the backing store.

        if (type instanceof Class) {
            if (!((Class) type).isInstance(value)) {
                value = convert(type, value);

                //noinspection unchecked
                backingMap.put(key, value);
            }
        } else {
            if (!(value instanceof ConvertingMap) && !(value instanceof ConvertingList)) {
                value = convert(type, value);

                if (value instanceof ConvertingMap || value instanceof ConvertingList) {
                    //noinspection unchecked
                    backingMap.put(key, value);
                }
            }
        }

        return value;
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
            if (!(type instanceof Class) || !value.getClass().isAssignableFrom((Class) type)) {
                value = convert(type, value);

                // TODO: Re-store this in the backingIterator, but beware ConcurrentModificationException
//                backingMap.put(key, value);
            }

            return value;
        }


        @Override
        public Object setValue(Object value) {
            throw new UnsupportedOperationException();
        }
    }
}
