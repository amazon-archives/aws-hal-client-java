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

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.core.JsonToken.END_ARRAY;


class HalJsonArrayUnmarshaller<T>
        implements Unmarshaller<List<T>, JsonUnmarshallerContext> {

    //-------------------------------------------------------------
    // Constants
    //-------------------------------------------------------------

    private final Unmarshaller<T, JsonUnmarshallerContext> itemUnmarshaller;


    //-------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------

    public HalJsonArrayUnmarshaller(Unmarshaller<T, JsonUnmarshallerContext> itemUnmarshaller) {
        this.itemUnmarshaller = itemUnmarshaller;
    }


    //-------------------------------------------------------------
    // Implementation - Unmarshaller
    //-------------------------------------------------------------

    @Override
    public List<T> unmarshall(JsonUnmarshallerContext context)
            throws Exception {
        List<T> list = new ArrayList<>();
        JsonToken token = context.getCurrentToken();

        while (token != null && token != END_ARRAY) {
            if (token == JsonToken.START_OBJECT) {
                list.add(itemUnmarshaller.unmarshall(context));
            }

            token = context.nextToken();
        }

        return list;
    }
}
