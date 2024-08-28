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
package com.ericsson.oss.services.ap.api.bind;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The result of a batch bind.
 */
public final class BatchBindResult implements Serializable {

    private static final long serialVersionUID = -3429317182389300099L;

    private final List<String> failedBindMessages = new ArrayList<>();
    private final Map<String, String> successfulBindsByHwId = new HashMap<>();

    private BatchBindResult() {

    }

    /**
     * Checks if the batch bind was successful for all nodes.
     *
     * @return true if bind is successful for all nodes
     */
    public boolean isSuccessful() {
        return failedBindMessages.isEmpty();
    }

    /**
     * Checks if the batch bind was unsuccessful for all nodes.
     *
     * @return true if bind failed for all nodes
     */
    public boolean isFailed() {
        return successfulBindsByHwId.isEmpty() && !failedBindMessages.isEmpty();
    }

    /**
     * Checks if the batch bind was successful for some nodes and unsuccessful for some nodes.
     *
     * @return true if bind failed for one or more nodes
     */
    public boolean isPartial() {
        return getSuccessfulBinds() > 0 && getFailedBinds() > 0;
    }

    /**
     * The total number of nodes on which a bind was attempted.
     *
     * @return the total number of nodes for which bind was executed
     */
    public int getTotalBinds() {
        return getSuccessfulBinds() + getFailedBinds();
    }

    /**
     * The number of successfully bound nodes.
     *
     * @return the number of successful binds
     */
    public int getSuccessfulBinds() {
        return successfulBindsByHwId.size();
    }

    /**
     * The error message for each bind failure
     *
     * @return error message for each bind failure
     */
    public List<String> getFailedBindMessages() {
        return Collections.unmodifiableList(failedBindMessages);
    }

    /**
     * Returns a map of serial number and node name for successful binds.
     *
     * @return map containing serial number and node name for all successful binds
     */
    public Map<String, String> getSuccessfulBindDetails() {
        return Collections.unmodifiableMap(successfulBindsByHwId);
    }

    /**
     * The number of nodes on which bind failed.
     *
     * @return the number of failed binds
     */
    public int getFailedBinds() {
        return failedBindMessages.size();
    }

    private void addFailureMessage(final String errorMessage) { //NOSONAR
        failedBindMessages.add(errorMessage);
    }

    private void addSuccessfulBindDetails(final String hardwareSerialNumber, final String nodeName) { //NOSONAR
        successfulBindsByHwId.put(hardwareSerialNumber, nodeName);
    }

    /**
     * Builds a {@link BatchBindResult}.
     */
    public static class Builder {

        private final BatchBindResult batchBindResult;

        /**
         * Builder for {@link BatchBindResult}.
         */
        public Builder() {
            batchBindResult = new BatchBindResult();
        }

        /**
         * Adds details for a successful bind to the {@link BatchBindResult}.
         *
         * @param hardwareSerialNumber
         *            the hardware serial number for the bind
         * @param nodeName
         *            the logical node name
         * @return the {@link Builder}
         */
        public Builder withBindSuccess(final String hardwareSerialNumber, final String nodeName) {
            batchBindResult.addSuccessfulBindDetails(hardwareSerialNumber, nodeName);
            return this;
        }

        /**
         * Adds details for a failed {@link BatchBindResult}.
         *
         * @param errorMessage
         *            the error message
         * @return the {@link Builder}
         */
        public Builder withBindFailure(final String errorMessage) {
            batchBindResult.addFailureMessage(errorMessage);
            return this;
        }

        /**
         * Builds the {@link BatchBindResult}.
         *
         * @return the BatchBindResult
         */
        public BatchBindResult build() {
            return batchBindResult;
        }
    }
}
