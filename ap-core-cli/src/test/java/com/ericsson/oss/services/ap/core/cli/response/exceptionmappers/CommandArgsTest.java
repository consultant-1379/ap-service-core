/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.cli.response.exceptionmappers;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.HARDWARE_SERIAL_NUMBER_VALUE;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_NAME;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for {@link CommandArgs}.
 */
public class CommandArgsTest {

    private static final String FILE_NAME = "fileName";

    @Test
    public void whenGettingProjectOrNodeName_andCommandContainsProject_thenProjectNameIsReturned() {
        final String command = "ap view -p " + PROJECT_NAME;
        final String result = CommandArgs.getProjectOrNodeName(command);
        assertEquals(PROJECT_NAME, result);
    }

    @Test
    public void whenGettingProjectOrNodeName_andCommandContainsNode_thenNodeNameIsReturned() {
        final String command = "ap view -n " + NODE_NAME;
        final String result = CommandArgs.getProjectOrNodeName(command);
        assertEquals(NODE_NAME, result);
    }

    @Test
    public void whenGettingProjectOrNodeName_andCommandContainsNoProjectOrNode_thenEmptyStringIsReturned() {
        final String command = "ap view";
        final String result = CommandArgs.getProjectOrNodeName(command);
        assertEquals("", result);
    }

    @Test
    public void whenGettingCommandTarget_andCommandContainsProject_thenProjectIsReturned() {
        final String command = "ap view -p " + PROJECT_NAME;
        final String result = CommandArgs.getCommandTarget(command);
        assertEquals("project", result);
    }

    @Test
    public void whenGettingCommandTarget_andCommandDoesNotContainProject_thenNodeIsReturned() {
        final String command = "ap view";
        final String result = CommandArgs.getCommandTarget(command);
        assertEquals("node", result);
    }

    @Test
    public void whenGettingFileName_andCommandHasFilePrefixAndFileName_thenFileNameIsReturned() {
        final String command = "ap order file:" + FILE_NAME;
        final String result = CommandArgs.getFilename(command);
        assertEquals(FILE_NAME, result);
    }

    @Test
    public void whenGettingFileName_andCommandHasFilePrefixButNoFileName_thenEmptyStringIsReturned() {
        final String command = "ap order file:";
        final String result = CommandArgs.getFilename(command);
        assertEquals("", result);
    }

    @Test
    public void whenGettingFileName_andCommandHasNoFilePrefix_thenEmptyStringIsReturned() {
        final String command = "ap order " + FILE_NAME;
        final String result = CommandArgs.getFilename(command);
        assertEquals("", result);
    }

    @Test
    public void whenGettingFileName_andCommandIsEmpty_thenFileNameIsReturned() {
        final String command = "";
        final String result = CommandArgs.getFilename(command);
        assertEquals("", result);
    }

    @Test
    public void whenGettingHardwareSerialNumber_andCommandContainsSerialFlag_thenSerialNumberIsReturned() {
        final String command = "ap bind -s " + HARDWARE_SERIAL_NUMBER_VALUE;
        final String result = CommandArgs.getHarwareSerialNumber(command);
        assertEquals(HARDWARE_SERIAL_NUMBER_VALUE, result);
    }

    @Test
    public void whenGettingHardwareSerialNumber_andCommandContainsNoSerialFlag_thenSerialNumberIsReturned() {
        final String command = "ap bind " + HARDWARE_SERIAL_NUMBER_VALUE;
        final String result = CommandArgs.getHarwareSerialNumber(command);
        assertEquals("", result);
    }
}
