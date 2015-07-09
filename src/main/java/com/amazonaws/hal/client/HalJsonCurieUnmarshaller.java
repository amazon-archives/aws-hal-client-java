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


class HalJsonCurieUnmarshaller
        implements Unmarshaller<HalLink, JsonUnmarshallerContext> {

    //-------------------------------------------------------------
    // Variables - Private - Static
    //-------------------------------------------------------------

    private static HalJsonCurieUnmarshaller instance = new HalJsonCurieUnmarshaller();


    //-------------------------------------------------------------
    // Methods - Package - Static
    //-------------------------------------------------------------

    static HalJsonCurieUnmarshaller getInstance() {
        return instance;
    }


    //-------------------------------------------------------------
    // Implementation - Unmarshaller
    //-------------------------------------------------------------

    @Override
    public HalLink unmarshall(JsonUnmarshallerContext context)
            throws Exception {
        HalLink halLink = new HalLink();
        JsonToken token = context.getCurrentToken();

        // Ignore curies for now.
        while (token != null && token != JsonToken.END_OBJECT) {
            token = context.nextToken();
        }

        return halLink;
    }
}
