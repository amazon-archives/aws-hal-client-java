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

package com.amazonaws.hal;


import com.amazonaws.http.HttpMethodName;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * This is used to annotate a resource method to indicate corresponds to a
 * link to another resource or resources.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Link {

    /**
     * The name of the relation that the link corresponds to.
     *
     * @return relation name
     */
    String relation();

    /**
     * The HTTP method that is used when invoking this link.
     * Defaults to GET.
     *
     * @return the HTTP method to use when invoking the link.
     */
    HttpMethodName method() default HttpMethodName.GET;


    /**
     * If this Link produces a map, the key for each entry can be either the item's
     * title or name.  By default, title is used.
     *
     * @return the field to use for map's keys.
     */
    KeyField keyField() default KeyField.Title;


    enum KeyField { Title, Name }
}
