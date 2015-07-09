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


import com.amazonaws.hal.ResourceInfo;
import com.amazonaws.transform.JsonUnmarshallerContext;
import com.amazonaws.transform.JsonUnmarshallerContextImpl;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;


public class HalJsonResourceUnmarshallerTest {

    //-------------------------------------------------------------
    // Methods - Test Cases
    //-------------------------------------------------------------

    @Test
    public void testNested()
            throws Exception {
        HalResource halResource = parseHalResourceFromClasspath("report.resource");

        Assert.assertNotNull(halResource);

        Report report = (Report) Proxy.newProxyInstance(Report.class.getClassLoader(),
                                                        new Class<?>[] { Report.class },
                                                        new HalResourceInvocationHandler(halResource, halResource._getSelfHref(), null));

        Assert.assertNotNull(report.getColumns());
        Assert.assertEquals(4, report.getColumns().size());
        Assert.assertNotNull(report.getColumns().get("day"));
        Assert.assertEquals("date", report.getColumns().get("day").getDisplayName());

        Assert.assertNotNull(report.getRows());
        Assert.assertEquals(30, report.getRows().size());
        Assert.assertEquals(4, report.getRows().get(0).size());
        Assert.assertEquals("2014-02-10T00:00:00Z", report.getRows().get(0).get(0));
        Assert.assertEquals("Android", report.getRows().get(0).get(1));
        Assert.assertEquals(1003921, report.getRows().get(0).get(2));
        Assert.assertEquals(6.34, report.getRows().get(0).get(3));
    }


    @Test
    public void testTypes()
            throws Exception {
        HalResource halResource = parseHalResourceFromClasspath("types.resource");

        Assert.assertNotNull(halResource);

        Types types = (Types) Proxy.newProxyInstance(Types.class.getClassLoader(),
                                                        new Class<?>[] { Types.class },
                                                        new HalResourceInvocationHandler(halResource, halResource._getSelfHref(), null));

        Assert.assertNotNull(types.getIntegerList());
        Assert.assertEquals(3, types.getIntegerList().size());
        Assert.assertEquals(1, (long) types.getIntegerList().get(0));

        Assert.assertNotNull(types.getIntegerMap());
        Assert.assertEquals(3, types.getIntegerMap().size());
        Assert.assertEquals(1, (long) types.getIntegerMap().get("one"));
    }


    @Test
    public void testEmbedded()
            throws Exception {
        HalResource halResource = parseHalResourceFromClasspath("blog.resource");

        Assert.assertNotNull(halResource);
        Assert.assertNotNull(halResource.getEmbedded());
        Assert.assertNotNull(halResource.getEmbedded().get("/people/alan-watts"));

        BlogPost proxy = (BlogPost) Proxy.newProxyInstance(BlogPost.class.getClassLoader(),
                                                           new Class<?>[] { BlogPost.class },
                                                           new HalResourceInvocationHandler(halResource, halResource._getSelfHref(), null));

        Assert.assertEquals("123", proxy.getId());
        Assert.assertNotNull(proxy.getComments());
        Assert.assertEquals(3, proxy.getComments().size());
        Assert.assertEquals("Roger", proxy.getComments().get(1).getAuthor());
    }


    //-------------------------------------------------------------
    // Methods - Private
    //-------------------------------------------------------------

    private HalResource parseHalResourceFromClasspath(String classpathFile)
            throws Exception {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(classpathFile);
        JsonParser jsonParser = new JsonFactory().createJsonParser(inputStream);
        JsonUnmarshallerContext jsonUnmarshallerContext = new JsonUnmarshallerContextImpl(jsonParser);

        return HalJsonResourceUnmarshaller.getInstance().unmarshall(jsonUnmarshallerContext);
    }


    //-------------------------------------------------------------
    // Inner Classes - Representations
    //-------------------------------------------------------------

    public interface Report extends ResourceInfo {
        Map<String, Column> getColumns();
        List<List<Object>> getRows();
    }


    public interface Column {

        String getName();
        String getDisplayName();
        String getDataType();
        String getFormat();
        String getUnit();
        String getType();
        int getColIndex();
    }


    public interface Types {
        List<Integer> getIntegerList();
        Map<String, Integer> getIntegerMap();
    }


    public interface BlogPost {
        String getId();
        List<Comment> getComments();
    }


    public interface Comment {
        Long getTime();
        String getAuthor();
        String getText();
    }


    public interface Author {
        String getName();
        String getBorn();
        String getDied();
        List<String> getIsbns();
    }
}
