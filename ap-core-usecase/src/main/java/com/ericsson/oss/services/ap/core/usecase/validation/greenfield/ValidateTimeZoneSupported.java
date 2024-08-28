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
package com.ericsson.oss.services.ap.core.usecase.validation.greenfield;

import static com.ericsson.oss.itpf.modeling.schema.util.SchemaConstants.OSS_EDT;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import com.ericsson.oss.itpf.modeling.modelservice.typed.core.edt.EnumDataTypeSpecification;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfoReader;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;

/**
 * Rule to validate that the <i>timeZone</i> in an AP <code>Node</code> is an official supported TimeZone according to the <code>NetworkElement</code>
 * model.
 */
@Group(name = ValidationRuleGroups.ORDER, priority = 14, abortOnFail = true)
@Rule(name = "ValidateTimeZoneSupported")
public class ValidateTimeZoneSupported extends ZipBasedValidation {

    private static final String VALIDATION_ATTRIBUTE_NOT_SUPPORTED_ERROR = "validation.project.zip.file.node.attribute.not.supported";
    private static final String TIMEZONE_DATA_TYPE = "TimeZone";
    private static final String NAMESPACE_OSS_NE_DEF = "OSS_NE_DEF";

    @Inject
    private ModelReader modelReader;

    @Inject
    private NodeInfoReader nodeInfoReader;

    @Override
    protected boolean validate(final ValidationContext context, final List<String> directoryNames) {
        final Collection<String> supportedTimeZones = getSupportedTimeZones();

        for (final String directoryName : directoryNames) {
            final NodeInfo nodeInfo = nodeInfoReader.read(getArchive(context), directoryName);
            validateTimeZone(context, nodeInfo, directoryName, supportedTimeZones);
        }
        return isValidatedWithoutError(context);
    }

    private boolean validateTimeZone(final ValidationContext context, final NodeInfo nodeInfo, final String dirName,
            final Collection<String> listOfTimeZones) {
        final String timeZone = nodeInfo.getTimeZone();

        if (timeZone != null && !listOfTimeZones.contains(timeZone)) {
            recordNodeValidationError(context, VALIDATION_ATTRIBUTE_NOT_SUPPORTED_ERROR, dirName, NodeAttribute.TIMEZONE.toString(), timeZone);
            return false;
        }
        return true;
    }

    private Collection<String> getSupportedTimeZones() {
        final EnumDataTypeSpecification supportedTimeZones = modelReader.getLatestEnumDataTypeSpecification(OSS_EDT, NAMESPACE_OSS_NE_DEF,
                TIMEZONE_DATA_TYPE);
        return supportedTimeZones.getMemberNames();
    }
}
