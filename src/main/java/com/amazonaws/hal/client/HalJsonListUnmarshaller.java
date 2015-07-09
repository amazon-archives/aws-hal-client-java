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

import java.util.ArrayList;
import java.util.List;


class HalJsonListUnmarshaller
        implements Unmarshaller<List<Object>, JsonUnmarshallerContext> {

    //-------------------------------------------------------------
    // Variables - Private - Static
    //-------------------------------------------------------------

    private static HalJsonListUnmarshaller instance = new HalJsonListUnmarshaller();


    //-------------------------------------------------------------
    // Methods - Package - Static
    //-------------------------------------------------------------

    static HalJsonListUnmarshaller getInstance() {
        return instance;
    }


    //-------------------------------------------------------------
    // Implementation - Unmarshaller
    //-------------------------------------------------------------

    @Override
    public List<Object> unmarshall(JsonUnmarshallerContext context)
            throws Exception {
        List<Object> list = new ArrayList<>();
        JsonToken token = context.getCurrentToken();

        while (token != null && token != JsonToken.END_ARRAY) {
            if (token.isScalarValue()) {
                list.add(JsonUnmarshallerUtil.getObjectForToken(token, context));
            } else if (token == JsonToken.START_OBJECT) {
                context.nextToken();
                list.add(HalJsonMapUnmarshaller.getInstance().unmarshall(context));
            } else if (token == JsonToken.START_ARRAY) {
                context.nextToken();
                list.add(HalJsonListUnmarshaller.getInstance().unmarshall(context));
            }

            token = context.nextToken();
        }

        return list;
    }
}
