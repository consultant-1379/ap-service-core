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
package com.ericsson.oss.services.ap.core.status;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.ericsson.oss.services.ap.api.exception.StatusEntryUpdateException;
import com.ericsson.oss.services.ap.api.status.StatusEntry;

/**
 * Unit tests for {@link StatusEntryFormatter}.
 */
public class StatusEntryFormatterTest {

    private final StatusEntryFormatter formatter = new StatusEntryFormatter();

    @Test
    public void when_valid_status_entry_then_conversion_to_json_is_successful() {
        final String expectedJson = "{\"taskName\":\"TestTask\",\"taskProgress\":\"STARTED\",\"timeStamp\":\"TimeStamp\",\"additionalInfo\":\"Additional, Info\"}";
        final StatusEntry statusEntry = new StatusEntry("TestTask", "STARTED", "TimeStamp", "Additional, Info");
        final String convertedJsonString = formatter.toJsonString(statusEntry);

        assertEquals(expectedJson, convertedJsonString);
    }

    @Test
    public void when_valid_json_then_conversion_to_status_entry_is_successful() {
        final String statusAsJsonString = "{\"taskName\":\"TestTask\",\"taskProgress\":\"STARTED\",\"timeStamp\":\"TimeStamp\",\"additionalInfo\":\"Additional, Info\"}";
        final StatusEntry convertedStatusEntry = formatter.fromJsonString(statusAsJsonString);

        final String taskName = convertedStatusEntry.getTaskName();
        assertEquals("TestTask", taskName);
        final String progress = convertedStatusEntry.getTaskProgress();
        assertEquals("STARTED", progress);
        final String additionalInfo = convertedStatusEntry.getAdditionalInfo();
        assertEquals("Additional, Info", additionalInfo);
        final String timeStamp = convertedStatusEntry.getTimeStamp();
        assertEquals("TimeStamp", timeStamp);
    }

    @Test(expected = StatusEntryUpdateException.class)
    public void when_invalid_json_then_conversion_to_status_entry_fails() {
        final String statusAsJsonString = "{\"InvalidField\":\"value\",\"}";
        formatter.fromJsonString(statusAsJsonString);
    }
}
