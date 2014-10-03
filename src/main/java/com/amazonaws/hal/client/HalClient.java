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


import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.AmazonWebServiceResponse;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.DefaultRequest;
import com.amazonaws.Request;
import com.amazonaws.Response;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.http.ExecutionContext;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.http.HttpResponseHandler;
import com.amazonaws.http.JsonErrorResponseHandler;
import com.amazonaws.http.JsonResponseHandler;
import com.amazonaws.transform.JsonErrorUnmarshaller;
import com.amazonaws.util.AWSRequestMetrics;
import com.amazonaws.util.StringInputStream;
import com.amazonaws.util.json.JSONObject;

import java.lang.reflect.Proxy;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.amazonaws.http.HttpMethodName.GET;
import static com.amazonaws.http.HttpMethodName.POST;
import static com.amazonaws.http.HttpMethodName.PUT;
import static com.amazonaws.http.HttpMethodName.DELETE;


/**
 * The HalClient is a lower-level class for interacting with a HAL-based API.  Preferably, a set of interface
 * classes that describe the interaction with the service is used, and this class is only used behind the
 * scenes.
 */
public class HalClient extends AmazonWebServiceClient {

    //-------------------------------------------------------------
    // Variables - Private
    //-------------------------------------------------------------

    private AWSCredentialsProvider awsCredentialsProvider;
    private List<JsonErrorUnmarshaller> exceptionUnmarshallers;
    private Map<String, Object> resourceCache;


    //-------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------

    public HalClient(ClientConfiguration clientConfiguration, String endpoint, String serviceName,
                     AWSCredentialsProvider awsCredentialsProvider, Map<String, Object> resourceCache) {
        super(clientConfiguration);

        this.setServiceNameIntern(serviceName);
        this.setEndpoint(endpoint);
        this.awsCredentialsProvider = awsCredentialsProvider;
        this.exceptionUnmarshallers = new ArrayList<>();
        this.exceptionUnmarshallers.add(new JsonErrorUnmarshaller());
        this.resourceCache = resourceCache;
        this.addRequestHandler(new AcceptHalJsonRequestHandler());
    }


    //-------------------------------------------------------------
    // Methods - Public
    //-------------------------------------------------------------

    public <T> T getResource(Class<T> resourceClass, String resourcePath) {
        return getResource(null, resourceClass, resourcePath, false);
    }


    public <T> T postResource(Class<T> resourceClass, String resourcePath, Object representation) {
        OptionalJsonResponseHandler<HalResource> responseHandler = new OptionalJsonResponseHandler<>(HalJsonResourceUnmarshaller.getInstance());
        HalResource halResource = invoke(POST, resourcePath, representation, responseHandler);
        Object cachedResource = resourceCache.get(resourcePath);

        String halResourcePath = getHalResourcePath(halResource, responseHandler);

        // Check if the cached resource we just POSTed to is the same resource we got back.  If yes, update the existing proxy's
        // invocation handler with the new data and return it.  If no, the target was likely a collection resource.  We leave the
        // cached data as is, letting the caching strategy update it when needed.
        // TODO: Review collection cache clearing strategy.
        if (cachedResource != null && resourcePath.equals(halResourcePath)) {
            HalResourceInvocationHandler invocationHandler = (HalResourceInvocationHandler) Proxy.getInvocationHandler(cachedResource);

            invocationHandler.resourceUpdated(halResource);
            // TODO: follow embedded resources and call resourceUpdated()

            return resourceClass.cast(cachedResource);
        }

        return createAndCacheResource(resourceClass, halResourcePath, halResource);
    }


    public <T> T putResource(Class<T> resourceClass, String resourcePath, Object representation) {
        OptionalJsonResponseHandler<HalResource> responseHandler = new OptionalJsonResponseHandler<>(HalJsonResourceUnmarshaller.getInstance());
        HalResource halResource = invoke(PUT, resourcePath, representation,responseHandler);
        Object cachedResource = resourceCache.get(resourcePath);

        // Per RFC2616, section 9.6 PUT, the cached resource should refer to the same resource we just received, so we update the
        // existing proxy's invocation handler with the new data.
        if (cachedResource != null) {
            HalResourceInvocationHandler invocationHandler = (HalResourceInvocationHandler) Proxy.getInvocationHandler(cachedResource);

            invocationHandler.resourceUpdated(halResource);
            // TODO: follow embedded resources and call resourceUpdated()

            return resourceClass.cast(cachedResource);
        }

        return createAndCacheResource(resourceClass, resourcePath, halResource);
    }


    public <T> T deleteResource(Class<T> resourceClass, String resourcePath) {
        OptionalJsonResponseHandler<HalResource> responseHandler = new OptionalJsonResponseHandler<>(HalJsonResourceUnmarshaller.getInstance());
        HalResource halResource = invoke(DELETE, resourcePath, null, responseHandler);
        Object cachedResource = resourceCache.get(resourcePath);

        // Per RFC2616, section 9.7 DELETE, the resource should be removed from the cache.  We additionally clear the cached
        // resource in case references to the proxy exist elsewhere.
        if (cachedResource != null) {
            HalResourceInvocationHandler invocationHandler = (HalResourceInvocationHandler) Proxy.getInvocationHandler(cachedResource);

            invocationHandler.resourceUpdated(null);

            resourceCache.remove(resourcePath);
            // TODO: follow embedded resources and call remove() and resourceUpdated()
        }

        return createResource(resourceClass, resourcePath, halResource);
    }


    //-------------------------------------------------------------
    // Methods - Package
    //-------------------------------------------------------------

    <T> T getResource(HalResource sourceResource, Class<T> resourceClass, String resourcePath, boolean lazy) {
        if (resourceCache.containsKey(resourcePath)) {
            return resourceClass.cast(resourceCache.get(resourcePath));
        }

        HalResource halResource;

        if (sourceResource != null && sourceResource.getEmbedded().containsKey(resourcePath)) {
            halResource = sourceResource.getEmbedded().get(resourcePath);
        } else if (lazy) {
            halResource = null;
        } else {
            halResource = getHalResource(resourcePath);
        }

        return createAndCacheResource(resourceClass, resourcePath, halResource);
    }


    HalResource getHalResource(String resourcePath) {
        JsonResponseHandler<HalResource> responseHandler = new JsonResponseHandler<>(HalJsonResourceUnmarshaller.getInstance());

        return invoke(GET, resourcePath, null, responseHandler);
    }


    //-------------------------------------------------------------
    // Methods - Private
    //-------------------------------------------------------------

    private <T> T invoke(HttpMethodName httpMethodName, String resourcePath, Object representation,
                         HttpResponseHandler<AmazonWebServiceResponse<T>> responseHandler)
            throws AmazonClientException {
        ExecutionContext executionContext = createExecutionContext();
        AWSRequestMetrics awsRequestMetrics = executionContext.getAwsRequestMetrics();

        awsRequestMetrics.startEvent(AWSRequestMetrics.Field.RequestMarshallTime.name());
        Request request = buildRequest(httpMethodName, resourcePath, representation);
        awsRequestMetrics.endEvent(AWSRequestMetrics.Field.RequestMarshallTime.name());

        awsRequestMetrics.startEvent(AWSRequestMetrics.Field.CredentialsRequestTime.name());
        AWSCredentials credentials = awsCredentialsProvider.getCredentials();
        awsRequestMetrics.endEvent(AWSRequestMetrics.Field.CredentialsRequestTime.name());

        executionContext.setCredentials(credentials);

        JsonErrorResponseHandler errorResponseHandler = new JsonErrorResponseHandler(exceptionUnmarshallers);

        awsRequestMetrics.startEvent(AWSRequestMetrics.Field.ClientExecuteTime.name());
        Response<T> response = client.execute(request, responseHandler, errorResponseHandler, executionContext);
        awsRequestMetrics.endEvent(AWSRequestMetrics.Field.ClientExecuteTime.name());

        awsRequestMetrics.log();

        return response.getAwsResponse();
    }


    private Request buildRequest(HttpMethodName httpMethodName, String resourcePath, Object representation) {
        Request request = new DefaultRequest(null);

        request.setHttpMethod(httpMethodName);
        request.setEndpoint(endpoint);

        populateResourcePathAndParameters(request, resourcePath);

        if (representation != null) {
            assignContent(request, representation);
        }

        return request;
    }


    private void populateResourcePathAndParameters(Request request, String resourcePath) {
        int questionIndex = resourcePath.indexOf("?");

        if (questionIndex < 0) {
            request.setResourcePath(resourcePath);

            return;
        }

        request.setResourcePath(resourcePath.substring(0, questionIndex));

        for (String parameterPair : resourcePath.substring(questionIndex + 1).split("&")) {
            int equalIndex = parameterPair.indexOf("=");

            if (equalIndex < 0) { // key with no value
                //noinspection deprecation
                request.addParameter(URLDecoder.decode(parameterPair), null);
            } else { // key=value
                //noinspection deprecation
                request.addParameter(URLDecoder.decode(parameterPair.substring(0, equalIndex)),
                                     URLDecoder.decode(parameterPair.substring(equalIndex + 1)));
            }
        }
    }


    private void assignContent(Request request, Object representation) {
        String contentString = new JSONObject(representation).toString();

        if (contentString == null) {
            throw new AmazonClientException("Unable to marshall representation to JSON: " + representation);
        }

        try {
            byte[] contentBytes = contentString.getBytes("UTF-8");

            request.setContent(new StringInputStream(contentString));
            request.addHeader("Content-Length", Integer.toString(contentBytes.length));
            request.addHeader("Content-Type", "application/json");
        } catch(Throwable t) {
            throw new AmazonClientException("Unable to marshall request to JSON: " + t.getMessage(), t);
        }
    }


    private <T> T createAndCacheResource(Class<T> resourceClass, String resourcePath, HalResource halResource) {
        T t = createResource(resourceClass, resourcePath, halResource);

        resourceCache.put(resourcePath, t);

        return t;
    }


    private <T> T createResource(Class<T> resourceClass, String resourcePath, HalResource halResource) {
        Object proxy = Proxy.newProxyInstance(resourceClass.getClassLoader(),
                                              new Class<?>[] { resourceClass },
                                              new HalResourceInvocationHandler(halResource, resourcePath, this));

        return resourceClass.cast(proxy);
    }


    private String getHalResourcePath(HalResource halResource, OptionalJsonResponseHandler<HalResource> responseHandler) {
        String resourcePath;

        if (halResource != null && halResource.isDefined()) {
            resourcePath = halResource._getSelfHref();
        } else {
            String endpointString = endpoint.toString();
            String location = responseHandler.getLocation();

            // Will throw an NPE if no location is present.  This is okay, since it means we don't know what this resource is
            // or where to find it.
            if (location.startsWith(endpointString)) {
                resourcePath = location.substring(endpointString.length());
            } else {
                resourcePath = location;
            }
        }

        return resourcePath;
    }
}
