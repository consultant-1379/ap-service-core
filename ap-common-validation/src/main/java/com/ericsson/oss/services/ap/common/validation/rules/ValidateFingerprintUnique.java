/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.validation.rules;

import static com.ericsson.oss.services.ap.common.model.MoType.LICENSE_OPTIONS;

import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Groups;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.shm.licenseservice.remoteapi.LicenseAccessException;
import com.ericsson.oss.services.shm.licenseservice.remoteapi.LicenseFileManagerService;

/**
 * Validate the fingerprint specified in a NodeInfo file is unique to node by calling SHM
 */

@Groups(value = {@Group(name = ValidationRuleGroups.ORDER_WORKFLOW, priority = 14),
    @Group(name = ValidationRuleGroups.MIGRATION_WORKFLOW, priority = 14)})
@Rule(name = "ValidateFingerprintUnique")
public class ValidateFingerprintUnique extends NodeConfigurationsValidation {

    private static final String AUTOMATIC_LICENSE_REQUEST_OPTION = "automaticLicenseRequest";
    private static final String FINGERPRINT = "fingerprint";
    private static final String INSTALL_LICENSE = "installLicense";

    private LicenseFileManagerService licenseFileManagerService;

    @Inject
    private DpsOperations dps;

    @PostConstruct
    public void init() {
        licenseFileManagerService = new ServiceFinderBean().find(LicenseFileManagerService.class);
    }

    @Override
    protected boolean validate(final ValidationContext context) {
        final String nodeFdn = getNodeFdn(context);
        final String licenseOptionsMoFdn = String.format("%s,%s=1", nodeFdn, LICENSE_OPTIONS.toString());
        final String nodeName = FDN.get(nodeFdn).getRdnValue();

        final ManagedObject licenseOptionsMo = dps.getDataPersistenceService().getLiveBucket().findMoByFdn(licenseOptionsMoFdn);

        final boolean isMigrationUseCase = isMigrationActivity(nodeFdn);

        if (isInstallLicense(licenseOptionsMo) && (isGreenfield(nodeFdn) || isMigrationUseCase)) {
            final String fingerprint = (String) Optional.ofNullable(licenseOptionsMo.getAllAttributes().get(FINGERPRINT)).orElse(nodeName);
            final boolean autoLicenseRequestPresent = attributeExistsInMO(AUTOMATIC_LICENSE_REQUEST_OPTION, licenseOptionsMo);
            validateFingerprintUniqueness(context, fingerprint, autoLicenseRequestPresent, isMigrationUseCase, nodeName);
        }
        return isValidatedWithoutError(context);
    }

    private void validateFingerprintUniqueness(final ValidationContext context, final String fingerprint, final boolean autoLicenseRequestPresent,
                                               final boolean isMigrationUseCase, final String nodeName) {
        try {
            final String associatedNode = licenseFileManagerService.getAssociatedNode(fingerprint);
            if (associatedNode != null && !isMigrationUseCase) {
                context.addValidationError(String.format("Fingerprint %s is already associated with Node %s", fingerprint, associatedNode));
            }
            if (associatedNode == null && isMigrationUseCase) {
                context.addValidationError(String.format("Associated License file with Node %s is not present in SHM", nodeName));
            }
        } catch (final LicenseAccessException lae) {
            if (!autoLicenseRequestPresent) {
                context.addValidationError(lae.getErrorMessage());
            }
        }
    }

    private boolean isMigrationActivity(final String nodeFdn) {
        final String nodeStatusFdn = String.format("%s,%s=1", nodeFdn, MoType.NODE_STATUS.toString());
        final ManagedObject nodeStatusMo = dps.getDataPersistenceService().getLiveBucket().findMoByFdn(nodeStatusFdn);
        return nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString()).equals(State.PRE_MIGRATION_STARTED.name());
    }

    private boolean isGreenfield(final String nodeFdn) {
        final String nodeStatusFdn = String.format("%s,%s=1", nodeFdn, MoType.NODE_STATUS.toString());
        final ManagedObject nodeStatusMo = dps.getDataPersistenceService().getLiveBucket().findMoByFdn(nodeStatusFdn);
        return nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString()).equals(State.ORDER_STARTED.name());
    }

    private static boolean isInstallLicense(final ManagedObject managedObject) {
        if (managedObject != null) {
            return (boolean) Optional.ofNullable(managedObject.getAllAttributes().get(INSTALL_LICENSE)).orElse(false);
        }
        return false;
    }

    private static boolean attributeExistsInMO(final String attributeName, final ManagedObject managedObject) {
        return managedObject.getAllAttributes().get(attributeName) != null;
    }
}
