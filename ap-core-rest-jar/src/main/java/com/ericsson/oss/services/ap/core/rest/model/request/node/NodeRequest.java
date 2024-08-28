/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.model.request.node;

import java.util.List;

public class NodeRequest {

    private List<String> nodeIds;

    /**
     * @return the nodeIds
     */
    public List<String> getNodeIds() {
        return nodeIds;
    }

    /**
     * @param nodeIds the nodeIds to set
     */
    public void setNodeIds(List<String> nodeIds) {
        this.nodeIds = nodeIds;
    }

}
