/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.cli;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for {@link CliCommand}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CliCommandTest {

    @Test
    public void testOperationIsEmptyWhenNotIncludedInCommand() {
        final CliCommand command = new CliCommand("ap   ", null);
        assertTrue(command.getOperation().isEmpty());
    }

    @Test
    public void testParametersAreEmptyWhenNotIncludedInCommand() {
        final CliCommand command = new CliCommand("ap order", null);
        assertTrue(command.getParameters().length == 0);
    }

    @Test
    public void testPropertiesAreEmptyWhenNotIncludedInCommand() {
        final CliCommand command = new CliCommand("ap order file:myfile", null);
        assertTrue(command.getProperties().isEmpty());
    }

    @Test
    public void testFullCommandSet() {
        final CliCommand command = new CliCommand("ap order file:myfile", null);
        assertEquals("ap order file:myfile", command.getFullCommand());
    }

    @Test
    public void testSpacesRemovedFromFullCommandSet() {
        final CliCommand command = new CliCommand("  ap   order    file:myfile   ", null);
        assertEquals("ap order file:myfile", command.getFullCommand());
    }

    @Test
    public void testCommandOperationSet() {
        final CliCommand command = new CliCommand("ap order file:myfile   ", null);
        assertEquals("order", command.getOperation());
    }

    @Test
    public void testParametersSet() {
        final String[] expectedParams = new String[] { "-a", "arg1", "-b", "arg2", "arg3", "-c", "arg4" };
        final CliCommand command = new CliCommand("ap cmd -a arg1 -b arg2 arg3 -c arg4", null);
        assertArrayEquals(expectedParams, command.getParameters());
    }

    @Test
    public void testCommandPropertiesSet() {
        final Map<String, Object> commandProperties = new HashMap<>();
        final CliCommand command = new CliCommand("  ap   order    file:myfile   ", commandProperties);
        assertEquals(commandProperties, command.getProperties());
    }
}
