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

package com.ericsson.oss.services.ap.core.usecase.importproject

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_NAME

import javax.inject.Inject

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.validation.ValidationEngine
import com.ericsson.oss.services.ap.core.usecase.archive.Archive
import com.ericsson.oss.services.ap.core.usecase.importproject.batch.BatchArchiveProcessor

class ProjectValidatorSpec extends CdiSpecification {

    private static final String PROJECT_FILE_NAME = "project.zip";
    private static final String GREENFIELD = "greenfield";
    private static final String EXPANSION = "expansion";
    private static final String REPLACE = "replace";
    private static final String MIGRATION = "migration";

    private static final String VALID_PROJECT_XML = "<?xml version='1.0' encoding='UTF-8'?> " +
    "<projectInfo xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' " +
    "xsi:noNamespaceSchemaLocation='erbsProjectInfo.xsd'>" +
    "<name>"+
    PROJECT_NAME +
    "</name>" +
    "<description>proj description</description> " +
    "<creator>APCreator</creator> "+
    "</projectInfo>";

    @ObjectUnderTest
    private ProjectValidator projectValidator;

    @Inject
    private ValidationEngine validationEngine;

    @MockedImplementation
    private BatchArchiveProcessor batchArchiveProcessor;

    @MockedImplementation
    private NodeSchemaProcessor nodeSchemaProcessor;

    @MockedImplementation
    private Archive batchArchive;

    @MockedImplementation
    private Archive standardArchive;

    private static final String HARDWARE_REPLACE_NODE_INFO_XSD = "HardwareReplaceNodeInfo.xsd";

    private static final String MIGRATION_NODE_INFO_XSD = "MigrationNodeInfo.xsd";

    def setup() {
        batchArchiveProcessor.process(batchArchive) >> standardArchive;
        standardArchive.getArtifactContentAsString("projectInfo.xml") >> VALID_PROJECT_XML;
        nodeSchemaProcessor.existsNodeInfoFile(standardArchive, GREENFIELD) >> true;
        nodeSchemaProcessor.existsNodeInfoFile(standardArchive, EXPANSION) >> true;
        nodeSchemaProcessor.existsNodeInfoFile(standardArchive, REPLACE) >> true;
        nodeSchemaProcessor.existsNodeInfoFile(standardArchive, MIGRATION) >> true;
        standardArchive.getAllDirectoryNames() >> Arrays.asList(GREENFIELD, EXPANSION, REPLACE, MIGRATION);
    }

    def "when batch project validated successfully THEN no validation exception thrown" () {
        given: "a valid batch project"
            validationEngine.validate(_) >> true;

        when: "validate the project file"
            projectValidator.validateBatchProject(PROJECT_FILE_NAME, batchArchive);

       then: "the validation is successful and no exception thrown"
            1 * validationEngine.validate(_);
            noExceptionThrown()
    }

    def "when standard archive validated successfully THEN no validation exception thrown and all nodes are validated" () {
        given: "a valid standard archive with greenfield, expansion and migration nodes"
            validationEngine.validate(_) >> true;

        when: "validate the project file"
            projectValidator.validateStandardProject(PROJECT_FILE_NAME, standardArchive);

        then: "the validate engine validates greenfield, hardware replace, expansion and migration nodes without exception"
            4 * validationEngine.validate(_);
    }

    def "when archive hardware replace validated successfully and THEN no validation exception thrown and all nodes are validated" () {
        given: "a valid standard archive with greenfield, expansion and migration nodes"
            validationEngine.validate(_) >> true

        and: "hardware replace nodes"
            nodeSchemaProcessor.getNoNamespaceSchemaLocation(standardArchive, GREENFIELD) >> HARDWARE_REPLACE_NODE_INFO_XSD

        when: "validate the project file"
            projectValidator.validateStandardProject(PROJECT_FILE_NAME, standardArchive);

        then: "the validate engine validates hardware replace nodes without exception"
            4 * validationEngine.validate(_);
    }

    def "when migration archive validated successfully and THEN no validation exception thrown and all nodes are validated" () {
        given: "a valid standard archive with migration of nodes"
            validationEngine.validate(_) >> true

        and: "migration of nodes"
            nodeSchemaProcessor.getNoNamespaceSchemaLocation(standardArchive, MIGRATION) >> MIGRATION_NODE_INFO_XSD

        when: "validate the project file"
            projectValidator.validateStandardProject(PROJECT_FILE_NAME, standardArchive);

        then: "the validate engine validates migration nodes without exception"
            4 * validationEngine.validate(_);
    }
}
