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


import com.amazonaws.transform.JsonUnmarshallerContext;
import com.amazonaws.transform.Unmarshaller;

import com.fasterxml.jackson.core.JsonToken;

import java.util.HashMap;
import java.util.Map;


class HalJsonMapUnmarshaller
        implements Unmarshaller<Map<String, Object>, JsonUnmarshallerContext> {

    //-------------------------------------------------------------
    // Variables - Private - Static
    //-------------------------------------------------------------

    private static HalJsonMapUnmarshaller instance = new HalJsonMapUnmarshaller();


    //-------------------------------------------------------------
    // Methods - Package - Static
    //-------------------------------------------------------------

    static HalJsonMapUnmarshaller getInstance() {
        return instance;
    }


    //-------------------------------------------------------------
    // Implementation - Unmarshaller
    //-------------------------------------------------------------

    @Override
    public Map<String, Object> unmarshall(JsonUnmarshallerContext context)
            throws Exception {
        Map<String, Object> map = new HashMap<>();
        JsonToken token = context.getCurrentToken();

        while (token != null && token != JsonToken.END_OBJECT) {
            if (token == JsonToken.FIELD_NAME) {
                String property = context.readText();

                token = context.nextToken();
                if (token == JsonToken.START_OBJECT) {
                    context.nextToken();
                    map.put(property, HalJsonMapUnmarshaller.getInstance().unmarshall(context));
                } else if (token == JsonToken.START_ARRAY) {
                    context.nextToken();
                    map.put(property, HalJsonListUnmarshaller.getInstance().unmarshall(context));
                } else {
                    map.put(property, JsonUnmarshallerUtil.getObjectForToken(token, context));
                }
            }

            token = context.nextToken();
        }

        return map;
    }
}
