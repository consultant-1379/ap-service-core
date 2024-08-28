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

import com.ericsson.oss.services.ap.api.model.eoi.ProjectRequestAttributes;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;

import java.util.*;

@Group(name = ValidationRuleGroups.EOI, priority = 7, abortOnFail = true)
@Rule(name = "EoiValidateIpAddressUniquePerNode")
public class EoiValidateIpAddressUniquePerNode extends EoiBasedValidation {

    private static final Set<String> DYNAMIC_IP_ADDRESSES = new HashSet<>(6);

    static {
        DYNAMIC_IP_ADDRESSES.add("0.0.0.0");
        DYNAMIC_IP_ADDRESSES.add("0:0:0:0:0:0:0:0");
        DYNAMIC_IP_ADDRESSES.add("::");
    }

    @Override
    protected boolean validate(final ValidationContext context, final List<Map<String, Object>> networkElements) {
        final Collection<String> ipAddresses = new HashSet<>();

        for (final Map<String, Object> networkElement : networkElements) {
            final String ipAddress = (String) networkElement.get(ProjectRequestAttributes.IPADDRESS.toString());
            processIpAddress(context, ipAddresses, ipAddress);
        }

        return context.getValidationErrors().isEmpty();
    }

    private void processIpAddress(final ValidationContext context, final Collection<String> ipAddresses, final String ipAddress) {
        if (!isDynamicIpAdress(ipAddress) && !ipAddresses.add(ipAddress)) {
            context.addValidationError(String.format("Duplicate IP address %s", ipAddress));
        }
    }

    private static boolean isDynamicIpAdress(final String ipAddress) {
        return DYNAMIC_IP_ADDRESSES.contains(ipAddress);
    }
}
