/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.client.instantaneouslicensing;

import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.entity.ContentType;

import java.util.Optional;
import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.InstantaneousLicensingRestServiceException;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.core.rest.client.instantaneouslicensing.model.CreateLicenseRequestDto;

/**
 * Builds REST Data objects for REST requests in the Instantaneous Licensing flow
 */
public class InstantaneousLicensingRestDataBuilder {

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    private DpsOperations dpsOperations;

    private static final String FINGERPRINT = "fingerprint";
    private static final String SOFTWARE_LICENSE_TARGET_ID = "softwareLicenseTargetId";
    private static final String SOFTWARE_PACKAGE_NAME = "upgradePackageName";
    private static final String HARDWARE_TYPE = "hardwareType";
    private static final String RADIO_ACCESS_TECHNOLOGIES = "radioAccessTechnologies";
    private static final String GROUP_ID = "groupId";

    /**
     * Create a license request object to be sent over REST, attributes for the request need to be read from
     * the LicenseOptions MO and the AutoIntegrationOptions MO
     *
     * @param apNodeFdn
     *              fdn of the node
     * @return HttpEntity object to be sent over REST to create a license request
     */
    protected HttpEntity buildCreateLicenseRequest(final String apNodeFdn) {
        final CreateLicenseRequestDto requestObject = new CreateLicenseRequestDto();
        final ManagedObject licenseOptionsMo = dpsOperations.getDataPersistenceService().getLiveBucket().findMoByFdn(apNodeFdn + ",LicenseOptions=1");
        final String fingerPrintValue = (String) Optional.ofNullable(licenseOptionsMo.getAttribute(FINGERPRINT)).orElse(FDN.get(apNodeFdn).getRdnValue());
        requestObject.setFingerprint(fingerPrintValue);
        requestObject.setSwltId(licenseOptionsMo.getAttribute(SOFTWARE_LICENSE_TARGET_ID));
        requestObject.setHardwareType(licenseOptionsMo.getAttribute(HARDWARE_TYPE));
        requestObject.setRadioAccessTechnologies(licenseOptionsMo.getAttribute(RADIO_ACCESS_TECHNOLOGIES));
        requestObject.setGroupId(licenseOptionsMo.getAttribute(GROUP_ID));

        final ManagedObject autoIntegrationMo = getMoByFdn(apNodeFdn + ",AutoIntegrationOptions=1");
        requestObject.setSoftwarePackageName(autoIntegrationMo.getAttribute(SOFTWARE_PACKAGE_NAME));

        return buildHttpRequest(requestObject);
    }

    private ManagedObject getMoByFdn(final String moFdn) {
        return dpsOperations.getDataPersistenceService().getLiveBucket().findMoByFdn(moFdn);
    }

    private HttpEntity buildHttpRequest(final CreateLicenseRequestDto requestObject) {
        try {
            final String requestBody = objectMapper.writeValueAsString(requestObject);
            final EntityBuilder entityBuilder = EntityBuilder.create();
            entityBuilder.setContentType(ContentType.APPLICATION_JSON);
            entityBuilder.setText(requestBody);
            return entityBuilder.build();
        } catch (JsonProcessingException e) {
            throw new InstantaneousLicensingRestServiceException("Error creating HTTP Request: " + e.getMessage(), e);
        }
    }
}
