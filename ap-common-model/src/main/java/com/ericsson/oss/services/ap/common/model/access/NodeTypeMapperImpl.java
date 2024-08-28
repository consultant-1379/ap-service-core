/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.model.access;

import static com.ericsson.oss.itpf.modeling.modelservice.typed.core.target.TargetTypeInformation.CATEGORY_NODE;
import static com.ericsson.oss.services.ap.common.model.Namespace.AP;
import static java.lang.String.format;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.modeling.modelservice.ModelService;
import com.ericsson.oss.itpf.modeling.modelservice.exception.UnknownModelException;
import com.ericsson.oss.itpf.modeling.modelservice.typed.capabilities.CapabilityInformation;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;

/**
 * Provides mappings from node type to Auto Provisioning internal representation.
 */
public class NodeTypeMapperImpl implements NodeTypeMapper {

    private static final String DEFAULT_INTERNAL_TYPE = "ecim";
    private static final String VNF_INTERNAL_TYPE = "vnf";
    private static final int INTERNAL_REPRESENTATION = 3;

    private static final String AP_MODEL_NAME = "AP";
    private static final String AP_CAPABILITY_NAME = "apMappedNamespace";
    private static final String AP_VERSION = "1.0.0";
    private static final String FH_NODE_TYPE_IN_AP = "FRONTHAUL6000";
    private static final String FH_NODE_TYPE_IN_OSS = "FRONTHAUL-6000";
    private static final String R6K2_NODE_TYPE_IN_AP = "Router60002";
    private static final String R6K2_NODE_TYPE_IN_OSS = "Router6000-2";
    private static final String SHARED_CNF_NODE_TYPE_IN_AP = "SharedCNF";
    private static final String SHARED_CNF_NODE_TYPE_IN_OSS = "Shared-CNF";

    private static final String VIRTUALIZATION_MODEL_NAME = "ned-common";
    private static final String VIRTUALIZATION_CAPABILITY_NAME = "supportsVirtualization";
    private static final String VIRTUALIZATION_VERSION = "1.0.0";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private ModelService modelService;

    @Override
    public String getInternalEjbQualifier(final String nodeType) {
        final String internalEjbQualifier = getInternalRepresentationFor(nodeType);
        return DEFAULT_INTERNAL_TYPE.equals(internalEjbQualifier) ? getInternalRepresentationForDefault(nodeType) : internalEjbQualifier;
    }

    @Override
    public String getInternalRepresentationFor(final String nodeType) {
        try {
            final CapabilityInformation capabilityInfo = modelService.getTypedAccess().getModelInformation(CapabilityInformation.class);
            final String namespace = (String) capabilityInfo.getCapabilityValue(CATEGORY_NODE, toOssRepresentation(nodeType),
                AP_MODEL_NAME, AP_CAPABILITY_NAME, AP_VERSION);

            if (!StringUtils.isBlank(namespace)) {
                return namespace.substring(INTERNAL_REPRESENTATION);
            }
            return DEFAULT_INTERNAL_TYPE;
        } catch (final UnknownModelException e) {
            logger.debug("Unable to retrieve model for type {}", nodeType, e);
            return DEFAULT_INTERNAL_TYPE;
        }
    }

    private String getInternalRepresentationForDefault(final String nodeType) {
        final CapabilityInformation capabilityInfo = modelService.getTypedAccess().getModelInformation(CapabilityInformation.class);
        final boolean supportsVirtualization = (boolean) capabilityInfo.getCapabilityValue(CATEGORY_NODE, toOssRepresentation(nodeType),
            VIRTUALIZATION_MODEL_NAME, VIRTUALIZATION_CAPABILITY_NAME, VIRTUALIZATION_VERSION);
        return supportsVirtualization ? VNF_INTERNAL_TYPE : DEFAULT_INTERNAL_TYPE;
    }

    @Override
    public String getNamespace(final String nodeType) {
        final String internalNodeType = getInternalRepresentationFor(nodeType);
        return buildApNamespace(internalNodeType);
    }

    private static String buildApNamespace(final String internalNodeType) {
        return format("%s_%s", AP.toString(), internalNodeType);
    }

    @Override
    public String toOssRepresentation(final String nodeType) {
        if (FH_NODE_TYPE_IN_AP.equals(nodeType)) {
            return FH_NODE_TYPE_IN_OSS;
        }
        if (R6K2_NODE_TYPE_IN_AP.equals(nodeType)) {
            return R6K2_NODE_TYPE_IN_OSS;
        }
        if(SHARED_CNF_NODE_TYPE_IN_AP.equals(nodeType)){
            return SHARED_CNF_NODE_TYPE_IN_OSS;
        }
        else {
            return nodeType;
        }
    }

    @Override
    public String toApRepresentation(final String nodeType) {
        if (FH_NODE_TYPE_IN_OSS.equals(nodeType)) {
            return FH_NODE_TYPE_IN_AP;
        }
        if (R6K2_NODE_TYPE_IN_OSS.equals(nodeType)) {
            return R6K2_NODE_TYPE_IN_AP;
        }
        if(SHARED_CNF_NODE_TYPE_IN_OSS.equals(nodeType)){
            return SHARED_CNF_NODE_TYPE_IN_AP;
        }
        else {
            return nodeType;
        }
    }
}
