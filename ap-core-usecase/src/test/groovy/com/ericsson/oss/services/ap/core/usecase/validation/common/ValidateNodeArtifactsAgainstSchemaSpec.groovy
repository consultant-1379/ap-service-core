/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.validation.common

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.services.ap.api.exception.ArtifactDataNotFoundException
import com.ericsson.oss.services.ap.api.schema.SchemaData
import com.ericsson.oss.services.ap.api.schema.SchemaService
import com.ericsson.oss.services.ap.api.validation.ValidationContext
import com.ericsson.oss.services.ap.common.util.xml.XmlValidator
import com.ericsson.oss.services.ap.common.util.xml.exception.SchemaAccessException
import com.ericsson.oss.services.ap.common.util.xml.exception.SchemaValidationException
import com.ericsson.oss.services.ap.core.usecase.archive.Archive
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfoReader
import com.ericsson.oss.services.ap.core.usecase.validation.greenfield.ImportProjectTargetKey
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_IDENTIFIER_VALUE

class ValidateNodeArtifactsAgainstSchemaSpec extends CdiSpecification {

    @MockedImplementation
    private Archive archive;

    @MockedImplementation
    private SchemaService schemaService;

    @MockedImplementation
    private Archive archiveReader;

    @MockedImplementation
    private XmlValidator xmlValidator;

    @ObjectUnderTest
    private ValidateNodeArtifactsAgainstSchema validateNodeArtifactsAgainstSchema;

    @MockedImplementation
    private NodeInfoReader nodeInfoReader;

    private ValidationContext validationContext;

    private ArchiveArtifact archiveArtifact;

    RuntimeConfigurableDps dps

    private static final String NODE_NAME = "Node1"
    private static final String RADIO_NODE = "RadioNode"
    private static final String NODE_INFO = "nodeInfo.xml";

    private NodeInfo nodeInfo;
    private final List<String> directoryNames = new ArrayList<>();
    private Map<String, Object> contextTarget;

    private static final String RADIO_FILE_NAME = "radio.xml";
    private static final String RADIO_FILE_CONTENT = "<?xml radio";
    private static final String RADIO_FILE_TYPE = "configurations";
    private static final String SCHEMA_FILE_NAME = "schema.xsd";
    private static final String SCHEMA_FILE_CONTENT = "<?xml Schema Content";
    private static final String SCHEMA_LOCATION = "/schema_location";

    private ArchiveArtifact radio;
    private SchemaData schemaData;

    def setup() {

        directoryNames.add(NODE_NAME);

        contextTarget = new HashMap<>();
        contextTarget.put("fileContent", archiveReader);
        contextTarget.put(ImportProjectTargetKey.DIRECTORY_LIST.toString(), directoryNames);

        validationContext = new ValidationContext("", contextTarget);

        final List<String> configArtifacts = new ArrayList<>();
        configArtifacts.add(RADIO_FILE_NAME);

        final Map<String, List<String>> nodeArtifactsByType = new HashMap<>();
        nodeArtifactsByType.put(RADIO_FILE_TYPE, configArtifacts);

        radio = new ArchiveArtifact(RADIO_FILE_NAME, RADIO_FILE_CONTENT);
        nodeInfo = new NodeInfo();
        nodeInfo.setName(NODE_NAME);
        nodeInfo.setNodeType(RADIO_NODE);
        nodeInfo.setNodeIdentifier(NODE_IDENTIFIER_VALUE);
        nodeInfo.setNodeArtifacts(nodeArtifactsByType);
        schemaData = new SchemaData(SCHEMA_FILE_NAME, RADIO_FILE_TYPE, NODE_IDENTIFIER_VALUE,
                SCHEMA_FILE_CONTENT.getBytes(), SCHEMA_LOCATION);
    }

    def "Validation fails when project file contains invalid artifact against schema" () {
        given: "Migration NodeInfo xml, artifact and corresponding schema"
        archiveArtifact = new ArchiveArtifact("NodeInfo", getNodeInfo("suspend=\"true\""))
        archive.getArtifactOfNameInDir(NODE_NAME, NODE_INFO) >> archiveArtifact

        nodeInfoReader.read(archiveReader, NODE_NAME) >> nodeInfo
        archiveReader.getAllDirectoryNames() >> directoryNames
        archiveReader.getArtifactOfNameInDir(NODE_NAME, radio.getName()) >> radio
        schemaService.readSchema(RADIO_NODE, NODE_IDENTIFIER_VALUE, RADIO_FILE_TYPE) >> schemaData
        xmlValidator.validateAgainstSchema(RADIO_FILE_CONTENT, SCHEMA_FILE_CONTENT.getBytes()) >> { throw new SchemaValidationException("Migration Schema Validation Failed")};

        when: "Artifact contents are invalid against the schema"
        boolean isValidationSuccess = validateNodeArtifactsAgainstSchema.execute(validationContext)

        then: "Validation failed"
        isValidationSuccess == false
    }

    def "Validation fails when project file contains invalid artifact and throws schema access exception" () {
        given: "Migration NodeInfo xml, artifact and corresponding schema"
        archiveArtifact = new ArchiveArtifact("NodeInfo", getNodeInfo("suspend=\"true\""))
        archive.getArtifactOfNameInDir(NODE_NAME, NODE_INFO) >> archiveArtifact

        nodeInfoReader.read(archiveReader, NODE_NAME) >> nodeInfo
        archiveReader.getAllDirectoryNames() >> directoryNames
        archiveReader.getArtifactOfNameInDir(NODE_NAME, radio.getName()) >> radio
        schemaService.readSchema(RADIO_NODE, NODE_IDENTIFIER_VALUE, RADIO_FILE_TYPE) >> schemaData
        xmlValidator.validateAgainstSchema(RADIO_FILE_CONTENT, SCHEMA_FILE_CONTENT.getBytes()) >> { throw new SchemaAccessException("Migration Schema Access Not Found")};

        when: "Schema access not available for migration artifact"
        boolean isValidationSuccess = validateNodeArtifactsAgainstSchema.execute(validationContext)

        then: "Validation failed"
        isValidationSuccess == false
    }

    def "Validation passes when project file contains valid artifact against schema" () {
        given: "Migration NodeInfo xml, artifact and corresponding schema"
        archiveArtifact = new ArchiveArtifact("NodeInfo", getNodeInfo("suspend=\"true\""))
        archive.getArtifactOfNameInDir(NODE_NAME, NODE_INFO) >> archiveArtifact

        nodeInfoReader.read(archiveReader, NODE_NAME) >> nodeInfo
        archiveReader.getAllDirectoryNames() >> directoryNames
        archiveReader.getArtifactOfNameInDir(NODE_NAME, radio.getName()) >> radio
        schemaService.readSchema(RADIO_NODE, NODE_IDENTIFIER_VALUE, RADIO_FILE_TYPE) >> schemaData

        when: "Artifact contents are valid against the schema"
        boolean isValidationSuccess = validateNodeArtifactsAgainstSchema.execute(validationContext)

        then: "Validation passed"
        isValidationSuccess == true
    }

    def "Validation passes when project artifact does not have corresponding schema" () {
        given: "Migration NodeInfo xml and artifact"
        archiveArtifact = new ArchiveArtifact("NodeInfo", getNodeInfo("suspend=\"true\""))
        archive.getArtifactOfNameInDir(NODE_NAME, NODE_INFO) >> archiveArtifact

        nodeInfoReader.read(archiveReader, NODE_NAME) >> nodeInfo
        archiveReader.getAllDirectoryNames() >> directoryNames
        archiveReader.getArtifactOfNameInDir(NODE_NAME, radio.getName()) >> radio
        schemaService.readSchema(RADIO_NODE, NODE_IDENTIFIER_VALUE, RADIO_FILE_TYPE) >> null

        when: "No schema is found for the artifact"
        boolean isTrue = validateNodeArtifactsAgainstSchema.execute(validationContext)

        then: "Validation passed"
        isTrue == true
    }

    def "ReadSchema failed when artifact not found and exception handled properly" () {
        given: "Migration NodeInfo xml, artifact and corresponding schema"
        archiveArtifact = new ArchiveArtifact("NodeInfo", getNodeInfo("suspend=\"true\""))
        archive.getArtifactOfNameInDir(NODE_NAME, NODE_INFO) >> archiveArtifact

        nodeInfoReader.read(archiveReader, NODE_NAME) >> nodeInfo
        archiveReader.getAllDirectoryNames() >> directoryNames
        archiveReader.getArtifactOfNameInDir(NODE_NAME, radio.getName()) >> radio
        schemaService.readSchema(RADIO_NODE, NODE_IDENTIFIER_VALUE, RADIO_FILE_TYPE) >> { throw new ArtifactDataNotFoundException("Migration Artifact Not Found")}

        when: "Schema validation invoked and artifact data not found exception handled properly"
        boolean isValidationSuccess = validateNodeArtifactsAgainstSchema.execute(validationContext)

        then: "No validation errors"
        isValidationSuccess == true
    }

    def "Validation fails when artifact read throws exception" () {
        given: "Migration NodeInfo xml, artifact and corresponding schema"
        archiveArtifact = new ArchiveArtifact("NodeInfo", getNodeInfo("suspend=\"true\""))
        archive.getArtifactOfNameInDir(NODE_NAME, NODE_INFO) >> archiveArtifact

        nodeInfoReader.read(archiveReader, NODE_NAME) >> { throw new ArtifactDataNotFoundException("Artifacts not available") }
        when: "Schema validation invoked and artifact data not found exception handled properly"
        boolean isValidationSuccess = validateNodeArtifactsAgainstSchema.execute(validationContext)

        then: "No validation errors"
        isValidationSuccess == false

    }

    def getNodeInfo(final String nodeName) {
        return  """<?xml version=\"1.0\" encoding=\"UTF-8\"?>
          <nodeInfo xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:noNamespaceSchemaLocation="MigrationNodeInfo.xsd">
              <name>${nodeName}</name>
              <artifacts>
                  <configurations>
                      <nodeConfiguration>radio.xml</nodeConfiguration>
                  </configurations>
              </artifacts>
          </nodeInfo>"""
    }

}
