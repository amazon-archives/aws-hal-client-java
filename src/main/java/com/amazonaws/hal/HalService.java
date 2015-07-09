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


import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.hal.client.HalClient;
import com.amazonaws.http.HttpResponseHandler;
import com.amazonaws.http.JsonErrorResponseHandler;
import com.amazonaws.transform.JsonErrorUnmarshaller;

import java.util.Collections;
import java.util.Map;


/**
 * The HalService simplifies the configuration and access to an AWS HAL-based service API.  It is constructed
 * with service information, the interface class that describes the service's root resource, and an optional path
 * to the service root (if not specified it is assumed to be "/").  A HalService can be further configured,
 * using the builder pattern, to use a particular AWSCredentialsProvider or ClientConfiguration.
 *
 * Once configured, this class is used to retrieve the service's root resource.
 *
 * @param <T> The type of the root resource.
 */
public class HalService<T> {

    //-------------------------------------------------------------
    // Variables - Private
    //-------------------------------------------------------------

    private String endpoint;
    private String serviceName;
    private String regionId;
    private Class<T> rootClass;
    private String rootPath;
    private ClientConfiguration clientConfiguration;
    private AWSCredentialsProvider awsCredentialsProvider;
    private Map<String, Object> resourceCache;
    private HttpResponseHandler<AmazonServiceException> errorResponseHandler;
    private HalClient halClient;

    public static String DEFAULT_ROOT_PATH = "/";


    //-------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------

    public HalService(String endpoint, String serviceName, String regionId, Class<T> rootClass,
                      HttpResponseHandler<AmazonServiceException> errorResponseHandler) {
        this(endpoint, serviceName, regionId, rootClass, DEFAULT_ROOT_PATH, errorResponseHandler);
    }


    public HalService(String endpoint, String serviceName, String regionId, Class<T> rootClass, String rootPath,
                      HttpResponseHandler<AmazonServiceException> errorResponseHandler) {
        this.endpoint = endpoint;
        this.serviceName = serviceName;
        this.regionId = regionId;
        this.rootClass = rootClass;
        this.rootPath = rootPath;
        this.errorResponseHandler = errorResponseHandler;
    }


    //-------------------------------------------------------------
    // Methods - Configuration
    //-------------------------------------------------------------

    public HalService<T> with(ClientConfiguration clientConfiguration) {
        setClientConfiguration(clientConfiguration);

        return this;
    }


    public void setClientConfiguration(ClientConfiguration clientConfiguration) {
        this.clientConfiguration = clientConfiguration;
    }


    public HalService<T> with(AWSCredentialsProvider awsCredentialsProvider) {
        setAwsCredentialsProvider(awsCredentialsProvider);

        return this;
    }


    public void setAwsCredentialsProvider(AWSCredentialsProvider awsCredentialsProvider) {
        this.awsCredentialsProvider = awsCredentialsProvider;
    }


    public HalService<T> with(Map<String, Object> resourceCache) {
        setResourceCache(resourceCache);

        return this;
    }


    public void setResourceCache(Map<String, Object> resourceCache) {
        this.resourceCache = resourceCache;
    }


    //-------------------------------------------------------------
    // Methods - Public
    //-------------------------------------------------------------

    public T getRootResource() {
        return getHalClient().getResource(rootClass, rootPath);
    }


    //-------------------------------------------------------------
    // Methods - Private
    //-------------------------------------------------------------

    private HalClient getHalClient() {
        if (halClient == null) {
            this.halClient = new HalClient(clientConfiguration == null ? new ClientConfiguration() : clientConfiguration,
                                           endpoint,
                                           serviceName,
                                           awsCredentialsProvider == null ? new DefaultAWSCredentialsProviderChain() : awsCredentialsProvider,
                                           resourceCache == null ? ImmediatelyExpiringCache.getInstance() : resourceCache,
                                           errorResponseHandler);

            if (regionId != null) {
                halClient.setSignerRegionOverride(regionId);
            }
        }

        return halClient;
    }
}
