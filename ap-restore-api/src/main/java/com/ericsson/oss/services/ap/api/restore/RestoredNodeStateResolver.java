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
package com.ericsson.oss.services.ap.api.restore;

import com.ericsson.oss.services.ap.api.status.State;

/**
 * Workflows must implement this interface so that the appropriate node state can be set when a restore from a backup has completed.
 */
public interface RestoredNodeStateResolver {

    /**
     * Get the state of the node after restore has completed.
     *
     * @param apNodeFdn
     *            the node whose state is required
     * @return the node state
     */
    State resolveNodeState(final String apNodeFdn);
}
