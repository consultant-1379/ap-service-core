/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.message.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.MissingResourceException;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link PropertyMessageResources}.
 */
public class PropertyMessageResourcesTest {

    private PropertyMessageResources propertyResources;

    @Before
    public void setUp() throws IOException {
        propertyResources = new PropertyMessageResources(PropertyMessageResourcesTest.class, "test-messages.properties");
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenResourceDoesNotExistThenIllegalArgumentExceptionIsThrown() throws IOException {
        new PropertyMessageResources(PropertyMessageResourcesTest.class, "nonExistentFile.properties");
    }

    @Test
    public void whenResourceIsEmptyThenNoExceptionIsThrown() throws IOException {
        new PropertyMessageResources(PropertyMessageResourcesTest.class, "test-messages-empty.properties");
    }

    @Test
    public void lookupValidKey() {
        final String message = "prop1";
        assertEquals(message, propertyResources.getString("prop1"));
    }

    @Test(expected = MissingResourceException.class)
    public void lookupMissingKey() {
        propertyResources.getString("no.such.prop");
    }

    @Test
    public void formatOutputMessageWithOutReplacement() {
        final String message = "prop1";
        assertEquals(message, propertyResources.format("prop1"));
    }

    @Test
    public void formatOutputMessageWithReplacement() {
        final String message = "My name is Fred";
        assertEquals(message, propertyResources.format("prop2", "Fred"));
    }

    @Test
    public void formatOutputMessageWithReplacementMissingData() {
        final String message = "My name is {0}";
        assertEquals(message, propertyResources.format("prop2"));
    }

    @Test
    public void lookupInt() {
        assertEquals(123, propertyResources.getInt("int1"));
    }

    @Test(expected = NumberFormatException.class)
    public void lookupInvalidInt() {
        propertyResources.getInt("bad.int");
    }

    @Test
    public void lookupBoolean() {
        assertTrue(propertyResources.getBoolean("bool1"));
    }

    @Test
    public void lookupInvalidBoolean() {
        assertFalse(propertyResources.getBoolean("bad.bool"));
    }

    @Test
    public void lookupStringArray() {
        final String[] strings = propertyResources.getStringArray("prop3");
        assertTrue(strings.length == 3);
        for (int i = 0; i < strings.length; i++) {
            assertEquals("prop3." + i, strings[i]);
        }
    }

    @Test
    public void checkIfValidKeyExists() {
        assertTrue(propertyResources.containsKey("prop1"));
    }

    @Test
    public void checkIfMissingKeyExists() {
        assertFalse(propertyResources.containsKey("no.such"));
    }

    @Test
    public void getKeys() {
        final Set<String> keySet = propertyResources.keySet();
        assertEquals(7, keySet.size());
    }

    @Test
    public void getLocale() {
        assertNull(propertyResources.getLocale());
    }
}
