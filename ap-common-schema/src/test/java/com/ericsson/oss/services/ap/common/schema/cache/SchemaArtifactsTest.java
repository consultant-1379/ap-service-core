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
package com.ericsson.oss.services.ap.common.schema.cache;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_IDENTIFIER_VALUE;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.VALID_NODE_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

/**
 * Units tests for {@code SchemaArtifacts}.
 */
public class SchemaArtifactsTest {

    @Test
    public void whenNodeTypeAndIdentifierSameEqualsReturnsTrue() {
        final SchemaArtifacts firstInstance = new SchemaArtifacts(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE);
        final SchemaArtifacts secondInstance = new SchemaArtifacts(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE);
        assertEquals(firstInstance, secondInstance);
    }

    @Test
    public void whenNodeTypeDiffersEqualsReturnsTrue() {
        final SchemaArtifacts firstInstance = new SchemaArtifacts(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE);
        final SchemaArtifacts secondInstance = new SchemaArtifacts("invalidNodeType", NODE_IDENTIFIER_VALUE);
        assertNotEquals(firstInstance, secondInstance);
    }

    @Test
    public void whenNodeIdentifierDiffersEqualsReturnsFalse() {
        final SchemaArtifacts firstInstance = new SchemaArtifacts(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE);
        final SchemaArtifacts secondInstance = new SchemaArtifacts(VALID_NODE_TYPE, "differentNodeIdentifierValue");
        assertNotEquals(firstInstance, secondInstance);
    }

    @Test
    public void whenSameInstanceEqualsReturnsTrue() {
        final SchemaArtifacts firstInstance = new SchemaArtifacts(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE);
        final SchemaArtifacts secondInstance = firstInstance;
        assertEquals(firstInstance, secondInstance);
    }

    @Test
    public void whenNotSameTypeEqualsReturnsFalse() {
        final SchemaArtifacts firstInstance = new SchemaArtifacts(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE);
        final Object secondInstance = new Object();
        assertNotEquals(firstInstance, secondInstance);
    }
}
