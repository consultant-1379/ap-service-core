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
package com.ericsson.oss.services.ap.common.workflow;

import com.ericsson.oss.services.ap.common.util.string.FDN;

/**
 * Generates the Business Key used to identify a workflow instance. Each workflow should have a unique business key.
 */
public final class BusinessKeyGenerator {

    private static final String BUSINESS_KEY_PREFIX = "AP_Node";

    private BusinessKeyGenerator() {

    }

    /**
     * Generates the business key for a node from its name.
     *
     * @param nodeName
     *            the name of the node
     * @return the generated business key
     */
    public static String generateBusinessKeyFromNodeName(final String nodeName) {
        return String.format("%s=%s", BUSINESS_KEY_PREFIX, nodeName);
    }

    /**
     * Generates the business key for a node from its FDN.
     *
     * @param nodeFdn
     *            the name of the node
     * @return the generated business key
     */
    public static String generateBusinessKeyFromFdn(final String nodeFdn) {
        final String nodeName = FDN.get(nodeFdn).getRdnValue();
        return generateBusinessKeyFromNodeName(nodeName);
    }
}
