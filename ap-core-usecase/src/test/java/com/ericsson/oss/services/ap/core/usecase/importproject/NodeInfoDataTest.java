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
package com.ericsson.oss.services.ap.core.usecase.importproject;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for {@link NodeInfoData}.
 */
public class NodeInfoDataTest {

    private static final String VALID_NODE_INFO = "<nodeInfo><name>" + NODE_NAME + "</name></nodeInfo>";

    @Test
    public void whenGetNodeNameThenNameIsReturned() {
        final NodeInfoData nodeInfoData = new NodeInfoData(VALID_NODE_INFO);
        final String result = nodeInfoData.getNodeName();
        assertEquals(NODE_NAME, result);
    }

    @Test
    public void whenGetNodeNameAndNameHasAlreadyBeenRetrievedThenSameNameIsReturned() {
        final NodeInfoData nodeInfoData = new NodeInfoData(VALID_NODE_INFO);
        nodeInfoData.getNodeName();
        final String result = nodeInfoData.getNodeName();
        assertEquals(NODE_NAME, result);
    }
}