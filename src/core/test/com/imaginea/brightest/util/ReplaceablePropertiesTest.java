/*
 * Copyright (c) 2011 Imaginea Technologies Private Ltd. 
 * Hyderabad, India
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following condition
 * is met:
 *
 *     + Neither the name of Imaginea, nor the
 *       names of its contributors may be used to endorse or promote
 *       products derived from this software.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.imaginea.brightest.util;

import junit.framework.Assert;

import org.junit.Test;

public class ReplaceablePropertiesTest {
    @Test
    public void testReplacements() {
        ReplaceableProperties properties = new ReplaceableProperties();
        String key = "IQMessage";
        properties.setProperty("IQ", "80");
        properties.setProperty(key, "My IQ is ${IQ}");
        Assert.assertEquals("My IQ is 80", properties.getProperty(key));
    }

    @Test
    public void testSimpleGet() {
        ReplaceableProperties properties = new ReplaceableProperties();
        String key = "IQMessage";
        properties.setProperty("IQ", "80");
        properties.setProperty(key, "My IQ is 40");
        Assert.assertEquals("My IQ is 40", properties.getProperty(key));
    }

    @Test
    public void testVariableGet() {
        ReplaceableProperties properties = new ReplaceableProperties();
        String key = "${IQ}";
        properties.setProperty("IQ", "80");
        Assert.assertEquals("80", properties.getProperty(key));
    }
}
