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

import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;


class JsonUnmarshallerUtil {

    //-------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------

    private JsonUnmarshallerUtil() {
    }


    //-------------------------------------------------------------
    // Methods - Package
    //-------------------------------------------------------------

    static Object getObjectForToken(JsonToken token, JsonUnmarshallerContext context)
            throws IOException {
        switch (token) {
        case VALUE_STRING:
            return context.getJsonParser().getText();
        case VALUE_NUMBER_FLOAT:
        case VALUE_NUMBER_INT:
            return context.getJsonParser().getNumberValue();
        case VALUE_FALSE:
            return Boolean.FALSE;
        case VALUE_TRUE:
            return Boolean.TRUE;
        case VALUE_NULL:
            return null;
        default:
            throw new RuntimeException("We expected a VALUE token but got: " + token);
        }
    }
}
