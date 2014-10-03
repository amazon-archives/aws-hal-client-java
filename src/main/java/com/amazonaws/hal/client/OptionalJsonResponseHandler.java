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


import com.amazonaws.http.JsonResponseHandler;
import com.amazonaws.transform.JsonUnmarshallerContext;
import com.amazonaws.transform.Unmarshaller;

import java.util.Map;


class OptionalJsonResponseHandler<T> extends JsonResponseHandler<T> {

    //-------------------------------------------------------------
    // Variables - Private
    //-------------------------------------------------------------

    private String location;


    //-------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------

    OptionalJsonResponseHandler(Unmarshaller<T, JsonUnmarshallerContext> responseUnmarshaller) {
        super(responseUnmarshaller);
    }


    //-------------------------------------------------------------
    // Methods - Getter/Setter
    //-------------------------------------------------------------

    String getLocation() {
        return location;
    }


    //-------------------------------------------------------------
    // Methods - Protected
    //-------------------------------------------------------------

    @Override
    protected void registerAdditionalMetadataExpressions(JsonUnmarshallerContext unmarshallerContext) {
        Map<String, String> headers = unmarshallerContext.getHttpResponse().getHeaders();
        
        if (headers.containsKey("Location")) {
            location = headers.get("Location");
        } else if (headers.containsKey("location")) {
            location = headers.get("location");
        }
    }
}
