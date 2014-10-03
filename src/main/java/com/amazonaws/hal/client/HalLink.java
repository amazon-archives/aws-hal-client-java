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


class HalLink {

    //-------------------------------------------------------------
    // Variables - Private
    //-------------------------------------------------------------

    private String href;
    private String name;
    private String title;
    private boolean templated;
    private String deprecation;


    //-------------------------------------------------------------
    // Methods - Getter/Setter
    //-------------------------------------------------------------

    public String getHref() {
        return href;
    }


    public void setHref(String href) {
        this.href = href;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public boolean isTemplated() {
        return templated;
    }


    public void setTemplated(boolean templated) {
        this.templated = templated;
    }


    public String getDeprecation() {
        return deprecation;
    }


    public void setDeprecation(String deprecation) {
        this.deprecation = deprecation;
    }
}
