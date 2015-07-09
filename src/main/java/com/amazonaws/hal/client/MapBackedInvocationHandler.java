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


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

import static com.amazonaws.hal.client.ConversionUtil.convert;
import static com.amazonaws.hal.client.ConversionUtil.getPropertyName;


class MapBackedInvocationHandler
        implements InvocationHandler {

    //-------------------------------------------------------------
    // Variables - Private
    //-------------------------------------------------------------

    private Type type;
    private Map map;


    //-------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------

    MapBackedInvocationHandler(Type type, Map map) {
        this.type = type;
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
            String propertyName = getPropertyName(method.getName());
            Object property = map.get(propertyName);
            Type returnType = method.getGenericReturnType();

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

            if (returnType instanceof Class) {
                if (!((Class) returnType).isInstance(property)) {
                    property = convert(returnType, property);

                    //noinspection unchecked
                    map.put(propertyName, property);
                }
            } else {
                if (!(property instanceof ConvertingMap) && !(property instanceof ConvertingList)) {
                    property = convert(returnType, property);

                    if (property instanceof ConvertingMap || property instanceof ConvertingList) {
                        //noinspection unchecked
                        map.put(propertyName, property);
                    }
                }
            }

            return property;
        } else if (method.getName().equals("toString")) {
            return "Proxy for type: " + type;
        }
        // TODO: equals() and hashCode()?

        throw new UnsupportedOperationException("Don't know how to handle '" + method.getName() + "'");
    }
}
