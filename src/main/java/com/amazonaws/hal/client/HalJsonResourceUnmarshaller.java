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


class HalJsonResourceUnmarshaller
        implements Unmarshaller<HalResource, JsonUnmarshallerContext> {

    //-------------------------------------------------------------
    // Variables - Private - Static
    //-------------------------------------------------------------

    private static HalJsonResourceUnmarshaller instance = new HalJsonResourceUnmarshaller();


    //-------------------------------------------------------------
    // Methods - Package - Static
    //-------------------------------------------------------------

    static HalJsonResourceUnmarshaller getInstance() {
        return instance;
    }


    //-------------------------------------------------------------
    // Implementation - Unmarshaller
    //-------------------------------------------------------------

    @Override
    public HalResource unmarshall(JsonUnmarshallerContext context)
            throws Exception {
        HalResource halResource = new HalResource();
        JsonToken token = context.getCurrentToken();

        if (token == null) {
            token = context.nextToken();
        }

        while (token != null && token != JsonToken.END_OBJECT) {
            if (token == JsonToken.FIELD_NAME) {
                if (context.testExpression("_links")) {
                    context.nextToken();
                    halResource.setLinks(HalJsonLinksUnmarshaller.getInstance().unmarshall(context));
                } else if (context.testExpression("_embedded")) {
                    context.nextToken();
                    halResource.setEmbedded(HalJsonEmbeddedUnmarshaller.getInstance().unmarshall(context));
                } else {
                    String property = context.readText();

                    token = context.nextToken();

                    if (token == JsonToken.START_OBJECT) {
                        context.nextToken();
                        halResource.addProperty(property, HalJsonMapUnmarshaller.getInstance().unmarshall(context));
                    } else if (token == JsonToken.START_ARRAY) {
                        context.nextToken();
                        halResource.addProperty(property, HalJsonListUnmarshaller.getInstance().unmarshall(context));
                    } else {
                        halResource.addProperty(property, JsonUnmarshallerUtil.getObjectForToken(token, context));
                    }
                }
            }

            token = context.nextToken();
        }

        return halResource;
    }
}
