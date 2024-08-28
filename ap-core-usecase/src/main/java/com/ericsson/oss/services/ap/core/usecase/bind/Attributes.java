/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.bind;

import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.Namespace;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.core.usecase.mapper.NodetypePlatformMapper;

public class Attributes {

    private String namespace;
    private String moType;
    private String attribute;

    public Attributes(final String nodeType) {

        final String ER6000 = "ER6000";
        final String ECIM = "ECIM";
        final String CPP = "CPP";
        final String RBS = "RBS";
        final String FRONTHAUL6000 = "FRONTHAUL-6000";


        if (NodetypePlatformMapper.getPlatformFromNodeType(nodeType).equalsIgnoreCase(ECIM)) {
            // ECIM & Controller & FH
            this.namespace = Namespace.RCS_HW_IM.toString();
            this.moType = MoType.HW_ITEM.toString();
            this.attribute = NodeAttribute.SERIAL_NUMBER.toString();
        } else if (NodetypePlatformMapper.getPlatformFromNodeType(nodeType).equalsIgnoreCase(CPP)) {
            // CPP
            this.namespace = Namespace.ERBS_NODE_MODEL.toString();
            if (nodeType.equalsIgnoreCase(RBS)) {
                this.namespace = Namespace.RBS_NODE_MODEL.toString();
            }
            this.moType = MoType.HW_UNIT.toString();
            this.attribute = NodeAttribute.PRODUCT_DATA_SERIAL_NUMBER.toString();
        } else if (NodetypePlatformMapper.getPlatformFromNodeType(nodeType).equalsIgnoreCase(ER6000)) {
            // Router
            this.namespace = Namespace.IPR_HW_IM.toString();
            this.moType = MoType.HW_ITEM.toString();
            this.attribute = NodeAttribute.SERIAL_NUMBER.toString();
        }else if (NodetypePlatformMapper.getPlatformFromNodeType(nodeType).equalsIgnoreCase(FRONTHAUL6000)) {
            // Fronthaul6000
            this.namespace = Namespace.OPTO_HW_IM.toString();
            this.moType = MoType.HW_ITEM.toString();
            this.attribute = NodeAttribute.SERIAL_NUMBER.toString();
        }

    }

    public String getNamespace() {
        return namespace;
    }

    public String getMoType() {
        return moType;
    }

    public String getAttribute() {
        return attribute;
    }

}