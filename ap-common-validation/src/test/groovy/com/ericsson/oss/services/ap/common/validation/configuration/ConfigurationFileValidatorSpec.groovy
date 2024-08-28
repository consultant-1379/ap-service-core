/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.ap.common.validation.configuration

import java.util.concurrent.Callable

import javax.inject.Inject

import org.mockito.internal.util.reflection.Whitebox

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.common.artifacts.util.ArtifactResourceOperations
import com.ericsson.oss.services.ap.common.cm.DpsQueries
import com.ericsson.oss.services.ap.common.cm.TransactionalExecutor
import com.ericsson.oss.services.ap.common.test.util.setup.MoCreatorSpec
import com.ericsson.oss.services.cm.bulkimport.api.ImportService
import com.ericsson.oss.services.cm.bulkimport.dto.ImportValidationSpecification
import com.ericsson.oss.services.cm.bulkimport.response.ImportServiceValidationResponse
import com.ericsson.oss.services.cm.bulkimport.response.dto.BulkImportServiceErrorDetails
import com.ericsson.oss.services.cm.bulkimport.response.dto.ImportServiceValidationError
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject

class ConfigurationFileValidatorSpec extends CdiSpecification {
    private final static String NODE_NAME = "VnfNode"
    private final static String NODE_TYPE = "RadioNode"
    private final static String PROJECT_NAME = "ProjectName"
    private final static String NODE_IDENTIFIER = "NodeIdentifier"
    private final static String ARTIFACT_PATH = "ericsson/tor/data/configurationFile.xml"
    private final static String ARTIFACT_CONTENTS = "artifact contents"
    private final static ImportServiceValidationError IMPORT_SERVICE_VALIDATION_ERROR = new ImportServiceValidationError(
            ImportServiceValidationError.builder().setFailureReason("FailureReason").setLineNumber(1))

    private ArchiveArtifact archiveArtifact

    @MockedImplementation
    BulkImportServiceErrorDetails bulkImportServiceErrorDetails

    @MockedImplementation
    ArtifactResourceOperations artifactResourceOperations;

    @MockedImplementation
    ImportService importService

    @MockedImplementation
    ImportServiceValidationResponse importServiceValidationResponse

    @ObjectUnderTest
    ConfigurationFileValidator configurationFileValidator

    @Inject
    private RuntimeConfigurableDps dps

    @Inject
    private DataPersistenceService dataPersistenceService

    @Inject
    private DpsQueries dpsQueries

    @Inject
    private PersistenceObject persistenceObject

    @MockedImplementation
    private TransactionalExecutor executor

    private ManagedObject networkElementMo

    private Map<String, Object> nodeAttributes = new HashMap<String, Object>()

    def setup () {
        archiveArtifact = new ArchiveArtifact(ARTIFACT_PATH, ARTIFACT_CONTENTS)

        dps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        dataPersistenceService = dps.build()
        dpsQueries.getDataPersistenceService() >> dataPersistenceService
        executor.execute(_ as Callable) >> { Callable call -> call.call() }
        Whitebox.setInternalState(dpsQueries, "dps", dps.build())
        Whitebox.setInternalState(dpsQueries, "executor", executor)
        MoCreatorSpec.setDps(dps)
        nodeAttributes.clear()
    }

    def "when a bulkCM file contains a synchronized node and validation is successful, then validation model & instance validation was used"() {
        given:
        if(createNetworkElement) {
            networkElementMo = MoCreatorSpec.createNetworkElementMo(NODE_NAME, NODE_TYPE, persistenceObject)
            MoCreatorSpec.createCmFunctionMo(networkElementMo, syncStatus)
        }
        importService.validate(_ as ImportValidationSpecification) >> importServiceValidationResponse
        importServiceValidationResponse.getValidationErrors() >> []
        importServiceValidationResponse.getErrors() >> []
        importServiceValidationResponse.getProperties()

        when:
        final List<String> errorMessages = configurationFileValidator.validateFile(PROJECT_NAME, NODE_NAME, NODE_IDENTIFIER, archiveArtifact)

        then:
        1 * importService.validate({it.isValidateInstances() == instanceValidation}) >> importServiceValidationResponse
        errorMessages.size() == 0
        where:
        syncStatus       || instanceValidation || createNetworkElement
        "SYNCHRONIZED"   || true               || true
        "PENDING"        || false              || true
        "TOPOLOGY"       || false              || true
        "ATTRIBUTE"      || false              || true
        "DELTA"          || false              || true
        "UNSYNCHRONIZED" || false              || true
        ""               || false              || false
    }

    def "when a bulkCM file contains no errors then validation is successful, no error messages are returned"() {
        given:
        importService.validate(_ as ImportValidationSpecification) >> importServiceValidationResponse
        importServiceValidationResponse.getValidationErrors() >> []
        importServiceValidationResponse.getErrors() >> []

        when:
        final List<String> errorMessages = configurationFileValidator.validateFile(PROJECT_NAME, NODE_NAME, NODE_IDENTIFIER, archiveArtifact)

        then:
        errorMessages.size() == 0
    }

    def "when a bulkCM file causes one import validation error, then an error message is returned"() {
        given:
        importService.validate(_ as ImportValidationSpecification) >> importServiceValidationResponse
        importServiceValidationResponse.getValidationErrors() >> [IMPORT_SERVICE_VALIDATION_ERROR]
        importServiceValidationResponse.getErrors() >> []

        when:
        final List<String> errorMessages = configurationFileValidator.validateFile(PROJECT_NAME, NODE_NAME, NODE_IDENTIFIER, archiveArtifact)

        then:
        errorMessages.size() == 1
        errorMessages.get(0) == "configurationFile.xml (MO operation on line 1): FailureReason"
    }

    def "when a bulkCM file causes one bulk import service error, then an error message is returned"() {
        given:
        importService.validate(_ as ImportValidationSpecification) >> importServiceValidationResponse
        importServiceValidationResponse.getValidationErrors() >> []
        importServiceValidationResponse.getErrors() >> [bulkImportServiceErrorDetails]
        bulkImportServiceErrorDetails.getErrorMessage() >> "Error 200: Error Message"

        when:
        final List<String> errorMessages = configurationFileValidator.validateFile(PROJECT_NAME, NODE_NAME, NODE_IDENTIFIER, archiveArtifact)

        then:
        errorMessages.size() == 1
        errorMessages.get(0) == "Error Message"
    }

    def "when an exception is thrown when validating a bulk import configuration file, then a message recording this is returned"() {
        given:
        importService.validate(_ as ImportValidationSpecification) >> { throw new IllegalArgumentException() }

        when:
        final List<String> errorMessages = configurationFileValidator.validateFile(PROJECT_NAME, NODE_NAME, NODE_IDENTIFIER, archiveArtifact)

        then:
        errorMessages.size() == 1
        errorMessages.get(0).contains("Validation failed while attempting to validate BulkCmFile configurationFile.xml with exception")
    }
}
