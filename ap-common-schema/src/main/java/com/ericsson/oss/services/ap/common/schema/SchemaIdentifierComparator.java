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

import java.io.Serializable;
import java.util.Comparator;

import com.ericsson.oss.services.ap.common.schema.cache.SchemaArtifacts;
import com.ericsson.oss.services.ap.common.util.string.MimVersionComparator;

/**
 * Comparator of the Node Identifier of {@link SchemaArtifacts} using {@link MimVersionComparator}.
 */
class SchemaIdentifierComparator implements Comparator<SchemaArtifacts>, Serializable {

    private static final long serialVersionUID = 6113110045223248942L;

    private final MimVersionComparator mimVersionComparator = new MimVersionComparator();

    @Override
    public int compare(final SchemaArtifacts first, final SchemaArtifacts second) {
        final String firstNodeIdentifier = first.getNodeIdentifier();
        final String secondNodeIdentifier = second.getNodeIdentifier();
        return mimVersionComparator.compare(firstNodeIdentifier, secondNodeIdentifier);
    }
}
