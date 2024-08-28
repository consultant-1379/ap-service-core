/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.util.log;

import java.util.HashMap;
import java.util.Map;

import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.itpf.sdk.recording.classic.SystemRecorderNonCDIImpl;

/**
 * This class uses SystemRecorder to identify when functionality belonging to an MR has been used.
 */
public class MRExecutionRecorder {

    private final SystemRecorder recorder = new SystemRecorderNonCDIImpl(); // NOPMD

    /**
     * Records event in DDP for MR statistics.
     *
     * @param mrId
     *            the id of the main requirement
     */
    public void recordMRExecution(final MRDefinition mrId) {
        final Map<String, Object> data = new HashMap<>();
        data.put("MR", mrId.toString());
        recorder.recordEventData("MR.EXECUTION", data);
    }

}
