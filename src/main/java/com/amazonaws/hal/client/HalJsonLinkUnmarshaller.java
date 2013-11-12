/*
 * Copyright 2010-2013 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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


class HalJsonLinkUnmarshaller
        implements Unmarshaller<HalLink, JsonUnmarshallerContext> {

    //-------------------------------------------------------------
    // Variables - Private - Static
    //-------------------------------------------------------------

    private static HalJsonLinkUnmarshaller instance = new HalJsonLinkUnmarshaller();


    //-------------------------------------------------------------
    // Methods - Package - Static
    //-------------------------------------------------------------

    static HalJsonLinkUnmarshaller getInstance() {
        return instance;
    }


    //-------------------------------------------------------------
    // Implementation - Unmarshaller
    //-------------------------------------------------------------

    @Override
    public HalLink unmarshall(JsonUnmarshallerContext context)
            throws Exception {
        HalLink halLink = new HalLink();
        JsonToken token = context.currentToken;

        while (token != null && token != JsonToken.END_OBJECT) {
            if (token == JsonToken.FIELD_NAME) {
                if (context.testExpression("href")) {
                    context.nextToken();
                    halLink.setHref(context.readText());
                } else if (context.testExpression("name")) {
                    context.nextToken();
                    halLink.setName(context.readText());
                } else if (context.testExpression("title")) {
                    context.nextToken();
                    halLink.setTitle(context.readText());
                } else if (context.testExpression("templated")) {
                    context.nextToken();
                    halLink.setTemplated(Boolean.valueOf(context.readText()));
                } else if (context.testExpression("deprecation")) {
                    context.nextToken();
                    halLink.setDeprecation(context.readText());
                } else {
                    // Ignore this.  Likely one of title, hreflang, profile, type
                    context.nextToken();
                }
            }

            token = context.nextToken();
        }

        return halLink;
    }
}
