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
package com.ericsson.oss.services.ap.core.usecase.validation.migration

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.services.ap.api.schema.SchemaData
import com.ericsson.oss.services.ap.api.schema.SchemaService
import com.ericsson.oss.services.ap.api.validation.ValidationContext
import com.ericsson.oss.services.ap.common.cm.DpsQueries
import com.ericsson.oss.services.ap.core.usecase.archive.Archive
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact
import com.ericsson.oss.services.ap.core.usecase.validation.greenfield.ImportProjectTargetKey
import org.mockito.internal.util.reflection.Whitebox
import javax.inject.Inject
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_IDENTIFIER_VALUE
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.VALID_NODE_TYPE

class ValidateMigrationNodeInfoArtifactAgainstSchemaSpec extends CdiSpecification {

    @MockedImplementation
    private Archive archive;

    @MockedImplementation
    private SchemaService schemaService;

    @ObjectUnderTest
    private ValidateMigrationNodeInfoArtifactAgainstSchema validateMigrationNodeInfoArtifactAgainstSchema;

    @Inject
    private DpsQueries dpsQueries

    @Inject
    private PersistenceObject persistanceObject

    private ValidationContext validationContext;

    private ArchiveArtifact archiveArtifact;

    RuntimeConfigurableDps dps

    private final Map<String, Object> projectDataContentTarget = new HashMap<>();
    private final List<SchemaData> schemas = new ArrayList<>();

    private static final String NODE_NAME = "Node1"
    private static final String RADIO_NODE = "RadioNode"
    private static final String NODE_IDENTIFIER = "1998-184-092"
    private static final String NODE_INFO = "nodeInfo.xml";

    def setup() {
        dps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        addNetworkElementMo()
        Whitebox.setInternalState(dpsQueries, "dps", dps.build())
        List<String> directoryList = new ArrayList<>();
        directoryList.add(NODE_NAME);
        archive.getAllDirectoryNames() >> directoryList;

        projectDataContentTarget.put(ImportProjectTargetKey.FILENAME.toString(), "migrationProject.zip");
        projectDataContentTarget.put(ImportProjectTargetKey.FILE_CONTENT.toString(), archive);
        projectDataContentTarget.put(ImportProjectTargetKey.DIRECTORY_LIST.toString(), directoryList);
        validationContext = new ValidationContext("import", projectDataContentTarget);

        def URL migrationNodeInfoSchemaUrl = getClass().getResource("/migration/MigrationNodeInfo.xsd");
        def Path migrationNodeInfoSchemaPath = Paths.get(migrationNodeInfoSchemaUrl.toURI());
        def SchemaData migrationNodeInfoSchema = createSchemaData("MigrationNodeInfo", migrationNodeInfoSchemaPath);
        schemas.add(migrationNodeInfoSchema);
        schemaService.readSchemas(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE, "NodeInfo") >> schemas;
        validateMigrationNodeInfoArtifactAgainstSchema.nodeInfoSchemasByVersion.put(NODE_IDENTIFIER, schemas)
    }

    def "Validate MigrationNodeInfo schema with all the new attributes" () {
        given: "Migration NodeInfo xml and the corresponding xsd"
        archiveArtifact = new ArchiveArtifact("NodeInfo", getNodeInfo())
        archive.getArtifactOfNameInDir(NODE_NAME, NODE_INFO) >> archiveArtifact

        when: "validate the new attributes against the expansion schema"
        boolean migrationSchemaCorrect = validateMigrationNodeInfoArtifactAgainstSchema.execute(validationContext)

        then: "the check returns true"
        migrationSchemaCorrect == true
    }

    def "Validate MigrationNodeInfo schema with invalid workOrderId" () {
        given: "Migration NodeInfo xml and the corresponding xsd"
        archiveArtifact = new ArchiveArtifact("NodeInfo", getInvalidNodeInfo())
        archive.getArtifactOfNameInDir(NODE_NAME, NODE_INFO) >> archiveArtifact

        when: "validate the new attributes against the expansion schema"
        boolean migrationSchemaCorrect = validateMigrationNodeInfoArtifactAgainstSchema.execute(validationContext)

        then: "the check returns false"
        migrationSchemaCorrect == false
    }

    def getNodeInfo() {
        return  """<?xml version=\"1.0\" encoding=\"UTF-8\"?>
        <nodeInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"
            xsi:noNamespaceSchemaLocation=\"MigrationNodeInfo.xsd\">
                <name>Node1</name>
                <workOrderId>ABC-12345</workOrderId>
                <notifications>
                    <email>abc@ex1.org,efg.123@example.net;ar-2b@mcc_rt.cn,111_wzt@164-bn.czt</email>
                </notifications>
                <artifacts>
                    <siteBasic>SiteBasic.xml</siteBasic>
                    <siteInstallation>SiteInstallation.xml</siteInstallation>
                    <siteEquipment>SiteEquipment.xml</siteEquipment>
                    <configurations>
                        <nodeConfiguration>radio.xml</nodeConfiguration>
                    </configurations>
                </artifacts>
        </nodeInfo>"""
    }

    def getInvalidNodeInfo() {
        return  """<?xml version=\"1.0\" encoding=\"UTF-8\"?>
        <nodeInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"
            xsi:noNamespaceSchemaLocation=\"MigrationNodeInfo.xsd\">
                <name>Node1</name>
                <workOrderId> ABC-12345</workOrderId>
                <notifications>
                    <email>abc@ex1.org,efg.123@example.net;ar-2b@mcc_rt.cn,111_wzt@164-bn.czt</email>
                </notifications>
                <artifacts>
                    <siteBasic>SiteBasic.xml</siteBasic>
                    <siteInstallation>SiteInstallation.xml</siteInstallation>
                    <siteEquipment>SiteEquipment.xml</siteEquipment>
                    <configurations>
                        <nodeConfiguration>radio.xml</nodeConfiguration>
                    </configurations>
                </artifacts>
        </nodeInfo>"""
    }

    def addNetworkElementMo() {
        final Map<String, Object> networkElementAttributes = new HashMap<String, Object>()
        networkElementAttributes.put("ossModelIdentity", NODE_IDENTIFIER)
        networkElementAttributes.put("neType", RADIO_NODE)

        dps.addManagedObject()
                .withFdn("NetworkElement=" + NODE_NAME)
                .namespace("OSS_NE_DEF")
                .version("2.0.0")
                .target(persistanceObject)
                .name(NODE_NAME)
                .addAttributes(networkElementAttributes)
                .build()
    }

    private SchemaData createSchemaData(final String artifactName, final Path schemaPath) throws IOException  {
        final byte[] artifactFileContents = Files.readAllBytes(schemaPath);
        return new SchemaData(artifactName, "", "", artifactFileContents, "");
    }
}
