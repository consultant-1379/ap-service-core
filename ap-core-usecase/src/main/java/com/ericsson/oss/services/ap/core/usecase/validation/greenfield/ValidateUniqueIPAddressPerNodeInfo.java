/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.validation.greenfield;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;

/**
 * Validate IP address specified per node is unique in the project. Multiple '0' IP addresses are allowed as these signify that the IP address will be
 * set dynamically.
 */
@Group(name = ValidationRuleGroups.ORDER, priority = 10)
@Rule(name = "ValidateUniqueIPAddressPerNodeInfo")
public class ValidateUniqueIPAddressPerNodeInfo extends ZipBasedValidation {

    private static final Set<String> DYNAMIC_IP_ADDRESSES = new HashSet<>(6);

    static {
        DYNAMIC_IP_ADDRESSES.add("0.0.0.0");
        DYNAMIC_IP_ADDRESSES.add("0:0:0:0:0:0:0:0");
        DYNAMIC_IP_ADDRESSES.add("::");
    }

    @Override
    protected boolean validate(final ValidationContext context, final List<String> directoryNames) {
        final Collection<String> ipAddresses = new HashSet<>();

        for (final String dirName : directoryNames) {
            final NodeInfo nodeInfo = getNodeInfo(context, dirName);
            final String ipAddress = nodeInfo.getIpAddress();
            processIpAddress(context, ipAddresses, ipAddress, dirName);
        }

        return context.getValidationErrors().isEmpty();
    }

    private void processIpAddress(final ValidationContext context, final Collection<String> ipAddresses, final String ipAddress,
            final String dirName) {
        if (!isDynamicIpAdress(ipAddress) && !ipAddresses.add(ipAddress)) {
            recordNodeValidationError(context, "validation.duplicate.node.ipaddress", dirName, ipAddress);
        }
    }

    private static boolean isDynamicIpAdress(final String ipAddress) {
        return DYNAMIC_IP_ADDRESSES.contains(ipAddress);
    }
}
