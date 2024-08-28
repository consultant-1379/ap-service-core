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
package com.ericsson.oss.services.ap.core.usecase.validation.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Groups;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo;
import com.ericsson.oss.services.ap.core.usecase.validation.greenfield.ZipBasedValidation;

/**
 * Validate the hardware serial number specified per node is unique in the project
 */
@Groups(value = { @Group(name = ValidationRuleGroups.ORDER, priority = 10, abortOnFail = true),
    @Group(name = ValidationRuleGroups.HARDWARE_REPLACE, priority = 10, abortOnFail = true) })
@Rule(name = "ValidateSerialNumberUniqueInProject")
public class ValidateHwSerialNumberUniqueInProject extends ZipBasedValidation {

    private static final String VALIDATION_FAILURE_FOR_HW_SERIAL_NUMBER_MESSAGE = "validation.duplicate.node.hardware.serial.number";

    @Override
    protected boolean validate(final ValidationContext context, final List<String> directoryNames) {
        final Collection<String> allProjectSerialNumbers = new HashSet<>();
        for (final String dirName : directoryNames) {
            final NodeInfo nodeInfo = getNodeInfo(context, dirName);
            final String hardwareSerialNumber = nodeInfo.getHardwareSerialNumber();
            validateSerialNumber(context, allProjectSerialNumbers, hardwareSerialNumber, dirName);
        }
        return context.getValidationErrors().isEmpty();
    }

    private void validateSerialNumber(final ValidationContext context, final Collection<String> allProjectSerialNumbers,
        final String hardwareSerialNumber, final String dirName) {
        if (!StringUtils.isBlank(hardwareSerialNumber)) {
            if (!allProjectSerialNumbers.add(hardwareSerialNumber)) {
                recordNodeValidationError(context, VALIDATION_FAILURE_FOR_HW_SERIAL_NUMBER_MESSAGE, dirName, hardwareSerialNumber);
            } else {
                allProjectSerialNumbers.add(hardwareSerialNumber);
            }
        }
    }
}
