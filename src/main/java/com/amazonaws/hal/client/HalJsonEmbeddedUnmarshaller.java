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


import com.amazonaws.transform.JsonUnmarshallerContext;
import com.amazonaws.transform.Unmarshaller;

import com.fasterxml.jackson.core.JsonToken;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


class HalJsonEmbeddedUnmarshaller
        implements Unmarshaller<Map<String, HalResource>, JsonUnmarshallerContext> {

    //-------------------------------------------------------------
    // Variables - Private - Static
    //-------------------------------------------------------------

    private static HalJsonEmbeddedUnmarshaller instance = new HalJsonEmbeddedUnmarshaller();


    //-------------------------------------------------------------
    // Methods - Package - Static
    //-------------------------------------------------------------

    static HalJsonEmbeddedUnmarshaller getInstance() {
        return instance;
    }


    //-------------------------------------------------------------
    // Implementation - Unmarshaller
    //-------------------------------------------------------------

    @Override
    public Map<String, HalResource> unmarshall(JsonUnmarshallerContext context)
            throws Exception {
        Map<String, HalResource> embedded = new LinkedHashMap<>();
        JsonToken token = context.getCurrentToken();

        while (token != null && token != JsonToken.END_OBJECT) {
            if (token == JsonToken.FIELD_NAME) {
                // Ignore the field name and move to the next token.  The item's key will be the embedded resource's selfHref.
                token = context.nextToken();

                if (token == JsonToken.START_ARRAY) {
                    List<HalResource> halResources = new HalJsonArrayUnmarshaller<>(HalJsonResourceUnmarshaller.getInstance()).unmarshall(context);

                    for (HalResource halResource : halResources) {
                        embedded.put(halResource._getSelfHref(), halResource);
                    }
                } else {
                    HalResource halResource = HalJsonResourceUnmarshaller.getInstance().unmarshall(context);

                    embedded.put(halResource._getSelfHref(), halResource);
                }
            }

            token = context.nextToken();
        }

        return embedded;
    }
}
