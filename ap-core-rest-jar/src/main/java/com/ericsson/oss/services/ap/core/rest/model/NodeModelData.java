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
package com.ericsson.oss.services.ap.core.rest.model;

import java.util.Collections;
import java.util.List;

/**
 * Contains a list of ossModelIdentities, that are valid for a particular node type
 */
public class NodeModelData {

    List<String> ossModelIds = Collections.emptyList();

    public void setOssModelIds(final List<String> ossModelIds) {
        this.ossModelIds = ossModelIds;
    }

    public List<String> getOssModelIds() {
        return ossModelIds;
    }
}
