/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.validation.greenfield;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.ericsson.oss.services.ap.api.exception.ValidationCrudException;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo;
import com.ericsson.oss.services.nedo.ipaddress.validator.DuplicateIpAddressValidationResult;
import com.ericsson.oss.services.nedo.ipaddress.validator.DuplicateIpAddressValidator;
import com.ericsson.oss.services.nedo.ipaddress.validator.State;

/**
 * Validator for checking that a node IP address is unique in the system, using ENM official library's
 * {@link DuplicateIpAddressValidator}.
 */
@Group(name = ValidationRuleGroups.ORDER, priority = 10)
@Rule(name = "ValidateIPAddressUnique")
public class ValidateIPAddressUnique extends ZipBasedValidation {

    private static final Set<String> DYNAMIC_IP_ADDRESSES = new HashSet<>(6);
    private static final String VALIDATION_RULE_IP_ADDRESS_NOT_UNIQUE = "validation.ipaddress.unique.in.database";
    private static final String VALIDATION_TERMINATED_CRUD_EXCEPTION_MESSAGE = "failure.general";

    static {
        DYNAMIC_IP_ADDRESSES.add("0.0.0.0");
        DYNAMIC_IP_ADDRESSES.add("0:0:0:0:0:0:0:0");
        DYNAMIC_IP_ADDRESSES.add("::");
    }

    @Inject
    private DuplicateIpAddressValidator duplicateIpAddressValidator;

    @Override
    protected boolean validate(final ValidationContext context, final List<String> directoryNames) {
        for (final String nodeDirectoryName : directoryNames) {
            validateIpAddressUniquenessForSingleNode(context, nodeDirectoryName);
        }

        return context.getValidationErrors().isEmpty();
    }

    private void validateIpAddressUniquenessForSingleNode(final ValidationContext validationContext, final String nodeDirectoryName) {
        final NodeInfo nodeInfo = getNodeInfo(validationContext, nodeDirectoryName);
        final String ipAddress = nodeInfo.getIpAddress();

        if (isDynamicIpAddress(ipAddress)) {
            return;
        }

        final List<String> ipAddresses = new ArrayList<>(1);
        ipAddresses.add(ipAddress);

        final String nodeName = nodeInfo.getName();

        try {
            final DuplicateIpAddressValidationResult ipAddressValidationResult =
                duplicateIpAddressValidator.checkForValidIpAddress(ipAddresses, false);
            if (ipAddressValidationResult.getStatusResult() == State.DUPLICATE) {
                recordNodeValidationError(validationContext, VALIDATION_RULE_IP_ADDRESS_NOT_UNIQUE, nodeDirectoryName, ipAddress,
                    ipAddressValidationResult.getFdn());
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
