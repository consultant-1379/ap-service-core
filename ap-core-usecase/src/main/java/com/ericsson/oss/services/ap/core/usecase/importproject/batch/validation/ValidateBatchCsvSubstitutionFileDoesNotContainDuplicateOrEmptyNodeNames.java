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

import static com.ericsson.oss.services.ap.core.usecase.validation.common.ProjectArtifact.NODEINFO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;
import com.ericsson.oss.services.ap.core.usecase.csv.CsvData;
import com.ericsson.oss.services.ap.core.usecase.csv.CsvReader;
import com.ericsson.oss.services.ap.core.usecase.importproject.batch.template.Template;
import com.ericsson.oss.services.ap.core.usecase.importproject.batch.template.TemplateReader;

/**
 * Validates that the node name specified in each row of the <code>substitutionVariables.csv</code> file is unique and is not empty.
 * <p>
 * This validation only applies to a batch project archive.
 */
@Group(name = ValidationRuleGroups.ORDER_BATCH, priority = 8, abortOnFail = false)
@Rule(name = "ValidateBatchCsvSubstitutionFileDoesNotContainDuplicateOrEmptyNodeNames")
public class ValidateBatchCsvSubstitutionFileDoesNotContainDuplicateOrEmptyNodeNames extends AbstractValidateBatchImport {

    private static final String NODEINFO_NAME_TAG = "<name>";
    private static final String NODEINFO_NAME_TAG_XPATH = "nodeInfo/name";
    private static final String DUPLICATE_NODE_NAME_VALIDATION_MESSAGE = "validation.batch.csv.data.nodename.duplicates";
    private static final String EMPTY_NODE_NAME_VALIDATION_MESSAGE = "validation.batch.csv.data.nodename.empty";
    private static final String MANDATORY_NODE_INFO_NAME_SUBSTITUTION_TAG_DOES_NOT_EXIST = "validation.batch.nodeinfo.mandatory.substitution.missing";
    private static final String MATCHING_NODE_NAME_HEADER_NOT_FOUND = "validation.batch.csv.substitution.variable.missing";

    @Override
    protected boolean validate(final ValidationContext context, final String fileName, final List<String> directoryNames) {
        final Archive batchArchiveFile = getArchive(context);
        final CsvData csvBatchData = new CsvReader(batchArchiveFile).readCsv();
        final String nodeNameSubstitutionTagValue = getNodeNameHeader(context);

        if (nodeNameSubstitutionTagValue == null) {
            recordValidationError(context, MANDATORY_NODE_INFO_NAME_SUBSTITUTION_TAG_DOES_NOT_EXIST, NODEINFO_NAME_TAG);
            return false;
        }

        if (doesNodeNameTagExistInCsvHeader(csvBatchData, nodeNameSubstitutionTagValue)) {
            checkForDuplicateOrEmptyNodeNames(context, csvBatchData, nodeNameSubstitutionTagValue);
        } else {
            recordValidationError(context, MATCHING_NODE_NAME_HEADER_NOT_FOUND, nodeNameSubstitutionTagValue, NODEINFO.toString());
        }

        return context.getValidationErrors().isEmpty();
    }

    private void checkForDuplicateOrEmptyNodeNames(final ValidationContext context, final CsvData data, final String nodeNameHeader) {
        final int columnNumber = getColumnNumberOfHeader(data, nodeNameHeader);
        final int rowCount = data.getRowCount();

        final Collection<String> nodeNames = new HashSet<>();
        final List<String> emptyNodeNameRows = new ArrayList<>();
        final Collection<String> duplicateNodeNames = new HashSet<>();

        for (int currentRow = 0; currentRow < rowCount; currentRow++) {
            final String nodeNameValue = data.getValue(currentRow, columnNumber);

            if (nodeNameValue.isEmpty()) {
                emptyNodeNameRows.add(Integer.toString(currentRow + 1));
            } else if (!nodeNames.add(nodeNameValue)) {
                duplicateNodeNames.add(nodeNameValue);
            }
        }

        recordDuplicateAndEmptyNodeNameErrors(context, emptyNodeNameRows, duplicateNodeNames);
    }

    private void recordDuplicateAndEmptyNodeNameErrors(final ValidationContext context, final List<String> emptyNodeNameRows,
            final Collection<String> duplicateNodeNames) {

        if (!emptyNodeNameRows.isEmpty()) {
            recordValidationError(context, EMPTY_NODE_NAME_VALIDATION_MESSAGE, createCommaSeparatedList(emptyNodeNameRows));
        }
        if (!duplicateNodeNames.isEmpty()) {
            recordValidationError(context, DUPLICATE_NODE_NAME_VALIDATION_MESSAGE, createCommaSeparatedList(duplicateNodeNames));
        }
    }

    private static String createCommaSeparatedList(final Collection<String> errorData) {
        return StringUtils.join(errorData, ", ");
    }

    private String getNodeNameHeader(final ValidationContext context) {
        try {
            final Template csvTemplate = getTemplate(NODEINFO.toString(), context);
            return csvTemplate.getPlaceHolderForPath(NODEINFO_NAME_TAG_XPATH);
        } catch (final IllegalArgumentException e) {
            logger.warn("Error retrieving nodeName header from CSV file: {}", e.getMessage(), e);
            return null;
        }
    }

    private Template getTemplate(final String templateName, final ValidationContext context) {
        final List<Template> csvTemplateList = new TemplateReader(getArchive(context)).listTemplates();
        for (final Template csvTemplate : csvTemplateList) {
            if (templateName.equalsIgnoreCase(csvTemplate.getName())) {
                return csvTemplate;
            }
        }

        throw new IllegalArgumentException("No template found with name: " + templateName);
    }

    private static boolean doesNodeNameTagExistInCsvHeader(final CsvData csvBatchData, final String nodeNameSubstitutionTagValue) {
        return getColumnNumberOfHeader(csvBatchData, nodeNameSubstitutionTagValue) >= 0;
    }

    private static int getColumnNumberOfHeader(final CsvData data, final String nodeNameHeader) {
        final int headerCount = data.getHeaderCount();

        for (int currentColumn = 0; currentColumn < headerCount; currentColumn++) {
            final String csvHeaderName = data.getHeader(currentColumn).trim();
            if (nodeNameHeader.equals(csvHeaderName)) {
                return currentColumn;
            }
        }
        return -1;
    }
}
