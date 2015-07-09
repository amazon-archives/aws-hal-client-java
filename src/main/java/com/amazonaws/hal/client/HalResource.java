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


import com.amazonaws.hal.ResourceInfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


class HalResource
        implements ResourceInfo {

    //-------------------------------------------------------------
    // Variables - Private
    //-------------------------------------------------------------

    private Map<String, Object> properties = new HashMap<>();
    private Map<String, HalLink> links = Collections.emptyMap();            // Map of relation to HalLink
    private Map<String, HalResource> embedded = Collections.emptyMap();     // Map of href to HalResource


    //-------------------------------------------------------------
    // Implementation - ResourceInfo
    //-------------------------------------------------------------

    @Override
    public boolean _isLinkAvailable(String relation) {
        return links.containsKey(relation);
    }


    @Override
    public Set<String> _getAvailableLinks() {
        return links.keySet();
    }


    @Override
    public Object _getProperty(String propertyName) {
        return properties.get(propertyName);
    }


    @Override
    public String _getSelfHref() {
        return _getLinkHref("self");
    }


    @Override
    public String _getLinkHref(String relation) {
        HalLink link = getLink(relation);

        if (link == null) {
            return null;
        }

        return link.getHref();
    }


    //-------------------------------------------------------------
    // Methods - Public - Canonical
    //-------------------------------------------------------------

    @Override
    public String toString() {
        HalLink selfLink = getLink("self");

        if (selfLink == null) {
            return "<undefined>";
        }

        return selfLink.getHref();
    }


    //-------------------------------------------------------------
    // Methods - Package
    //-------------------------------------------------------------

    Object getProperty(String key) {
        return properties.get(key);
    }


    void addProperty(String key, Object value) {
        properties.put(key, value);
    }


    HalLink getLink(String relation) {
        return links.get(relation);
    }


    void setLinks(Map<String, HalLink> links) {
        this.links = links;
    }


    Map<String, HalResource> getEmbedded() {
        return embedded;
    }


    void setEmbedded(Map<String, HalResource> embedded) {
        this.embedded = embedded;
    }


    boolean isDefined() {
        return getLink("self") != null;
    }
}
