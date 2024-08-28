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
package com.ericsson.oss.services.ap.common.model.access;

import java.util.Arrays;

import com.ericsson.oss.itpf.modeling.modelservice.ModelService;
import com.ericsson.oss.itpf.modeling.modelservice.ModelServiceImpl;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.target.MimMappedTo;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.target.TargetTypeInformation;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.target.TargetTypeVersionInformation;
import com.ericsson.oss.mediation.modeling.tools.networkmodelidentityconverter.NetworkModelIdentityConverter;
import com.ericsson.oss.mediation.modeling.tools.networkmodelidentityconverter.NetworkModelIdentityConverterImpl;

/**
 * Provides mappings from node version to ENM model versions.
 */
public class ModelVersionMapper {

    private final NetworkModelIdentityConverter networkModelIdentityConverter = new NetworkModelIdentityConverterImpl();

    private ModelService modelService;

    /**
     * Checks if the node version is valid.
     *
     * @param modelName
     *            the model name
     * @param nodeType
     *            the node type
     * @param nodeVersion
     *            the node version
     * @return true if the node version is valid
     */
    public boolean isNodeVersionValid(final String modelName, final String nodeType, final String nodeVersion) {
        final String eModelVersion = nodeVersionToEnmVersion(modelName, nodeVersion);
        final TargetTypeInformation targetTypeInformation = getModelService().getTypedAccess().getModelInformation(TargetTypeInformation.class);
        final TargetTypeVersionInformation versionInformation = targetTypeInformation.getTargetTypeVersionInformation(
                TargetTypeInformation.CATEGORY_NODE, nodeType);

        for (final String targetModelIdentity : versionInformation.getTargetModelIdentities()) {
            for (final MimMappedTo mimMappedTo : versionInformation.getMimsMappedTo(targetModelIdentity)) {
                if (mimMappedTo.getVersion().equals(eModelVersion)) {
                    return true;
                }
            }
        }

        return false;
    }

    private String nodeVersionToEnmVersion(final String modelName, final String nodeVersion) {
        final String[] mimVersionParts = splitMimVersionIntoFourParts(nodeVersion);

        final String mimName = modelName + "_" + mimVersionParts[0].toUpperCase();
        final String mimVersion = mimVersionParts[1];
        final String mimRelease = mimVersionParts[2];
        final String mimCorrection = mimVersionParts[3];

        return networkModelIdentityConverter.getEModelVersion(mimName, mimVersion, mimRelease, mimCorrection);
    }

    private static String[] splitMimVersionIntoFourParts(final String mimVersion) {
        final String[] mimVersionParts = mimVersion.split("\\.");
        return Arrays.copyOf(mimVersionParts, 4);
    }

    private ModelService getModelService() {
        if (modelService == null) {
            modelService = new ModelServiceImpl();
        }
        return modelService;
    }
}
