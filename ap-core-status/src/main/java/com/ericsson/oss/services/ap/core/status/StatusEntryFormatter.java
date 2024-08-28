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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.ap.api.exception.StatusEntryUpdateException;
import com.ericsson.oss.services.ap.api.status.StatusEntry;
import com.google.gson.Gson;

/**
 * Class to handle conversion of a {@link StatusEntry} instance to/from a JSON formatted string.
 */
public class StatusEntryFormatter {

    private static final Gson GSON_FORMATTER = new Gson();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Convert the {@link StatusEntry} instance to a JSON formatted string.
     *
     * @param statusEntry
     *            the status entry to convert
     * @return the {@link StatusEntry} instance as a string
     */
    public String toJsonString(final StatusEntry statusEntry) {
        try {
            return GSON_FORMATTER.toJson(statusEntry);
        } catch (final Exception e) {
            logger.warn("Error converting {} to string: {}", statusEntry, e.getMessage(), e);
            throw new StatusEntryUpdateException(
                    "Error occured during conversion of StatusEntry to formatted status entry string: " + e.getMessage());
        }
    }

    /**
     * Convert a JSON formatted string containing status entry information to a {@link StatusEntry} instance.
     *
     * @param statusEntryInfo
     *            the JSON string to convert
     * @return a {@link StatusEntry} instance
     */
    public StatusEntry fromJsonString(final String statusEntryInfo) {
        try {
            return GSON_FORMATTER.fromJson(statusEntryInfo, StatusEntry.class);
        } catch (final Exception e) {
            logger.warn("Error converting [{}] to statusEntry: {}", statusEntryInfo, e.getMessage(), e);
            throw new StatusEntryUpdateException("Error occured during conversion of status entry string to StatusEntry: " + e.getMessage());
        }
    }
}
