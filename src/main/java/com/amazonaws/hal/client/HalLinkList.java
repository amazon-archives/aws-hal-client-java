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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


class HalLinkList<T>
        implements List<T> {

    //-------------------------------------------------------------
    // Variables - Private
    //-------------------------------------------------------------

    private List<T> inner;


    //-------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------

    HalLinkList(HalResource halResource, String relation, Class<T> type, HalClient halClient) {
        List<T> resources = new ArrayList<>();
        HalLink halLink;
        int index = 0;

        while ((halLink = halResource.getLink(indexedRelation(relation, index++))) != null) {
            resources.add(halClient.getResource(halResource, type, halLink.getHref(), true));
        }

        // Handle case when there was a single item and therefore was not mapped to an array
        if (resources.size() == 0 && (halLink = halResource.getLink(relation)) != null) {
            resources.add(halClient.getResource(halResource, type, halLink.getHref(), true));
        }

        this.inner = Collections.unmodifiableList(resources);
    }


    //-------------------------------------------------------------
    // Implementation - Collection
    //-------------------------------------------------------------

    @Override
    public int size() {
        return inner.size();
    }


    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }


    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }


    @Override
    public Object[] toArray() {
        return inner.toArray();
    }


    @Override
    public <T> T[] toArray(T[] a) {
        //noinspection SuspiciousToArrayCall
        return inner.toArray(a);
    }


    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean containsAll(Collection <?> c) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean retainAll(Collection<?> c) {
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
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(inner).iterator();
    }


    //-------------------------------------------------------------
    // Implementation - List
    //-------------------------------------------------------------

    @Override
    public T get(int index) {
        return inner.get(index);
    }


    @Override
    public ListIterator<T> listIterator() {
        return inner.listIterator();
    }


    @Override
    public ListIterator<T> listIterator(int index) {
        return inner.listIterator(index);
    }


    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return inner.subList(fromIndex, toIndex);
    }


    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }


    @Override
    public T set(int index, T element) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void add(int index, T element) {
        throw new UnsupportedOperationException();
    }


    @Override
    public T remove(int index) {
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
    // Methods - Private
    //-------------------------------------------------------------

    private String indexedRelation(String relation, int index) {
        return relation + "_" + index;
    }
}
