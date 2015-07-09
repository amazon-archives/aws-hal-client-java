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


import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonServiceException.ErrorType;
import com.amazonaws.http.HttpResponse;
import com.amazonaws.http.HttpResponseHandler;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Map.Entry;


public class StatusCodeErrorResponseHandler
        implements HttpResponseHandler<AmazonServiceException> {

    //-------------------------------------------------------------
    // Variables - Private
    //-------------------------------------------------------------

    private Map<Integer, Class<? extends AmazonServiceException>> exceptionClasses;


    //-------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------

    public StatusCodeErrorResponseHandler(Map<Integer, Class<? extends AmazonServiceException>> exceptionClasses) {
        this.exceptionClasses = exceptionClasses;
    }


    //-------------------------------------------------------------
    // Implementation - HttpResponseHandler
    //-------------------------------------------------------------


    public AmazonServiceException handle(HttpResponse response)
            throws Exception {
        JSONObject jsonBody = getBodyAsJson(response);
        Class<? extends AmazonServiceException> exceptionClass = exceptionClasses.get(response.getStatusCode());
        AmazonServiceException result;

        // Support other attribute names for the message?
        // TODO: Inspect exception type (caching details) and apply other values from the body
        String message = jsonBody.has("message") ? jsonBody.getString("message") : jsonBody.getString("Message");

        if (exceptionClass != null) {
            result = exceptionClass.getConstructor(String.class).newInstance(message);
        } else {
            result = AmazonServiceException.class.getConstructor(String.class).newInstance(message);
        }

        result.setServiceName(response.getRequest().getServiceName());
        result.setStatusCode(response.getStatusCode());

        if (response.getStatusCode() < 500) {
            result.setErrorType(ErrorType.Client);
        } else {
            result.setErrorType(ErrorType.Service);
        }

        for (Entry<String, String> headerEntry : response.getHeaders().entrySet()) {
            if (headerEntry.getKey().equalsIgnoreCase("X-Amzn-RequestId")) {
                result.setRequestId(headerEntry.getValue());
            }
        }

        return result;
    }


    public boolean needsConnectionLeftOpen() {
        return false;
    }


    //-------------------------------------------------------------
    // Methods - Private
    //-------------------------------------------------------------

    private JSONObject getBodyAsJson(HttpResponse response) {
        try (InputStream stream = response.getContent()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder sb = new StringBuilder();

            while (true) {
                String line = reader.readLine();

                if (line == null) {
                    break;
                }

                sb.append(line);
            }

            return new JSONObject(sb.length() == 0 ? "{}" : sb.toString());
        } catch (IOException e) {
            throw new AmazonClientException("Unable to read error response: " + e.getMessage(), e);
        } catch (JSONException e) {
            throw new AmazonClientException("Unable to parse error response: " + e.getMessage(), e);
        }
    }
}
