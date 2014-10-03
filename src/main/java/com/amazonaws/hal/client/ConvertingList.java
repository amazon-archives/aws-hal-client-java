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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static com.amazonaws.hal.client.ConversionUtil.convert;


public class ConvertingList
        implements List {

    //-------------------------------------------------------------
    // Variables - Private
    //-------------------------------------------------------------

    private Type type;
    private List backingList;


    //-------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------

    public ConvertingList(Type type, List backingList) {
        this.type = type;
        this.backingList = backingList;
    }


    //-------------------------------------------------------------
    // Implementation - Collection
    //-------------------------------------------------------------

    @Override
    public int size() {
        return backingList.size();
    }


    @Override
    public boolean isEmpty() {
        return backingList.isEmpty();
    }


    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }


    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }


    @Override
    public Object[] toArray(Object[] a) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean add(Object o) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean containsAll(Collection c) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean addAll(Collection c) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean removeAll(Collection c) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean retainAll(Collection c) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }


    //-------------------------------------------------------------
    // Implementation - Iterable
    //-------------------------------------------------------------

    @Override
    public Iterator iterator() {
        return new ConvertingIterator();
    }


    //-------------------------------------------------------------
    // Implementation - List
    //-------------------------------------------------------------

    @Override
    public Object get(int index) {
        Object value = backingList.get(index);

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
                backingList.set(index, value);
            }
        } else {
            if (!(value instanceof ConvertingMap) && !(value instanceof ConvertingList)) {
                value = convert(type, value);

                if (value instanceof ConvertingMap || value instanceof ConvertingList) {
                    //noinspection unchecked
                    backingList.set(index, value);
                }
            }
        }

        return value;
    }


    @Override
    public List subList(int fromIndex, int toIndex) {
        return new ConvertingList(type, backingList.subList(fromIndex, toIndex));
    }


    @Override
    public ListIterator listIterator() {
        throw new UnsupportedOperationException();
    }


    @Override
    public ListIterator listIterator(int index) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean addAll(int index, Collection c) {
        throw new UnsupportedOperationException();
    }


    @Override
    public Object set(int index, Object element) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void add(int index, Object element) {
        throw new UnsupportedOperationException();
    }


    @Override
    public Object remove(int index) {
        throw new UnsupportedOperationException();
    }


    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException();
    }


    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException();
    }


    //-------------------------------------------------------------
    // Inner Classes
    //-------------------------------------------------------------

    private final class ConvertingIterator
            implements Iterator {
        Iterator backingIterator = backingList.iterator();


        @Override
        public boolean hasNext() {
            return backingIterator.hasNext();
        }


        @Override
        public Object next() {
            // TODO: Re-store this in the backingIterator, but beware ConcurrentModificationException
            return convert(type, backingIterator.next());
        }


        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
