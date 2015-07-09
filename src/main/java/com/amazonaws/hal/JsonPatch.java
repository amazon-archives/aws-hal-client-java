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


import java.util.ArrayList;
import java.util.List;


public class JsonPatch {

    //-------------------------------------------------------------
    // Variables - Private
    //-------------------------------------------------------------

    private List<Item> patchItems = new ArrayList<Item>();


    //-------------------------------------------------------------
    // Methods - Public
    //-------------------------------------------------------------

    public JsonPatch with(Item patchItem) {
        patchItems.add(patchItem);

        return this;
    }


    //-------------------------------------------------------------
    // Methods - Getter/Setter
    //-------------------------------------------------------------

    public List<? extends Item> getPatchItems() {
        return patchItems;
    }


    public void setPatchItems(List<Item> patchItems) {
        this.patchItems = patchItems;
    }


    //-------------------------------------------------------------
    // Inner Classes
    //-------------------------------------------------------------

    abstract static class Item {

        //-------------------------------------------------------------
        // Variables - Private
        //-------------------------------------------------------------

        private String path;


        //-------------------------------------------------------------
        // Constructors
        //-------------------------------------------------------------

        public Item(String path) {
            this.path = path;
        }


        //-------------------------------------------------------------
        // Methods - Abstract
        //-------------------------------------------------------------

        public abstract String getOp();


        //-------------------------------------------------------------
        // Methods - Getter
        //-------------------------------------------------------------

        public String getPath() {
            return path;
        }
    }

    
    abstract static class ValueItem extends Item {

        //-------------------------------------------------------------
        // Variables - Private
        //-------------------------------------------------------------

        private String value;


        //-------------------------------------------------------------
        // Constructors
        //-------------------------------------------------------------

        public ValueItem(String path, String value) {
            super(path);

            this.value = value;
        }


        //-------------------------------------------------------------
        // Methods - Getter/Setter
        //-------------------------------------------------------------

        public String getValue() {
            return value;
        }
    }
    
    
    abstract static class FromItem extends Item {

        //-------------------------------------------------------------
        // Variables - Private
        //-------------------------------------------------------------

        private String from;


        //-------------------------------------------------------------
        // Constructors
        //-------------------------------------------------------------

        public FromItem(String path, String from) {
            super(path);

            this.from = from;
        }


        //-------------------------------------------------------------
        // Methods - Getter/Setter
        //-------------------------------------------------------------

        public String getFrom() {
            return from;
        }
    }


    public static class Add extends ValueItem {

        //-------------------------------------------------------------
        // Constructors
        //-------------------------------------------------------------

        public Add(String path, String value) {
            super(path, value);
        }


        //-------------------------------------------------------------
        // Implementation - Item
        //-------------------------------------------------------------

        @Override
        public String getOp() {
            return "add";
        }
    }


    public static class Remove extends Item {

        //-------------------------------------------------------------
        // Constructors
        //-------------------------------------------------------------

        public Remove(String path) {
            super(path);
        }


        //-------------------------------------------------------------
        // Implementation - Item
        //-------------------------------------------------------------

        @Override
        public String getOp() {
            return "remove";
        }
    }


    public static class Replace extends ValueItem {

        //-------------------------------------------------------------
        // Constructors
        //-------------------------------------------------------------

        public Replace(String path, String from) {
            super(path, from);
        }


        //-------------------------------------------------------------
        // Implementation - Item
        //-------------------------------------------------------------

        @Override
        public String getOp() {
            return "replace";
        }
    }


    public static class Move extends FromItem {

        //-------------------------------------------------------------
        // Constructors
        //-------------------------------------------------------------

        public Move(String path, String from) {
            super(path, from);
        }


        //-------------------------------------------------------------
        // Implementation - Item
        //-------------------------------------------------------------

        @Override
        public String getOp() {
            return "move";
        }
    }


    public static class Copy extends FromItem {

        //-------------------------------------------------------------
        // Constructors
        //-------------------------------------------------------------

        public Copy(String path, String from) {
            super(path, from);
        }


        //-------------------------------------------------------------
        // Implementation - Item
        //-------------------------------------------------------------

        @Override
        public String getOp() {
            return "copy";
        }
    }


    public static class Test extends ValueItem {

        //-------------------------------------------------------------
        // Constructors
        //-------------------------------------------------------------

        public Test(String path, String value) {
            super(path, value);
        }


        //-------------------------------------------------------------
        // Implementation - Item
        //-------------------------------------------------------------

        @Override
        public String getOp() {
            return "test";
        }
    }
}
