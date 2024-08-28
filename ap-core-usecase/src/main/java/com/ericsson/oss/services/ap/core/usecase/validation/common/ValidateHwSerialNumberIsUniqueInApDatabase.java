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

import static com.ericsson.oss.services.ap.common.model.MoType.NODE;
import static com.ericsson.oss.services.ap.common.model.Namespace.AP;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ValidationCrudException;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Groups;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo;
import com.ericsson.oss.services.ap.core.usecase.validation.greenfield.ZipBasedValidation;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.ericsson.oss.services.ap.core.usecase.bind.Attributes;

/**
 * Rule to validate that the hardware serial number is not already used by an existing AP node, i.e. the hardware serial number has been used already
 * to bind a different node.
 */
@Groups(value = { @Group(name = ValidationRuleGroups.ORDER, priority = 10, abortOnFail = true),
    @Group(name = ValidationRuleGroups.HARDWARE_REPLACE, priority = 10, abortOnFail = true) })
@Rule(name = "ValidateHwSerialNumberIsUniqueInApDatabase")
class ValidateHwSerialNumberIsUniqueInApDatabase extends ZipBasedValidation {

    private static final String VALIDATION_TERMINATED_CRUD_EXCEPTION_MESSAGE = "failure.general";
    private static final String VALIDATION_FAILURE_FOR_HW_SERIAL_NUMBER_MESSAGE = "validation.hardware.serial.number.unique.in.database";
    private static final String VALIDATION_FAILURE_FOR_HW_SERIAL_NUMBER_MESSAGE_DUPLICATENODE = "validation.hardware.serial.number.unique.in.database.duplicatenode";

    @Inject
    private DpsQueries dpsQueries;

    private static String getDateFormat() {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return dateFormat.format(new Date());
    }

    @Override
    protected boolean validate(final ValidationContext context, final List<String> dirNames) {
        boolean result = true;
        for (final String dirName : dirNames) {
            result &= validateSerialNumberIsUnique(context, dirName);
        }
        return result;
    }

    private boolean validateSerialNumberIsUnique(final ValidationContext context, final String dirName) {
        final NodeInfo nodeInfo = getNodeInfo(context, dirName);
        final String nodeName = nodeInfo.getName();
        final String hardwareSerialNumber = nodeInfo.getHardwareSerialNumber();
        final String nodeType = nodeInfo.getNodeType();
        validateHwSerialNumber(context, dirName, nodeName, hardwareSerialNumber);
        validateHWSerialNoWithENMNodes(context, dirName, nodeName, hardwareSerialNumber, nodeType);
        return context.getValidationErrors().isEmpty();
    }

    private void validateHwSerialNumber(final ValidationContext context, final String dirName, final String nodeName, final String hardwareSerialNumber) {
        if (StringUtils.isBlank(hardwareSerialNumber)) {
            return;
        }

        try {
            logger.info("Request sent to dps to find MOs with same HwID from AP at : {}", getDateFormat());
            final Iterator<ManagedObject> mosWithGivenHwSerialNumber = dpsQueries.findMosWithAttributeValue(
                NodeAttribute.HARDWARE_SERIAL_NUMBER.toString(), hardwareSerialNumber, getNamespace(), getMoType()).execute();
            logger.info("Response received from AP dps at : {}", getDateFormat());

            if (mosWithGivenHwSerialNumber.hasNext()) {
                final String fdn = mosWithGivenHwSerialNumber.next().getFdn();
                final String nodeNameInAP = new FDN(fdn).getRdnValue();
                final String projectNameFromDps = new FDN(fdn).getProjectName();
                recordNodeValidationErrorBind(context, VALIDATION_FAILURE_FOR_HW_SERIAL_NUMBER_MESSAGE, dirName, hardwareSerialNumber, projectNameFromDps, nodeNameInAP);
            }

        } catch (final Exception e) {
            logger.error("Unexpected error while validating the hardware serial number uniqueness for node {}", nodeName, e);
            throw new ValidationCrudException(apMessages.get(VALIDATION_TERMINATED_CRUD_EXCEPTION_MESSAGE), e);
        }
    }

    private void validateHWSerialNoWithENMNodes(final ValidationContext context, final String dirName, final String nodeName, final String hardwareSerialNumber, final String nodeType) {

        if (StringUtils.isBlank(hardwareSerialNumber)) {
            return;
        }

        Attributes attributes = new Attributes(nodeType);
        logger.info("Namespace:{}, MoType:{}, Attribute:{} for nodetype:{} are:", attributes.getNamespace(), attributes.getMoType(), attributes.getAttribute(), nodeType);

        try {
            logger.info("Request sent to dps to find MOs with same HwID from ENM at : {}", getDateFormat());
            final Iterator<ManagedObject> mosWithGivenHwSerialNumber = dpsQueries.findMosWithAttributeValue(
                attributes.getAttribute(), hardwareSerialNumber, attributes.getNamespace(), attributes.getMoType()).execute();
            logger.info("Response received from ENM dps at : {}", getDateFormat());
            while (mosWithGivenHwSerialNumber.hasNext()) {
                final String fdn = mosWithGivenHwSerialNumber.next().getFdn();
                final String nodeNameInEnm = new FDN(fdn).getNodeName();
                logger.info("Node names with matching HwId from ENM:{}", nodeNameInEnm);
                recordNodeValidationErrorBind(context, VALIDATION_FAILURE_FOR_HW_SERIAL_NUMBER_MESSAGE_DUPLICATENODE, dirName, hardwareSerialNumber, nodeNameInEnm);
            }
        } catch (final Exception e) {
            logger.error("Unexpected error while validating the hardware serial number uniqueness for node {}", nodeName, e);
            throw new ValidationCrudException(apMessages.get(VALIDATION_TERMINATED_CRUD_EXCEPTION_MESSAGE), e);
        }
    }

    protected String getNamespace() {
        return AP.toString();
    }

    protected String getMoType() {
        return NODE.toString();
    }
}
