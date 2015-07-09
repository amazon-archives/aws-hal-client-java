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


class HalJsonLinksUnmarshaller
        implements Unmarshaller<Map<String, HalLink>, JsonUnmarshallerContext> {

    //-------------------------------------------------------------
    // Variables - Private - Static
    //-------------------------------------------------------------

    private static HalJsonLinksUnmarshaller instance = new HalJsonLinksUnmarshaller();


    //-------------------------------------------------------------
    // Methods - Package - Static
    //-------------------------------------------------------------

    static HalJsonLinksUnmarshaller getInstance() {
        return instance;
    }


    //-------------------------------------------------------------
    // Implementation - Unmarshaller
    //-------------------------------------------------------------

    @Override
    public Map<String, HalLink> unmarshall(JsonUnmarshallerContext context)
            throws Exception {
        Map<String, HalLink> links = new LinkedHashMap<>();
        JsonToken token = context.getCurrentToken();

        while (token != null && token != JsonToken.END_OBJECT) {
            if (token == JsonToken.FIELD_NAME) {
                if (context.testExpression("curie")) {
                    context.nextToken();
                    HalJsonCurieUnmarshaller.getInstance().unmarshall(context);
                } else {
                    String relation = context.readText();
                    token = context.nextToken();

                    if (token == JsonToken.START_ARRAY) {
                        List<HalLink> halLinks = new HalJsonArrayUnmarshaller<>(HalJsonLinkUnmarshaller.getInstance()).unmarshall(context);

                        int i = 0;
                        for (HalLink halLink : halLinks) {
                            links.put(relation + "_" + i++, halLink);
                        }
                    } else {
                        links.put(relation, HalJsonLinkUnmarshaller.getInstance().unmarshall(context));
                    }
                }
            }

            token = context.nextToken();
        }

        return links;
    }
}
