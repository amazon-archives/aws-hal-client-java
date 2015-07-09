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

package com.amazonaws.hal;


import java.util.Set;


/**
 * An optional interface that a HAL resource interface can extend to
 * expose additional details about the resource.
 */
public interface ResourceInfo {

    /**
     * Test to see if the current resource contains a link with the
     * specified relation.
     *
     * @param relation the name of the relation to test for
     * @return True if the link is present, false otherwise
     */
    boolean _isLinkAvailable(String relation);


    /**
     * A HAL resource may have one or more links.  This method returns
     * a set with the names of the link relations.
     *
     * @return A set of relation names for this resource.
     */
    Set<String> _getAvailableLinks();


    /**
     * Properties of a HAL resource will normally be accessed by
     * getter methods declared in an resource interface.  This method
     * can be used to get a property value that isn't exposed for
     * some reason.
     *
     * @param propertyName the name of the property to get
     * @return the value of the property, or null if it doesn't exist
     */
    Object _getProperty(String propertyName);


    /**
     * A HAL resource has at least a "self" link.  This method returns
     * the href associated with this relation.
     *
     * @return the href value of the "self" link.
     */
    String _getSelfHref();


    /**
     * Get the href associated to the link with the specified relation.
     *
     * @param relation the name of the relation
     * @return the href value of the specified relation
     */
    String _getLinkHref(String relation);
}
