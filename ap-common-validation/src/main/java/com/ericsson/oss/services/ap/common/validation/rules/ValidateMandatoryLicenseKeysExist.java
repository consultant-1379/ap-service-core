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
package com.ericsson.oss.services.ap.common.validation.rules;

import static com.ericsson.oss.services.ap.common.model.MoType.LICENSE_OPTIONS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactDetails;
import com.ericsson.oss.services.ap.common.artifacts.raw.RawArtifactHandler;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.ericsson.oss.services.ap.common.util.xml.DocumentReader;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.shm.licenseservice.remoteapi.LicenseAccessException;
import com.ericsson.oss.services.shm.licenseservice.remoteapi.LicenseFileManagerService;
import com.ericsson.oss.services.shm.licenseservice.remoteapi.LicenseValidationException;
import com.ericsson.oss.services.shm.licenseservice.remoteapi.ValidationFailure;

/**
 * Validate the mandatory license keys specified per node exists.
 */
@Group(name = ValidationRuleGroups.ORDER_WORKFLOW, priority = 13, abortOnFail = true)
@Rule(name = "ValidateMandatoryLicenseKeysExist")
public class ValidateMandatoryLicenseKeysExist extends NodeConfigurationsValidation {

    private static final String AUTOMATIC_LICENSE_REQUEST_OPTION = "automaticLicenseRequest";
    private static final String MANDATORY_LICENSE_KEYS_LICENSE_OPTION = "mandatoryLicenseKeys";
    private static final String FINGERPRINT = "fingerprint";

    private LicenseFileManagerService licenseFileManagerService;

    @Inject
    private DpsOperations dps;

    @Inject
    private RawArtifactHandler rawArtifactHandler;

    @PostConstruct
    public void init() {
        licenseFileManagerService = new ServiceFinderBean().find(LicenseFileManagerService.class);
    }

    @Override
    protected boolean validate(final ValidationContext context) {
        final String nodeFdn = getNodeFdn(context);
        final String licenseOptionsMoFdn = nodeFdn + "," + LICENSE_OPTIONS.toString() + "=1";
        final String nodeName = FDN.get(nodeFdn).getRdnValue();
        final ManagedObject licenseOptionsMo = dps.getDataPersistenceService().getLiveBucket().findMoByFdn(licenseOptionsMoFdn);
        if(licenseOptionsMo != null){
            if(checkAttributeExistsInMO(AUTOMATIC_LICENSE_REQUEST_OPTION, licenseOptionsMo))
                return true;
            if (checkAttributeExistsInMO(MANDATORY_LICENSE_KEYS_LICENSE_OPTION, licenseOptionsMo)) {
                final String licenseKeysFileName = licenseOptionsMo.getAttribute(MANDATORY_LICENSE_KEYS_LICENSE_OPTION);
                if (licenseKeysFileName != null) {
                    final List<String> licenseKeysToCheck = getLicenseKeysToBeValidated(nodeFdn);
                    final String fingerprint = (String) Optional.ofNullable(licenseOptionsMo.getAllAttributes().get(FINGERPRINT)).orElse(nodeName);
                    validateLicenseKeys(context, fingerprint, licenseKeysToCheck);
                }
            }
        }
        return isValidatedWithoutError(context);
    }

    private void validateLicenseKeys(final ValidationContext context, final String nodeFingerprint,
            final List<String> licenseKeysToCheck) {
        try {
            licenseFileManagerService.validateLicenseKeys(nodeFingerprint, licenseKeysToCheck);
        } catch (final LicenseAccessException e) {
            logger.debug(e.toString());
            context.addValidationError(e.getErrorMessage());
        } catch (final LicenseValidationException e) {
            logger.debug(e.toString());
            recordValidationFailures(context, e.getFailingLicenseKeys());
        }
    }

    private static void recordValidationFailures(final ValidationContext context,
            final Map<ValidationFailure, Set<String>> failingLicenseKeys) {
        for (final Entry<ValidationFailure, Set<String>> entry : failingLicenseKeys.entrySet()) {
            final Set<String> failingLicenseKeyNames = entry.getValue();
            if (!failingLicenseKeyNames.isEmpty()) {
                final String errorMessage = String.format("%s: [%s]", entry.getKey(), StringUtils.join(entry.getValue(), ','));
                context.addValidationError(errorMessage);
            }
        }
    }

    private List<String> getLicenseKeysToBeValidated(final String nodeFdn) {
        final Collection<ArtifactDetails> lic = rawArtifactHandler.readAllOfType(nodeFdn, "MandatoryLicenseKeys");
        final ArtifactDetails licenseKeysFile = lic.iterator().next();

        final DocumentReader licenseKeysFileDocument = new DocumentReader(licenseKeysFile.getArtifactContent());
        final Collection<Element> licenseKeys = licenseKeysFileDocument.getAllElements("licenseKey");

        final Set<String> licenseKeysToBeValidated = new HashSet<>();

        for (final Element licenseKey : licenseKeys) {
            licenseKeysToBeValidated.add(licenseKey.getTextContent());
        }
        return new ArrayList<>(licenseKeysToBeValidated);
    }

    private static boolean checkAttributeExistsInMO(final String attributeName, final ManagedObject managedObject) {
        return managedObject.getAllAttributes()
                .get(attributeName) != null;
    }
}
