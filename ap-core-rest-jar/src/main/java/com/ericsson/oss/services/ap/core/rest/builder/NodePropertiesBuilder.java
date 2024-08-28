/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.builder;

import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.core.rest.model.nodeproperty.AttributeGroup;
import com.ericsson.oss.services.ap.core.rest.model.nodeproperty.NodeProperties;
import com.ericsson.oss.services.ap.core.rest.view.ViewNodePropertiesResponseBuilder;
import com.ericsson.oss.services.ap.core.rest.view.properties.data.ViewProperties;
import com.ericsson.oss.services.ap.core.rest.view.properties.data.ViewPropertiesResponse;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class NodePropertiesBuilder {

    @Inject
    private ViewNodePropertiesResponseBuilder nodePropertiesResponseBuilder;

    /**
     * Builds a Data Object of {@link NodeProperties} attributes
     *
     * @param moDataList
     *            List of MO Data
     * @return {@link NodeProperties} which is unmarshalled as JSON
     */
    public NodeProperties buildNodeProperties(final List<MoData> moDataList) {
        final NodeProperties nodeProperties = new NodeProperties();
        final ViewPropertiesResponse viewPropertiesResponse = nodePropertiesResponseBuilder.buildViewForNode(moDataList);
        final List<ViewProperties> viewProperties = viewPropertiesResponse.getViewProperties();
        if (viewProperties.isEmpty()) {
            throw new ApServiceException("MetaData service failed to provide View MetaData");
        }
        final ViewProperties nodeAttributes = viewProperties.remove(0);
        nodeProperties.setAttributes(nodeAttributes.getProperties());
        final List<AttributeGroup> attributeGroups = new ArrayList<>(viewProperties.size());
        for (final ViewProperties viewProperty : viewProperties) {
            attributeGroups.add(new AttributeGroup(viewProperty.getType(), viewProperty.getProperties()));
        }
        nodeProperties.setAttributeGroups(attributeGroups);
        return nodeProperties;
    }
}
