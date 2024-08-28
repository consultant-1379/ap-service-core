/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.ap.core.usecase.validation.eoi;

import com.ericsson.oss.services.ap.api.exception.ValidationCrudException;
import com.ericsson.oss.services.ap.api.model.eoi.ProjectRequestAttributes;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.nedo.ipaddress.validator.DuplicateIpAddressValidationResult;
import com.ericsson.oss.services.nedo.ipaddress.validator.DuplicateIpAddressValidator;
import com.ericsson.oss.services.nedo.ipaddress.validator.State;

import javax.inject.Inject;
import java.util.*;

@Group(name = ValidationRuleGroups.EOI, priority = 6, abortOnFail = true)
@Rule(name = "EoiValidateIpAddressUnique")
public class EoiValidateIpAddressUnique extends EoiBasedValidation {


    private static final Set<String> DYNAMIC_IP_ADDRESSES = new HashSet<>(6);
    private static final String VALIDATION_TERMINATED_CRUD_EXCEPTION_MESSAGE = "failure.general";

    static {
        DYNAMIC_IP_ADDRESSES.add("0.0.0.0");
        DYNAMIC_IP_ADDRESSES.add("0:0:0:0:0:0:0:0");
        DYNAMIC_IP_ADDRESSES.add("::");
    }

    @Inject
    private DuplicateIpAddressValidator duplicateIpAddressValidator;

    @Override
    protected boolean validate(final ValidationContext context, final List<Map<String, Object>> networkElements) {
        for (final Map<String, Object> networkElement : networkElements) {
            validateIpAddressUniquenessForSingleNode(context, networkElement);
        }

        return context.getValidationErrors().isEmpty();
    }

    private void validateIpAddressUniquenessForSingleNode(final ValidationContext context, final Map<String, Object> networkElement) {
        final String ipAddress = (String) networkElement.get(ProjectRequestAttributes.IPADDRESS.toString());
        if (isDynamicIpAddress(ipAddress)) {
            return;
        }

        final List<String> ipAddresses = new ArrayList<>(1);
        ipAddresses.add(ipAddress);

        final String nodeName = (String) networkElement.get(ProjectRequestAttributes.NODE_NAME.toString());//nodeName

        try {
            final DuplicateIpAddressValidationResult ipAddressValidationResult =
                duplicateIpAddressValidator.checkForValidIpAddress(ipAddresses, false);
            if (ipAddressValidationResult.getStatusResult() == State.DUPLICATE) {
                context.addValidationError(String.format("IP address %s is already in use (found in %s)", ipAddress, ipAddressValidationResult.getFdn()));
            }
        } catch (final Exception e) {
            logger.error("Unexpected error while validating the IP address uniqueness for node {}", nodeName, e);
            throw new ValidationCrudException(apMessages.get(VALIDATION_TERMINATED_CRUD_EXCEPTION_MESSAGE), e);
        }
    }

    private static boolean isDynamicIpAddress(final String ipAddress) {
        return DYNAMIC_IP_ADDRESSES.contains(ipAddress);
    }
}
