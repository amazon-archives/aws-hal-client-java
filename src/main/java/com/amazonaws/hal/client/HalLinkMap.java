/*
 * Copyright 2010-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.amazonaws.hal.Link.KeyField;


class HalLinkMap<T>
        implements Map<String, T> {

    //-------------------------------------------------------------
    // Variables - Private
    //-------------------------------------------------------------

    private Map<String, T> inner;
    private static Log log = LogFactory.getLog(HalLinkMap.class);


    //-------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------

    HalLinkMap(HalResource halResource, String relation, KeyField keyField, Class<T> type, HalClient halClient) {
        Map<String, T> resources = new HashMap<>();
        HalLink halLink;
        int index = 0;

        String indexedRelation = indexedRelation(relation, index);
        while ((halLink = halResource.getLink(indexedRelation)) != null) {
            String key = keyField == KeyField.Title ? halLink.getTitle() : halLink.getName();

            if (key == null) {
                log.warn("No key value for mapped link.  Using '" + indexedRelation + "' instead");

                resources.put(indexedRelation, halClient.getResource(halResource, type, halLink.getHref(), true));
            } else {
                resources.put(key, halClient.getResource(halResource, type, halLink.getHref(), true));
            }

            indexedRelation = indexedRelation(relation, index++);
        }

        // Handle case when there was a single item and therefore was not mapped to an array
        if (resources.size() == 0 && (halLink = halResource.getLink(relation)) != null) {
            String key = keyField == KeyField.Title ? halLink.getTitle() : halLink.getName();

            if (key == null) {
                log.warn("No key value for mapped link.  Using '" + relation + "' instead");

                resources.put(relation, halClient.getResource(halResource, type, halLink.getHref(), true));
            } else {
                resources.put(key, halClient.getResource(halResource, type, halLink.getHref(), true));
            }
        }

        inner = Collections.unmodifiableMap(resources);
    }


    //-------------------------------------------------------------
    // Implementation - Map
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
    public boolean containsKey(Object key) {
        return inner.containsKey(key);
    }


    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }


    @Override
    public T get(Object key) {
        return inner.get(key);
    }


    @Override
    public T put(String key, T value) {
        throw new UnsupportedOperationException();
    }


    @Override
    public T remove(Object key) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void putAll(Map<? extends String, ? extends T> m) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }


    @Override
    public Set<String> keySet() {
        return inner.keySet();
    }


    @Override
    public Collection<T> values() {
        return inner.values();
    }


    @Override
    public Set<Entry<String, T>> entrySet() {
        return inner.entrySet();
    }


    //-------------------------------------------------------------
    // Methods - Private
    //-------------------------------------------------------------

    private String indexedRelation(String relation, int index) {
        return relation + "_" + index;
    }
}
