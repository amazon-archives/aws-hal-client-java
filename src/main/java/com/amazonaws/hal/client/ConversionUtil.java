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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.bind.DatatypeConverter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;


class ConversionUtil {

    //-------------------------------------------------------------
    // Variables - Private
    //-------------------------------------------------------------

    private static final Log log = LogFactory.getLog(ConversionUtil.class);


    //-------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------

    private ConversionUtil() {
    }


    //-------------------------------------------------------------
    // Methods - Package
    //-------------------------------------------------------------

    static Type getCollectionType(Type type, int index, Class defaultClass) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            return parameterizedType.getActualTypeArguments()[index];
        } else {
            return defaultClass;
        }
    }


    static String getPropertyName(String original) {
        StringBuilder propertyName = new StringBuilder(original.substring("get".length()));

        propertyName.setCharAt(0, Character.toLowerCase(propertyName.charAt(0)));

        return propertyName.toString();
    }


    static Object convert(Type type, Object value) {
        if (value == null) {
            return convertFromNull(type);
        } else if (value instanceof Number) {
            return convertFromNumber((Class) type, (Number) value);
        } else if (value instanceof Boolean) {
            return value;
        } else if (value instanceof String) {
            return convertFromString((Class) type, (String) value);
        } else if (value instanceof Map) {
            return convertFromMap(type, (Map) value);
        } else if (value instanceof List) {
            return convertFromList(type, (List) value);
        } else {
            throw new RuntimeException("Not sure how to convert " + value + " to a " + type);
        }
    }


    //-------------------------------------------------------------
    // Methods - Private
    //-------------------------------------------------------------

    private static Object convertFromNull(Type type) {
        if (!(type instanceof Class)) {
            return null;
        }

        Class<?> clazz = (Class) type;

        if (!clazz.isPrimitive()) {
            return null;
        }

        if (int.class.isAssignableFrom(clazz)) {
            return 0;
        } else if (long.class.isAssignableFrom(clazz)) {
            return 0L;
        } else if (short.class.isAssignableFrom(clazz)) {
            return 0;
        } else if (double.class.isAssignableFrom(clazz)) {
            return 0.0;
        } else if (float.class.isAssignableFrom(clazz)) {
            return 0.0F;
        } else if (boolean.class.isAssignableFrom(clazz)) {
            return Boolean.FALSE;
        } else if (char.class.isAssignableFrom(clazz)) {
            return 0;
        } else if (byte.class.isAssignableFrom(clazz)) {
            return 0;
        } else {
            throw new RuntimeException("Unexpected primitive type: " + clazz.getSimpleName());
        }
    }

    
    private static Object convertFromNumber(Class<?> clazz, Number value) {
        if (String.class.isAssignableFrom(clazz)) {
            return value.toString();
        } else if (int.class.isAssignableFrom(clazz) || Integer.class.isAssignableFrom(clazz)) {
            return value.intValue();
        } else if (long.class.isAssignableFrom(clazz) || Long.class.isAssignableFrom(clazz)) {
            return value.longValue();
        } else if (short.class.isAssignableFrom(clazz) || Short.class.isAssignableFrom(clazz)) {
            return value.shortValue();
        } else if (double.class.isAssignableFrom(clazz) || Double.class.isAssignableFrom(clazz)) {
            return value.doubleValue();
        } else if (float.class.isAssignableFrom(clazz) || Float.class.isAssignableFrom(clazz)) {
            return value.floatValue();
        } else if (boolean.class.isAssignableFrom(clazz) || Boolean.class.isAssignableFrom(clazz)) {
            return Boolean.valueOf(value.toString());
        } else if (char.class.isAssignableFrom(clazz) || Character.class.isAssignableFrom(clazz)) {
            if (value.longValue() <= 255) {
                return (char)value.longValue();
            } else {
                throw new RuntimeException("Not sure how to convert " + value + " to a " + clazz.getSimpleName());
            }
        } else if (byte.class.isAssignableFrom(clazz) || Byte.class.isAssignableFrom(clazz)) {
            return value.byteValue();
        } else if (BigDecimal.class.isAssignableFrom(clazz)) {
            return new BigDecimal(value.toString());
        } else if (BigInteger.class.isAssignableFrom(clazz)) {
            // Necessary because BigInteger(long) is a private method and we need to convert the Number to a long to
            // prevent the constructor from throwing a NumberFormatException Example: BigInteger(1.2)
            return new BigInteger(String.valueOf(value.longValue()));
        } else if (Date.class.isAssignableFrom(clazz)) {
            return new Date(value.longValue());
        } else if (clazz.isEnum()) {
            try {
                //noinspection unchecked
                return Enum.valueOf((Class<Enum>) clazz, value.toString());
            } catch (IllegalArgumentException e) {
                log.error(String.format("'%s' is not a recognized enum value for %s.  Returning default of %s instead.",
                                        value, clazz.getName(), clazz.getEnumConstants()[0]));

                return clazz.getEnumConstants()[0];
            }
        } else {
            throw new RuntimeException("Not sure how to convert " + value + " to a " + clazz.getSimpleName());
        }
    }
    
    
    private static Object convertFromString(Class<?> clazz, String value) {
        if (String.class.isAssignableFrom(clazz)) {
            return value;
        } else if (int.class.isAssignableFrom(clazz) || Integer.class.isAssignableFrom(clazz)) {
            return new Integer(value);
        } else if (long.class.isAssignableFrom(clazz) || Long.class.isAssignableFrom(clazz)) {
            return new Long(value);
        } else if (short.class.isAssignableFrom(clazz) || Short.class.isAssignableFrom(clazz)) {
            return new Short(value);
        } else if (double.class.isAssignableFrom(clazz) || Double.class.isAssignableFrom(clazz)) {
            return new Double(value);
        } else if (float.class.isAssignableFrom(clazz) || Float.class.isAssignableFrom(clazz)) {
            return new Float(value);
        } else if (boolean.class.isAssignableFrom(clazz) || Boolean.class.isAssignableFrom(clazz)) {
            return Boolean.valueOf(value);
        } else if (char.class.isAssignableFrom(clazz) || Character.class.isAssignableFrom(clazz)) {
            return value.charAt(0);
        } else if (byte.class.isAssignableFrom(clazz) || Byte.class.isAssignableFrom(clazz)) {
            return new Byte(value);
        } else if (BigDecimal.class.isAssignableFrom(clazz)) {
            return new BigDecimal(value);
        } else if (BigInteger.class.isAssignableFrom(clazz)) {
            return new BigInteger(value);
        } else if (Date.class.isAssignableFrom(clazz)) {
            try {
                return new Date(Long.parseLong(value));
            } catch (NumberFormatException e) {
                try {
                    return DatatypeConverter.parseDateTime(value).getTime();
                } catch (IllegalArgumentException e1) {
                    throw new RuntimeException("Unexpected date format: " + value + ".  We currently parse xsd:datetime and milliseconds.");
                }
            }
        } else if (clazz.isEnum()) {
            try {
                //noinspection unchecked
                return Enum.valueOf((Class<Enum>) clazz, value);
            } catch (IllegalArgumentException e) {
                log.error(String.format("'%s' is not a recognized enum value for %s.  Returning default of %s instead.",
                                        value, clazz.getName(), clazz.getEnumConstants()[0]));

                return clazz.getEnumConstants()[0];
            }
        } else {
            throw new RuntimeException("Not sure how to convert " + value + " to a " + clazz.getSimpleName());
        }
    }


    private static Object convertFromMap(Type type, Map value) {
        if (type instanceof Class && !Map.class.isAssignableFrom((Class) type)) {
            Class typeClass = (Class) type;

            return Proxy.newProxyInstance(typeClass.getClassLoader(),
                                          new Class<?>[] { typeClass },
                                          new MapBackedInvocationHandler(type, value));
        } else {
            return new ConvertingMap(getCollectionType(type, 1, Object.class), value);
        }
    }


    private static Object convertFromList(Type type, List value) {
        return new ConvertingList(getCollectionType(type, 0, Object.class), value);
    }
}
