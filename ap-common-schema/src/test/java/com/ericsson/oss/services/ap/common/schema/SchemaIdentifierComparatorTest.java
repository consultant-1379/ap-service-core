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
package com.ericsson.oss.services.ap.common.schema;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.VALID_NODE_TYPE;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.ericsson.oss.services.ap.common.schema.cache.SchemaArtifacts;

/**
 * Units tests for {@code SchemaIdentifierComparator}.
 */
public class SchemaIdentifierComparatorTest {

    private static final SchemaArtifacts LATEST_SCHEMA = new SchemaArtifacts(VALID_NODE_TYPE, "e.1.200");
    private static final SchemaArtifacts OLDEST_SCHEMA = new SchemaArtifacts(VALID_NODE_TYPE, "d.1.44");

    private final SchemaIdentifierComparator comparator = new SchemaIdentifierComparator();

    @Test
    public void whenComparingSchemaToOlderSchemaThenPositiveResultIsReturned() {
        final int result = comparator.compare(LATEST_SCHEMA, OLDEST_SCHEMA);
        assertEquals(1, result);
    }

    @Test
    public void whenComparingSchemaToNewerSchemaThenNegativeResultIsReturned() {
        final int result = comparator.compare(OLDEST_SCHEMA, LATEST_SCHEMA);
        assertEquals(-1, result);
    }

    @Test
    public void whenComparingSchemaToSameSchemaThenZeroResultIsReturned() {
        final int result = comparator.compare(OLDEST_SCHEMA, OLDEST_SCHEMA);
        assertEquals(0, result);
    }
}
