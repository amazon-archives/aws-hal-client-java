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

package com.amazonaws.hal;


import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;


public class ImmediatelyExpiringCache
        implements Map<String, Object> {

    //-------------------------------------------------------------
    // Variables - Private - Static
    //-------------------------------------------------------------

    private static ImmediatelyExpiringCache instance = new ImmediatelyExpiringCache();


    //-------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------

    private ImmediatelyExpiringCache() {
    }


    //-------------------------------------------------------------
    // Methods - Public - Static
    //-------------------------------------------------------------

    public static ImmediatelyExpiringCache getInstance() {
        return instance;
    }


    //-------------------------------------------------------------
    // Implementation - Map
    //-------------------------------------------------------------

    @Override
    public int size() {
        return 0;
    }


    @Override
    public boolean isEmpty() {
        return true;
    }


    @Override
    public boolean containsKey(Object key) {
        return false;
    }


    @Override
    public boolean containsValue(Object value) {
        return false;
    }


    @Override
    public Object get(Object key) {
        return null;
    }


    @Override
    public Object put(String key, Object value) {
        return value;
    }


    @Override
    public Object remove(Object key) {
        return null;
    }


    @Override
    public void putAll(Map<? extends String, ?> m) {
    }


    @Override
    public void clear() {
    }


    @Override
    public Set<String> keySet() {
        return Collections.emptySet();
    }


    @Override
    public Collection<Object> values() {
        return Collections.emptySet();
    }


    @Override
    public Set<Entry<String, Object>> entrySet() {
        return Collections.emptySet();
    }
}
