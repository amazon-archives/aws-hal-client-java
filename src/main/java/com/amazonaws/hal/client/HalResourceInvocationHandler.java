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


import com.amazonaws.hal.Link;
import com.amazonaws.hal.ResourceInfo;
import com.amazonaws.hal.UriValue;
import com.amazonaws.hal.UriVariable;

import com.damnhandy.uri.template.MalformedUriTemplateException;
import com.damnhandy.uri.template.UriTemplate;
import com.damnhandy.uri.template.VariableExpansionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static com.amazonaws.hal.client.ConversionUtil.convert;
import static com.amazonaws.hal.client.ConversionUtil.getCollectionType;
import static com.amazonaws.hal.client.ConversionUtil.getPropertyName;


class HalResourceInvocationHandler
        implements InvocationHandler {

    //-------------------------------------------------------------
    // Variables - Private
    //-------------------------------------------------------------

    private HalResource halResource;
    private String resourcePath;
    private HalClient halClient;

    private static Log log = LogFactory.getLog(HalResourceInvocationHandler.class);

    private static final Object[] EMPTY_ARGS = new Object[0];


    //-------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------

    HalResourceInvocationHandler(HalResource halResource, String resourcePath, HalClient halClient) {
        this.halResource = halResource;
        this.resourcePath = resourcePath;
        this.halClient = halClient;
    }


    //-------------------------------------------------------------
    // Implementation - InvocationHandler
    //-------------------------------------------------------------

    /**
     */
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        if (halResource == null || !halResource.isDefined()) {
            halResource = halClient.getHalResource(resourcePath);
        }

        try {
            Method resourceInfoMethod = ResourceInfo.class.getMethod(method.getName(), method.getParameterTypes());

            return resourceInfoMethod.invoke(halResource, args);
        } catch (NoSuchMethodException ignore) {
            // If the method is not defined in ResourceInfo, we handle it below
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }

        Link link;
        if ((link = method.getAnnotation(Link.class)) != null) {
            switch (link.method()) {
            case GET:
                if (List.class.isAssignableFrom(method.getReturnType())) {
                    //noinspection unchecked
                    return new HalLinkList(halResource, link.relation(),
                                           (Class) getCollectionType(method.getGenericReturnType(), 0, ResourceInfo.class),
                                           halClient);
                } else if (Map.class.isAssignableFrom(method.getReturnType())) {
                    //noinspection unchecked
                    return new HalLinkMap(halResource, link.relation(), link.keyField(),
                                          (Class) getCollectionType(method.getGenericReturnType(), 1, ResourceInfo.class),
                                          halClient);
                } else {
                    return halClient.getResource(halResource, method.getReturnType(),
                                                 getRelationHref(link, args == null ? EMPTY_ARGS : args, method.getParameterAnnotations()), false);
                }

            case POST:
                if (args == null) {
                    throw new IllegalArgumentException("POST operations require a representation argument.");
                }

                return halClient.postResource(method.getReturnType(),
                                              getRelationHref(link, args, method.getParameterAnnotations()), args[0]);

            case PUT:
                if (args == null) {
                    throw new IllegalArgumentException("PUT operations require a representation argument.");
                }

                return halClient.putResource(method.getReturnType(),
                                             getRelationHref(link, args, method.getParameterAnnotations()), args[0]);

            case DELETE:
                return halClient.deleteResource(method.getReturnType(),
                                                getRelationHref(link, args == null ? EMPTY_ARGS : args, method.getParameterAnnotations()));

            default:
                throw new UnsupportedOperationException("Unexpected HTTP method: " + link.method());
            }

        } else if (method.getName().startsWith("get")) {
            String propertyName = getPropertyName(method.getName());
            Object property = halResource.getProperty(propertyName);
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
                    halResource.addProperty(propertyName, property);
                }
            } else {
                if (!(property instanceof ConvertingMap) && !(property instanceof ConvertingList)) {
                    property = convert(returnType, property);

                    if (property instanceof ConvertingMap || property instanceof ConvertingList) {
                        //noinspection unchecked
                        halResource.addProperty(propertyName, property);
                    }
                }
            }

            return property;
        } else if (method.getName().equals("toString") && args == null) {
            return resourcePath;
        } else if (method.getName().equals("equals") && args != null && args.length == 1) {
            HalResourceInvocationHandler other;

            try {
                other = (HalResourceInvocationHandler) Proxy.getInvocationHandler(args[0]);
            } catch (IllegalArgumentException e) {
                // argument is not a proxy
                return false;
            } catch (ClassCastException e) {
                // argument is the wrong type of proxy
                return false;
            }

            return resourcePath.equals(other.resourcePath);
        } else if (method.getName().equals("hashCode") && args == null) {
            return resourcePath.hashCode();
        }

        throw new UnsupportedOperationException("Don't know how to handle '" + method.getName() + "'");
    }


    //-------------------------------------------------------------
    // Methods - Package
    //-------------------------------------------------------------

    /**
     * The resource this InvocationHandler manages has been updated or deemed stale.
     *
     * @param halResource The new HalResource or null if the resource was stale and the new value is not yet known.
     */
    void resourceUpdated(HalResource halResource) {
        this.halResource = halResource;
    }


    //-------------------------------------------------------------
    // Methods - Private
    //-------------------------------------------------------------

    private String getRelationHref(Link link, Object[] args, Annotation[][] parameterAnnotations) {
        HalLink halLink = halResource.getLink(link.relation());

        if (halLink == null) {
            throw new UnsupportedOperationException(link.relation());
        }

        if (halLink.getDeprecation() != null) {
            log.warn("Link '" + link + "' has been deprecated: " + halLink.getDeprecation());
        }

        String href;

        if (halLink.isTemplated()) {
            try {
                UriTemplate uriTemplate = UriTemplate.fromTemplate(halLink.getHref());

                for (int i = 0; i < args.length; i++) {
                    for (Annotation annotation : parameterAnnotations[i]) {
                        if (annotation.annotationType() == UriVariable.class) {
                            UriVariable uriVariable = (UriVariable) annotation;

                            assignTemplateValue(uriTemplate, uriVariable.name(), args[i]);
                        }
                    }
                }

                for (int i = 0; i < link.uriValues().length; i++) {
                    UriValue uriValue = link.uriValues()[i];

                    assignTemplateValue(uriTemplate, uriValue.name(), uriValue.value());
                }

                href = uriTemplate.expand();
            } catch (MalformedUriTemplateException | VariableExpansionException e) {
                throw new RuntimeException(e);
            }
        } else {
            href = halLink.getHref();
        }

        return href;
    }


    private void assignTemplateValue(UriTemplate uriTemplate, String variableName, Object value) {
        if (uriTemplate.hasVariable(variableName)) {
            log.warn(String.format("Duplicate assignment to variable %s.  Current = '%s', skipping new value '%s'.",
                                   variableName, uriTemplate.get(variableName), value));

            return;
        }

        uriTemplate.set(variableName, value);
    }
}
