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


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import static com.amazonaws.hal.client.ConversionUtil.convert;
import static com.amazonaws.hal.client.ConversionUtil.getPropertyName;


class MapBackedInvocationHandler
        implements InvocationHandler {

    //-------------------------------------------------------------
    // Variables - Private
    //-------------------------------------------------------------

    private Map map;


    //-------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------

    MapBackedInvocationHandler(Map map) {
        this.map = map;
    }


    //-------------------------------------------------------------
    // Implementation - InvocationHandler
    //-------------------------------------------------------------

    /**
     */
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        if (method.getName().startsWith("get")) {
            return convert(method.getGenericReturnType(), map.get(getPropertyName(method.getName())));
        }

        throw new UnsupportedOperationException("Don't know how to handle '" + method.getName() + "'");
    }
}
