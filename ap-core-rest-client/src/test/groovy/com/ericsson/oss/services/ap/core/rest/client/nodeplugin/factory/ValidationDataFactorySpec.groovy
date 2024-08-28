/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.client.nodeplugin.factory

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.DataBucket
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.sdk.core.retry.classic.RetryManagerBean
import com.ericsson.oss.itpf.sdk.resources.FileResourceBuilder
import com.ericsson.oss.itpf.sdk.resources.Resource
import com.ericsson.oss.itpf.sdk.resources.Resources
import com.ericsson.oss.services.ap.api.exception.ApApplicationException
import com.ericsson.oss.services.ap.common.artifacts.ArtifactFileFormat
import com.ericsson.oss.services.ap.common.artifacts.UpgradePackageProductDetails
import com.ericsson.oss.services.ap.common.artifacts.ArtifactDetails.ArtifactBuilder
import com.ericsson.oss.services.ap.common.artifacts.raw.RawArtifactHandler
import com.ericsson.oss.services.ap.common.artifacts.util.ArtifactResourceOperations
import com.ericsson.oss.services.ap.common.artifacts.util.ShmDetailsRetriever
import com.ericsson.oss.services.ap.core.rest.client.shm.ShmRestClient

class ValidationDataFactorySpec extends CdiSpecification {

    private static final String SITE_EQUIPMENT = "SiteEquipment"
    private static final String SITE_BASIC = "SiteBasic"
    private static final String UNLOCK_CELLS = "unlockCell"
    private static final String NODE_CONFIGURATION = "nodeConfiguration"
    private static final String OPTIONAL_FEATURE = "optionalFeature"
    private static final String NODE_NAME = "LTE01dg2ERBS00001"
    private static final String AP_NODE_FDN = "Project=RadioNodeECTValidSEValidSB,Node=LTE01dg2ERBS00001"
    private static final String NODE_TYPE = "RadioNode"
    private static final String UPGRADE_PACKAGE_NAME = "RadioNode R29A89 release upgrade package"
    private static final String PRODUCT_NUMBER = "CXP9024418/6"
    private static final String PRODUCT_REVISION = "R29A89"
    private static final String UPGRADE_PACKAGE_PATH = "/home/smrs/smrsroot/software/radionode/RadioNode_R29A89_release_upgrade_package"
    private static final String PRECONFIGURATION_FILE_PATH = "/ericsson/autoprovisioning/artifacts/generated/RadioNodeECTValidSEValidSB/LTE01dg2ERBS00001/preconfiguration_LTE01dg2ERBS00001.xml"
    private static final String PRECONFIGURATION_FILE_CONTENT = """<ManagedElement xmlns=\"urn:com:ericsson:ecim:ComTop\">"
                                        "<managedElementId>LTE01dg2ERBS00001</managedElementId>"
                                        "</ManagedElement>""";
    private static final String INVALID_PRECONFIGURATION_FILE_CONTENT_1 = """<RootTag1 xmlns=\"urn:com:ericsson:ecim:ComTop\">"
                                        "<managedElementId>LTE01dg2ERBS00001</managedElementId>"
                                        "</RootTag1>""";
    private static final String INVALID_PRECONFIGURATION_FILE_CONTENT_2 = """<ManagedElement xmlns=\"urn:com:ericsson:ecim:ComTop\">"
                                        "<managedElementId>LTE01dg2ERBS00001</managedElementId>""";

    @ObjectUnderTest
    private ValidationDataFactory dataFactory

    @MockedImplementation
    private RawArtifactHandler mockRawArtifactHandler

    @MockedImplementation
    private ShmRestClient mockShmRestClient

    @MockedImplementation
    private ShmDetailsRetriever mockShmDetailsRetriever

    @MockedImplementation
    private ArtifactResourceOperations mockArtifactResourceOperations

    @MockedImplementation
    private FileResourceBuilder mockResourceBuilder

    @MockedImplementation
    private Resource mockResource

    @MockedImplementation
    private DataPersistenceService mockDps;

    @MockedImplementation
    private DataBucket mockDataBucket

    @MockedImplementation
    private ManagedObject mockNodeMo

    @MockedImplementation
    private ManagedObject mockNodeChildMo

    def siteBasicArtifact = new ArtifactBuilder()
                                .apNodeFdn(AP_NODE_FDN)
                                .name("SiteBasic.xml")
                                .artifactContent("<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"ValidationDataFactory UT\">")
                                .fileFormat(ArtifactFileFormat.NETCONF)
                                .build()

    def siteEquipmentArtifact = new ArtifactBuilder()
                                .apNodeFdn(AP_NODE_FDN)
                                .name("SiteEquipment.xml")
                                .artifactContent("<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"ValidationDataFactory UT\">")
                                .fileFormat(ArtifactFileFormat.NETCONF)
                                .build()

    def static radioArtifact = new ArtifactBuilder()
                                .apNodeFdn(AP_NODE_FDN)
                                .name("radio.xml")
                                .type(NODE_CONFIGURATION)
                                .artifactContent("<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"ValidationDataFactory UT\">")
                                .fileFormat(ArtifactFileFormat.NETCONF)
                                .build()

    def static transportArtifact = new ArtifactBuilder()
                                .apNodeFdn(AP_NODE_FDN)
                                .name("transport.xml")
                                .type(NODE_CONFIGURATION)
                                .artifactContent("ValidationDataFactory UT")
                                .fileFormat(ArtifactFileFormat.BULK_3GPP)
                                .build()
    def nodeConfigurationList = new ArrayList<>()

    def optionalFeatureArtifact = new ArtifactBuilder()
                                .apNodeFdn(AP_NODE_FDN)
                                .name("optionalFeature.xml")
                                .type(OPTIONAL_FEATURE)
                                .artifactContent("<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"ValidationDataFactory UT\">")
                                .fileFormat(ArtifactFileFormat.NETCONF)
                                .build()
    def optionalFeatureList = new ArrayList<>()

    def unlockCellArtifact = new ArtifactBuilder()
                                .apNodeFdn(AP_NODE_FDN)
                                .name("unlockCell.xml")
                                .type(UNLOCK_CELLS)
                                .artifactContent("<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"ValidationDataFactory UT\">")
                                .fileFormat(ArtifactFileFormat.NETCONF)
                                .build()
    def unlockCellList = new ArrayList<>()

    def productDetails = new UpgradePackageProductDetails()

    def "When the product number contains underscores then replace them with forward slash" () {
        given: "A product number contains underscores"
            String productNumber = "CXP9024418_6"

        when: "When we get the product number after validation data creation"
            String result = dataFactory.getProductNumber(productNumber)

        then: "The result contains forward slash"
            result == "CXP9024418/6"
    }

    def "Verify getNetconfNodeConfigurationFiles method can get all configuration files when the artifacts are in Netconf format" () {
        given: "Three different types of configurations artifact in Netconf format will be imported"
            nodeConfigurationList.add(radioArtifact)
            optionalFeatureList.add(optionalFeatureArtifact)
            unlockCellList.add(unlockCellArtifact)
            mockRawArtifactHandler.readAllOfType(AP_NODE_FDN, NODE_CONFIGURATION) >> nodeConfigurationList
            mockRawArtifactHandler.readAllOfType(AP_NODE_FDN, OPTIONAL_FEATURE) >> optionalFeatureList
            mockRawArtifactHandler.readAllOfType(AP_NODE_FDN, UNLOCK_CELLS) >> unlockCellList

        when: "Execute getNetconfNodeConfigurationFiles method in ValidationDataFactory"
            def netconfConfigurations = dataFactory.getNetconfNodeConfigurationFiles(AP_NODE_FDN)

        then: "Three configuration files are retrieved"
            netconfConfigurations.size() == 3
    }

    def "Verify getNetconfNodeConfigurationFiles method gets nothing when the configuration artifact is not in Netconf format" () {
        given: "One configuration artifact in BulkCM format will be imported"
            nodeConfigurationList.add(transportArtifact)
            mockRawArtifactHandler.readAllOfType(AP_NODE_FDN, NODE_CONFIGURATION) >> nodeConfigurationList
            mockRawArtifactHandler.readAllOfType(AP_NODE_FDN, OPTIONAL_FEATURE) >> optionalFeatureList
            mockRawArtifactHandler.readAllOfType(AP_NODE_FDN, UNLOCK_CELLS) >> unlockCellList

        when: "Execute getNetconfNodeConfigurationFiles method in ValidationDataFactory"
            def netconfConfigurations = dataFactory.getNetconfNodeConfigurationFiles(AP_NODE_FDN)

        then: "No configuration file can be retrieved"
            netconfConfigurations.size() == 0
    }

    def "Verify getPreconfigurationFile method can get the result when the preconfiguration file exists" () {
        given: "The preconfiguration file exists"
            mockArtifactResourceOperations.readArtifactAsText(PRECONFIGURATION_FILE_PATH) >> PRECONFIGURATION_FILE_CONTENT

        when: "Execute getPreconfigurationFile method in ValidationDataFactory"
            def preconfigurationFile = dataFactory.getPreconfigurationFile(AP_NODE_FDN)

        then: "The preconfiguration file can be retrieved"
            preconfigurationFile.getFileName().equals("preconfiguration_LTE01dg2ERBS00001.xml")
    }

    def "Verify getPreconfigurationFile method can get the result when the preconfiguration file exists for 2nd try" () {
        given: "The preconfiguration file existing check fails the 1st time but succeeds the 2nd time"
            dataFactory.retryManager = new RetryManagerBean()
            Resources.fileResourceBuilder = this.mockResourceBuilder
            mockResourceBuilder.getResource(_) >> mockResource
            mockResource.exists() >>> [false, true]
        and: "The preconfiguration file content can be read"
            mockArtifactResourceOperations.readArtifactAsText(PRECONFIGURATION_FILE_PATH) >> PRECONFIGURATION_FILE_CONTENT

        when: "Execute getPreconfigurationFile method in ValidationDataFactory"
            def preconfigurationFile = dataFactory.getPreconfigurationFile(AP_NODE_FDN)

        then: "The preconfiguration file can be retrieved"
            preconfigurationFile.getFileName().equals("preconfiguration_LTE01dg2ERBS00001.xml")

        cleanup:
            Resources.fileResourceBuilder = null
    }

    def "Verify getPreconfigurationFile method fail to get the result when the preconfiguration file check fails for all retries" () {
        given: "The preconfiguration file existing check fails all retries"
            dataFactory.retryManager = new RetryManagerBean()
            Resources.fileResourceBuilder = this.mockResourceBuilder
            mockResourceBuilder.getResource(_) >> mockResource
            mockResource.exists() >> false

        when: "Execute getPreconfigurationFile method in ValidationDataFactory"
            def preconfigurationFile = dataFactory.getPreconfigurationFile(AP_NODE_FDN)

        then: "The preconfiguration file can not be retrieved, exception is thrown"
            preconfigurationFile == null
            ApApplicationException exception = thrown()
            exception.getMessage().toString().contains("Failed to get node configuration required for NETCONF validation.")

        cleanup:
            Resources.fileResourceBuilder = null
    }

    def "Verify validation data is not built and exception is thrown when preconfiguration file root tag is not ManagedElement" () {
        given: "Preconfiguration file is invalid xml format"
            mockShmRestClient.getUpgradePackageName(NODE_NAME, NODE_TYPE) >> UPGRADE_PACKAGE_NAME
            mockArtifactResourceOperations.readArtifactAsText(_) >> INVALID_PRECONFIGURATION_FILE_CONTENT_1

        when: "Execute createDeltaValidationData method in ValidationDataFactory"
            def validationData = dataFactory.createDeltaValidationData(AP_NODE_FDN, NODE_TYPE)

        then: "Validation data is not built, an ApApplicationException is thrown with specific info"
            validationData == null
            ApApplicationException exception = thrown()
            exception.getMessage().toString().contains("Failed to get node configuration required for NETCONF validation.")
    }

    def "Verify validation data is not built and exception is thrown when preconfiguration file content is not XML format" () {
        given: "Preconfiguration file is blank"
            mockShmRestClient.getUpgradePackageName(NODE_NAME, NODE_TYPE) >> UPGRADE_PACKAGE_NAME
            mockArtifactResourceOperations.readArtifactAsText(_) >> INVALID_PRECONFIGURATION_FILE_CONTENT_2

        when: "Execute createDeltaValidationData method in ValidationDataFactory"
            def validationData = dataFactory.createDeltaValidationData(AP_NODE_FDN, NODE_TYPE)

        then: "Validation data is not built, an ApApplicationException is thrown with specific info"
            validationData == null
            ApApplicationException exception = thrown()
            exception.getMessage().toString().contains("Failed to get node configuration required for NETCONF validation.")
    }

    def "Verify validation data is built when all data are provided" () {
        given: "Upgrade package data and configurations strict attribute can be retrieved"
            dataFactory.dps = mockDps
            mockDps.getLiveBucket() >> mockDataBucket
            mockDataBucket.findMoByFdn(_) >> mockNodeMo
            mockNodeMo.getChild(_) >> mockNodeChildMo
            mockNodeChildMo.getAttribute(_) >>> [UPGRADE_PACKAGE_NAME, strictAttr]

            productDetails.setProductNumber(PRODUCT_NUMBER)
            productDetails.setProductRevision(PRODUCT_REVISION)
            mockShmDetailsRetriever.getUpgradePackageProductDetails(UPGRADE_PACKAGE_NAME, NODE_TYPE) >> productDetails

            mockShmDetailsRetriever.getUpgradePackageAbsolutePath(UPGRADE_PACKAGE_NAME) >> UPGRADE_PACKAGE_PATH
        and: "Configuration files SiteBasic.xml and SiteEquipment.xml are given"
            mockRawArtifactHandler.readFirstOfType(AP_NODE_FDN, SITE_BASIC) >> siteBasicArtifact
            mockRawArtifactHandler.readFirstOfType(AP_NODE_FDN, SITE_EQUIPMENT) >> siteEquipmentArtifact
        and: "Node configuration file in different format is provided"
            nodeConfigurationList.add(configurationArtifact)
            mockRawArtifactHandler.readAllOfType(AP_NODE_FDN, NODE_CONFIGURATION) >> nodeConfigurationList

        when: "Execute createValidationData method in ValidationDataFactory"
            def validationData = dataFactory.createValidationData(AP_NODE_FDN, NODE_TYPE)

        then: "Validation data is built successfully with expected number of files to be validated"
            numOfFilesToBeValidated == validationData.getConfigurationFiles().size()
            validationData.getProductNumber() == PRODUCT_NUMBER
            validationData.getRevision() == PRODUCT_REVISION

        where:
            strictAttr | configurationArtifact | numOfFilesToBeValidated
               false   |  radioArtifact        |   3
               false   |  transportArtifact    |   2
               true    |  radioArtifact        |   2
    }

    def "Verify validation data is not built and exception is thrown when nodeMo cannot be retrieved" () {
        given: "NodeMo cannot be retrieved"
            dataFactory.dps = mockDps
            mockDps.getLiveBucket() >> mockDataBucket
            mockDataBucket.findMoByFdn(_) >> null

        when: "Execute createValidationData method in ValidationDataFactory"
            def validationData = dataFactory.createValidationData(AP_NODE_FDN, NODE_TYPE)

        then: "Validation data is not built, an ApApplicationException is thrown with specific info"
            validationData == null
            ApApplicationException exception = thrown()
            exception.getMessage().toString().contains("No AP node found for FDN")
    }

    def "Verify UpgradePackage name cannot be retrieved and exception is thrown when autoIntegrationMo is null" () {
        given: "autoIntegrationMo cannot be retrieved"
            dataFactory.dps = mockDps
            mockDps.getLiveBucket() >> mockDataBucket
            mockDataBucket.findMoByFdn(_) >> mockNodeMo
            mockNodeMo.getChild(_) >> null

        when: "Execute retrieveUpgradePackageData method in ValidationDataFactory"
            def UPName = dataFactory.retrieveUpgradePackageData(AP_NODE_FDN)

        then: "UpgradePackage name is null, an ApApplicationException is thrown with specific info"
            UPName == null
            ApApplicationException exception = thrown()
            exception.getMessage().toString().contains("AutoIntegrationOptions child MO not found")
    }

    def "Verify exception is thrown when artifactContainerMo is null" () {
        given: "artifactContainerMo cannot be retrieved"
            dataFactory.dps = mockDps
            mockDps.getLiveBucket() >> mockDataBucket
            mockDataBucket.findMoByFdn(_) >> mockNodeMo
            mockNodeMo.getChild(_) >> null

        when: "Execute isImportConfigurationInStrictSequence method in ValidationDataFactory"
            dataFactory.isImportConfigurationInStrictSequence(AP_NODE_FDN)

        then: "An ApApplicationException is thrown with specific info"
            ApApplicationException exception = thrown()
            exception.getMessage().toString().contains("NodeArtifactContainer child MO not found")
    }
}
