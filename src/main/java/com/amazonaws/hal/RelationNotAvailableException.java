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

package com.amazonaws.hal;


public class RelationNotAvailableException extends RuntimeException {

    //-------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------

    public RelationNotAvailableException() {
    }


    public RelationNotAvailableException(String message) {
        super(message);
    }


    public RelationNotAvailableException(Throwable cause) {
        super(cause);
    }


    public RelationNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }


    public RelationNotAvailableException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
