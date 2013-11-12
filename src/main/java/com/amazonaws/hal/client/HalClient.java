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


import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.AmazonWebServiceResponse;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.DefaultRequest;
import com.amazonaws.Request;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.Signer;
import com.amazonaws.http.ExecutionContext;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.http.HttpResponseHandler;
import com.amazonaws.http.JsonErrorResponseHandler;
import com.amazonaws.http.JsonResponseHandler;
import com.amazonaws.internal.DynamoDBBackoffStrategy;
import com.amazonaws.transform.JsonErrorUnmarshaller;
import com.amazonaws.transform.Unmarshaller;
import com.amazonaws.util.AWSRequestMetrics;
import com.amazonaws.util.StringInputStream;
import com.amazonaws.util.json.JSONObject;

import java.lang.reflect.Proxy;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


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
    private List<Unmarshaller<AmazonServiceException, JSONObject>> exceptionUnmarshallers;
    private Signer signer;
    private Map<String, Object> cacheMap;


    //-------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------

    public HalClient(ClientConfiguration clientConfiguration, String endpoint, AWSCredentialsProvider awsCredentialsProvider, Signer signer,
                     Map<String, Object> cacheMap) {
        super(clientConfiguration);

        this.setEndpoint(endpoint);
        this.awsCredentialsProvider = awsCredentialsProvider;
        this.exceptionUnmarshallers = new ArrayList<>();
        this.exceptionUnmarshallers.add(new JsonErrorUnmarshaller());
        this.signer = signer;
        this.cacheMap = cacheMap;
        this.addRequestHandler(new AcceptHalJsonRequestHandler());
    }


    //-------------------------------------------------------------
    // Methods - Public
    //-------------------------------------------------------------

    public <T> T getResource(Class<T> resourceClass, String resourcePath) {
        return getResource(null, resourceClass, resourcePath, false);
    }


    public <T> T putResource(Class<T> resourceClass, String resourcePath, Object representation) {
        ExecutionContext executionContext = createExecutionContext();
        AWSRequestMetrics awsRequestMetrics = executionContext.getAwsRequestMetrics();

        awsRequestMetrics.startEvent(AWSRequestMetrics.Field.RequestMarshallTime.name());
        Request request = new DefaultRequest(null);
        request.setHttpMethod(HttpMethodName.PUT);
        populateResourcePathAndParameters(request, resourcePath);
        assignContent(request, representation);
        awsRequestMetrics.endEvent(AWSRequestMetrics.Field.RequestMarshallTime.name());

        OptionalJsonResponseHandler<HalResource> responseHandler = new OptionalJsonResponseHandler<>(HalJsonResourceUnmarshaller.getInstance());

        HalResource halResource = invoke(request, responseHandler, executionContext);

        Object patchedResource = cacheMap.get(resourcePath);

        if (patchedResource == null) {
            return createResourceProxy(resourceClass, resourcePath, halResource);
        } else {
            HalResourceInvocationHandler patchedResourceInvocationHandler = (HalResourceInvocationHandler) Proxy.getInvocationHandler(patchedResource);

            patchedResourceInvocationHandler.resourceUpdated(resourcePath.equals(halResource._getSelfHref()) ? halResource : null);
            // TODO: follow embedded resources and call resourceUpdated()

            //noinspection unchecked
            return (T) patchedResource;
        }
    }


    public <T> T postResource(Class<T> resourceClass, String resourcePath, Object representation) {
        ExecutionContext executionContext = createExecutionContext();
        AWSRequestMetrics awsRequestMetrics = executionContext.getAwsRequestMetrics();

        awsRequestMetrics.startEvent(AWSRequestMetrics.Field.RequestMarshallTime.name());
        Request request = new DefaultRequest(null);
        request.setHttpMethod(HttpMethodName.POST);
        populateResourcePathAndParameters(request, resourcePath);
        assignContent(request, representation);
        awsRequestMetrics.endEvent(AWSRequestMetrics.Field.RequestMarshallTime.name());

        OptionalJsonResponseHandler<HalResource> responseHandler = new OptionalJsonResponseHandler<>(HalJsonResourceUnmarshaller.getInstance());

        HalResource halResource = invoke(request, responseHandler, executionContext);

        Object postedResource = cacheMap.get(resourcePath);

        if (postedResource != null) {
            HalResourceInvocationHandler postedResourceInvocationHandler = (HalResourceInvocationHandler) Proxy.getInvocationHandler(postedResource);

            postedResourceInvocationHandler.resourceUpdated(null);
            // TODO: follow embedded resources and call resourceUpdated()
        }

        String locationPath;
        if (halResource.isDefined()) {
            locationPath = halResource._getSelfHref();
        } else {
            String endpointString = endpoint.toString();
            String location = responseHandler.getLocation();

            if (location.startsWith(endpointString)) {
                locationPath = location.substring(endpointString.length());
            } else {
                locationPath = location;
            }
        }

        return createResourceProxy(resourceClass, locationPath, halResource);
    }


    public void deleteResource(String resourcePath) {
        ExecutionContext executionContext = createExecutionContext();
        AWSRequestMetrics awsRequestMetrics = executionContext.getAwsRequestMetrics();

        awsRequestMetrics.startEvent(AWSRequestMetrics.Field.RequestMarshallTime.name());
        Request request = new DefaultRequest(null);
        request.setHttpMethod(HttpMethodName.DELETE);
        populateResourcePathAndParameters(request, resourcePath);
        awsRequestMetrics.endEvent(AWSRequestMetrics.Field.RequestMarshallTime.name());

        JsonResponseHandler<HalResource> responseHandler = new JsonResponseHandler<>(HalJsonResourceUnmarshaller.getInstance());

        invoke(request, responseHandler, executionContext);

        cacheMap.remove(resourcePath);
        // TODO: follow embedded resources and call remove()
    }


    //-------------------------------------------------------------
    // Methods - Package
    //-------------------------------------------------------------

    <T> T getResource(HalResource sourceResource, Class<T> resourceClass, String resourcePath, boolean lazy) {
        if (cacheMap.containsKey(resourcePath)) {
            return resourceClass.cast(cacheMap.get(resourcePath));
        }

        HalResource halResource;

        if (sourceResource != null && sourceResource.getEmbedded().containsKey(resourcePath)) {
            halResource = sourceResource.getEmbedded().get(resourcePath);
        } else if (lazy) {
            halResource = null;
        } else {
            halResource = getHalResource(resourcePath);
        }

        return createResourceProxy(resourceClass, resourcePath, halResource);
    }


    HalResource getHalResource(String resourcePath) {
        ExecutionContext executionContext = createExecutionContext();
        AWSRequestMetrics awsRequestMetrics = executionContext.getAwsRequestMetrics();

        awsRequestMetrics.startEvent(AWSRequestMetrics.Field.RequestMarshallTime.name());
        Request request = new DefaultRequest(null);
        request.setHttpMethod(HttpMethodName.GET);
        populateResourcePathAndParameters(request, resourcePath);
        awsRequestMetrics.endEvent(AWSRequestMetrics.Field.RequestMarshallTime.name());

        JsonResponseHandler<HalResource> responseHandler = new JsonResponseHandler<>(HalJsonResourceUnmarshaller.getInstance());

        return invoke(request, responseHandler, executionContext);
    }


    //-------------------------------------------------------------
    // Methods - Private
    //-------------------------------------------------------------

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
                request.addParameter(URLDecoder.decode(parameterPair.substring(0, equalIndex)), URLDecoder.decode(parameterPair.substring(equalIndex + 1)));
            }
        }
    }


    private <T> T invoke(Request request, HttpResponseHandler<AmazonWebServiceResponse<T>> responseHandler, ExecutionContext executionContext)
            throws AmazonClientException {
        request.setEndpoint(endpoint);

        AWSRequestMetrics awsRequestMetrics = executionContext.getAwsRequestMetrics();

        awsRequestMetrics.startEvent(AWSRequestMetrics.Field.CredentialsRequestTime.name());
        AWSCredentials credentials = awsCredentialsProvider.getCredentials();
        awsRequestMetrics.endEvent(AWSRequestMetrics.Field.CredentialsRequestTime.name());

        executionContext.setSigner(signer);
        executionContext.setCredentials(credentials);
        executionContext.setCustomBackoffStrategy(DynamoDBBackoffStrategy.DEFAULT);

        JsonErrorResponseHandler errorResponseHandler = new JsonErrorResponseHandler(exceptionUnmarshallers);

        awsRequestMetrics.startEvent(AWSRequestMetrics.Field.ClientExecuteTime.name());
        T result = client.execute(request, responseHandler, errorResponseHandler, executionContext);
        awsRequestMetrics.endEvent(AWSRequestMetrics.Field.ClientExecuteTime.name());

        awsRequestMetrics.log();

        return result;
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


    private <T> T createResourceProxy(Class<T> resourceClass, String resourcePath, HalResource halResource) {
        Object proxy = Proxy.newProxyInstance(resourceClass.getClassLoader(),
                                              new Class<?>[] { resourceClass },
                                              new HalResourceInvocationHandler(halResource, resourcePath, this));

        cacheMap.put(resourcePath, proxy);

        return resourceClass.cast(proxy);
    }
}
