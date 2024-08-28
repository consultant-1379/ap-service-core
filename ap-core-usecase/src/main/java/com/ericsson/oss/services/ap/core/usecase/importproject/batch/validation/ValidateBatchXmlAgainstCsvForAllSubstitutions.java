/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.importproject.batch.validation;

import java.util.List;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;
import com.ericsson.oss.services.ap.core.usecase.csv.CsvData;
import com.ericsson.oss.services.ap.core.usecase.csv.CsvReader;
import com.ericsson.oss.services.ap.core.usecase.importproject.batch.template.Template;
import com.ericsson.oss.services.ap.core.usecase.importproject.batch.template.TemplateReader;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;

/**
 * Validate that each <code>%substitution_variable%</code> listed in the .xml files is present as a header in the .csv file. <br>
 * <ul>
 * <li>nodeInfo.xml - minimum mandatory substitution variable for the element tag
 * <code>{@literal <}name{@literal >}%var%{@literal <}/name{@literal >}</code></li>
 * <li>nodeInfo.xml - additional optional substitution variables</li>
 * <li>*.xml - optional substitution variables</li>
 * </ul>
 */
@Group(name = ValidationRuleGroups.ORDER_BATCH, priority = 9, abortOnFail = true)
@Rule(name = "ValidateBatchXmlAgainstCsvForAllSubstitutions")
public class ValidateBatchXmlAgainstCsvForAllSubstitutions extends AbstractValidateBatchImport {

    @Override
    protected boolean validate(final ValidationContext context, final String filename, final List<String> directoryNames) {
        final Archive batchArchiveFile = getArchive(context);
        final CsvData csvSubstitutionFileData = new CsvReader(batchArchiveFile).readCsv();
        final List<Template> xmlTemplateFilenameList = new TemplateReader(batchArchiveFile).listTemplates();

        for (final Template xmlTemplateFilename : xmlTemplateFilenameList) {
            validateEachXmlTemplate(context, xmlTemplateFilename, csvSubstitutionFileData);
        }

        return isValidatedWithoutError(context);
    }

    private void validateEachXmlTemplate(final ValidationContext context, final Template xmlTemplate, final CsvData csvData) {
        boolean areAllSubVarsInCsv = true;
        final StringBuilder missingSubstitutionValues = new StringBuilder();

        for (final String substitutionValue : xmlTemplate.getPlaceHolders()) {
            if (isInvalidSubstitutionValue(csvData, substitutionValue)) {
                missingSubstitutionValues.append(", ").append(substitutionValue.trim());
                areAllSubVarsInCsv = false;
            }
        }

        if (!areAllSubVarsInCsv) {
            recordValidationError(context, "validation.batch.csv.substitution.variable.missing", missingSubstitutionValues.toString().substring(2),
                    xmlTemplate.getName());
        }
    }

    private static boolean isInvalidSubstitutionValue(final CsvData csvData, final String substitutionValue) {
        return !isSubVarPresentinCsv(csvData, substitutionValue) && !ReservedSubstitutionTagChecker.isReserved(substitutionValue);
    }

    private static boolean isSubVarPresentinCsv(final CsvData data, final String subVar) {
        final int hc = data.getHeaderCount();
        final String trimmedSubstituionValue = subVar.trim();

        for (int i = 0; i < hc; i++) {
            if ((data.getHeader(i).trim()).equals(trimmedSubstituionValue)) {
                return true;
            }
        }

        return false;
    }
}
