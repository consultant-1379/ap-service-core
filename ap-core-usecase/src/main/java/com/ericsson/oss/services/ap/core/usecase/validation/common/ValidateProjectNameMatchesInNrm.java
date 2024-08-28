/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
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
import static com.ericsson.oss.services.ap.core.usecase.validation.common.ProjectArtifact.PROJECTINFO;

import java.util.List;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ValidationCrudException;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Groups;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.util.xml.DocumentReader;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;

/**
 * Rule for expansion or replace of node to validate imported project name is the same as projectMo in ENM, if projectMo already exists
 */
@Groups(value = {
    @Group(name = ValidationRuleGroups.EXPANSION, priority = 9, abortOnFail = true),
    @Group(name = ValidationRuleGroups.HARDWARE_REPLACE, priority = 9, abortOnFail = true),
    @Group(name = ValidationRuleGroups.MIGRATION, priority = 9, abortOnFail = true)})
@Rule(name = "ValidateProjectMatchesInNrm")
public class ValidateProjectNameMatchesInNrm extends AbstractValidateRule {

    private static final String VALIDATION_TERMINATED_CRUD_EXCEPTION_MESSAGE = "failure.general";
    private static final String VALIDATION_FAIL_IMPORTED_PROJECT_DOES_NOT_MATCH_EXISTING_NODE_PARENT_MO = "validation.imported.project.name.does.not.match.enm.project.name";

    @Override
    protected boolean validate(final ValidationContext context, final List<String> directoryNames) {
        boolean result = true;
        for (final String dirName : directoryNames) {
            result &= validateProjectNameExistsInEnm(context, dirName);
        }
        return result;
    }

    private boolean validateProjectNameExistsInEnm(final ValidationContext context, final String dirName) {
        final String projectInfoContent = getArtifactOfName(context, PROJECTINFO.toString());
        final String importedProjectName = new DocumentReader(projectInfoContent).getElementValue("name");

        final String nodeInfoContent = getContentAsString(getArchive(context), ProjectArtifact.NODEINFO.toString(), dirName);
        final String fileNodeName = new DocumentReader(nodeInfoContent).getElementValue("name");

        try {
            final ManagedObject enmNodeNameMo = findMo(fileNodeName, NODE.toString(), AP.toString());
            final String enmProjectName = findProjectName(enmNodeNameMo);

            if (enmProjectName == null || importedProjectName.equals(enmProjectName)) {
                return true;
            } else {
                final String message = apMessages.format(VALIDATION_FAIL_IMPORTED_PROJECT_DOES_NOT_MATCH_EXISTING_NODE_PARENT_MO, fileNodeName,
                    enmProjectName, importedProjectName);
                addNodeValidationFailure(context, message, dirName);
                return false;
            }

        } catch (final Exception e) {
            logger.error("Unexpected error while validating project name matches project Mo in ENM for node {}", fileNodeName, e);
            throw new ValidationCrudException(apMessages.get(VALIDATION_TERMINATED_CRUD_EXCEPTION_MESSAGE), e);
        }
    }

    private static String findProjectName(final ManagedObject enmNodeNameMo) {
        return enmNodeNameMo != null ? enmNodeNameMo.getParent().getName() : null;
    }
}
