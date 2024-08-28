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
package com.ericsson.oss.services.ap.core.usecase.importproject;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.modeling.modelservice.exception.UnknownModelException;
import com.ericsson.oss.services.ap.api.model.ModelData;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;
import com.ericsson.oss.services.ap.core.usecase.view.AutomaticLicenseRequestData;

/**
 * Creates the <code>LicenseOptions</code> MO from the input {@link NodeInfo}.
 */
public class LicenseOptionsMoCreator {

    private static final String GROUP_ID = "groupId";
    private static final String HARDWARE_TYPE = "hardwareType";
    private static final String RADIO_ACCESS_TECHNOLOGIES = "radioAccessTechnologies";
    private static final String SOFTWARE_LICENSE_TARGET_ID = "softwareLicenseTargetId";

    @Inject
    private ModelReader modelReader;

    @Inject
    private NodeTypeMapper nodeTypeMapper;

    @Inject
    private DpsOperations dpsOperations;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Creates an {@link MoType#LICENSE_OPTIONS} MO to AP model with attributes read from the supplied {@link NodeInfo}.
     *
     * @param nodeMo
     *            the AP node MO
     * @param nodeData
     *            the node data from nodeInfo.xml
     */
    public void create(final ManagedObject nodeMo, final NodeInfo nodeData) {
        try {
            createLicenseOptionsMo(nodeMo, nodeData);
        } catch (final UnknownModelException e) {
            logger.debug("LicenseOptions Model not deployed for node type {}: {}", nodeData.getNodeType(), e.getMessage(), e);
        }
    }

    private void createLicenseOptionsMo(final ManagedObject nodeMo, final NodeInfo nodeData) {
        final String apNamespace = nodeTypeMapper.getNamespace(nodeData.getNodeType());
        final ModelData licenseOptionsModelData = modelReader.getLatestPrimaryTypeModel(apNamespace, MoType.LICENSE_OPTIONS.toString());

        final Map<String, Object> licenseAttributes = nodeData.getLicenseAttributes();
        final AutomaticLicenseRequestData automaticLicenseRequestData = nodeData.getAutomaticLicenseReqAttributes();

        if (automaticLicenseRequestData != null) {
            final Map<String, Object> automaticLicenseRequestFileAttributes = new HashMap<>();
            automaticLicenseRequestFileAttributes.put(GROUP_ID, automaticLicenseRequestData.getGroupId());
            automaticLicenseRequestFileAttributes.put(HARDWARE_TYPE, automaticLicenseRequestData.getHardwareType());
            automaticLicenseRequestFileAttributes.put(RADIO_ACCESS_TECHNOLOGIES, automaticLicenseRequestData.getRadioAccessTechnologies());
            automaticLicenseRequestFileAttributes.put(SOFTWARE_LICENSE_TARGET_ID, automaticLicenseRequestData.getSoftwareLicenseTargetId());
            licenseAttributes.putAll(automaticLicenseRequestFileAttributes);
        }

        dpsOperations.getDataPersistenceService().getLiveBucket()
            .getMibRootBuilder()
            .parent(nodeMo)
            .namespace(apNamespace)
            .version(licenseOptionsModelData.getVersion())
            .type(MoType.LICENSE_OPTIONS.toString())
            .name("1")
            .addAttributes(licenseAttributes)
            .create();
    }
}
