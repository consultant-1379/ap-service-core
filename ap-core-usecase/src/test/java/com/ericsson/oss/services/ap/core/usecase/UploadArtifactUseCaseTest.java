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
package com.ericsson.oss.services.ap.core.usecase;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_IDENTIFIER_VALUE;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NodeDescriptorBuilder.createDefaultNode;
import static com.ericsson.oss.services.ap.model.NodeType.ERBS;
import static com.ericsson.oss.services.ap.model.NodeType.RadioNode;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.UploadArtifactService;
import com.ericsson.oss.services.ap.api.exception.ArtifactDataNotFoundException;
import com.ericsson.oss.services.ap.api.exception.ArtifactNotFoundException;
import com.ericsson.oss.services.ap.api.exception.IllegalUploadNodeStateException;
import com.ericsson.oss.services.ap.api.exception.UnsupportedArtifactTypeException;
import com.ericsson.oss.services.ap.api.exception.ValidationException;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.api.resource.ResourceService;
import com.ericsson.oss.services.ap.api.schema.SchemaData;
import com.ericsson.oss.services.ap.api.schema.SchemaService;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.api.workflow.NodePluginCapabilityValidationService;
import com.ericsson.oss.services.ap.api.workflow.ValidationConfigurationService;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactFileFormat;
import com.ericsson.oss.services.ap.common.artifacts.util.ArtifactResourceOperations;
import com.ericsson.oss.services.ap.common.artifacts.util.NodeArtifactMoOperations;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.model.NodeArtifactContainerAttribute;
import com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor;
import com.ericsson.oss.services.ap.common.test.stubs.dps.StubbedDpsGenerator;
import com.ericsson.oss.services.ap.common.util.xml.XmlValidator;
import com.ericsson.oss.services.ap.common.util.xml.exception.SchemaValidationException;
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;
import com.ericsson.oss.services.ap.common.validation.configuration.ConfigurationFileValidator;
import com.ericsson.oss.services.ap.common.workflow.recording.ErrorRecorder;

/**
 * Unit tests for {@link UploadArtifactUseCase}.
 */
@RunWith(MockitoJUnitRunner.class)
public class UploadArtifactUseCaseTest { //NOPMD

    private static final String SITE_BASIC = "siteBasic";
    private static final String SITE_EQUIPMENT = "siteEquipment";
    private static final String SITE_INSTALL = "siteInstall";
    private static final String VNF_DESCRIPTOR = "virtualNetworkDescriptor";
    private static final String SITE_BASIC_XML = "siteBasic.xml";
    private static final String SITE_EQUIPMENT_XML = "siteEquipment.xml";
    private static final String SITE_INSTALL_XML = "siteInstall.xml";
    private static final String VNF_DESCRIPTOR_XML = "vnfDescriptor.xml";
    private static final String LICENSE_KEY_FILE_ZIP = "licenseKeyFile.zip";
    private static final String LICENSE_FILE = "licenseFile";

    private static final String PRE_MIGRATION_CONFIGURATION = "preMigrationConfiguration";
    private static final String LOCK_G2_CELLS_XML = "lockG2Cells.xml";
    private static final String CONFIGURATION = "configuration";
    private static final String TN_DATA_NETCONF_XML = "TN_Data_Netconf.xml";
    private static final String RN_DATA_XML = "RN_Data.xml";
    private static final String TN_DATA_BULKCM_XML = "TN_Data_BulkCM.xml";
    private static final String AMOS_SCRIPT_MOS = "AMOS_Script.mos";

    private static final String RAW_LOCATION = "/raw/";
    private static final String FILECONTENT = "<?xml version>";
    private static final String AMOS_SCRIPT_CONTENT = "get 0";
    private static final String NETCONFFILECONTENT = "<?xml version>" +
            "<hello>\n" +
            "</hello>\n" +
            "]]>]]>\n" +
            "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "   <edit-config>\n" +
            "      <target>\n" +
            "         <running/>\n" +
            "      </target>\n" +
            "      <config>\n" +
            "      </config>\n" +
            "   </edit-config>\n" +
            "</rpc>\n" +
            "]]>]]>";
    private static final String ARTIFACTS_XSD = "Artifacts.xsd";
    private static final String SCHEMA_LOCATION = "/schema_location";

    private static final String NON_EXISTING_FILENAME_XML = "non-existing-filename.xml";

    private static final String ARTIFACTS_SCHEMA = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" + "<xs:complexType name=\"Artifacts\">"
        + "<xs:all>" + "<xs:element name=\"siteBasic\" type=\"xs:string\" minOccurs=\"1\" />"
        + "<xs:element name=\"siteEquipment\" type=\"xs:string\" minOccurs=\"1\" />"
        + "<xs:element name=\"virtualNetworkDescriptor\" type=\"xs:string\" minOccurs=\"1\" />"
        + "<xs:element name=\"siteInstallation\" type=\"xs:string\" minOccurs=\"1\" />"
        + "<xs:element name=\"configurations\" type=\"configurations\" minOccurs=\"1\" />"
        + "</xs:all> </xs:complexType>" + "<xs:complexType name=\"configurations\">" + "<xs:annotation>" + "<xs:documentation>"
        + "Bulk configuration files to be imported during integration. The config files will be imported in the order in which they are declared."
        + "</xs:documentation>" + "</xs:annotation>"
        + "<xs:sequence>" + "<xs:element name=\"configuration\" type=\"xs:string\" minOccurs=\"1\" maxOccurs=\"unbounded\" />" + "</xs:sequence>"
        + "</xs:complexType>" + "</xs:schema>";

    private static final SchemaData ARTIFACTS_SCHEMA_DATA = new SchemaData(ARTIFACTS_XSD, "SCHEMA", "", ARTIFACTS_SCHEMA.getBytes(), SCHEMA_LOCATION);
    private static final SchemaData DUMMY_ERBS_SCHEMA_DATA = new SchemaData("Erbs.xsd", "SCHEMA", "", "schema_erbs".getBytes(), SCHEMA_LOCATION);

    @Mock
    private Logger logger; // NOPMD

    @Mock
    private ResourceService resourceService;

    @Mock
    private ConfigurationFileValidator configurationFileValidator;

    @Mock
    private ValidationConfigurationService validationConfigurationService;

    @Mock
    private SchemaService schemaService;

    @Mock
    private XmlValidator xmlValidator;

    @Mock
    private NodeTypeMapper nodeTypeMapper;

    @Mock
    private ArtifactResourceOperations resourceOperations;

    @Mock
    private ServiceFinderBean serviceFinder;

    @Mock
    private UploadArtifactService uploadResolver;

    @Mock
    private ErrorRecorder errorRecorder; //NOPMD

    @InjectMocks
    private DpsQueries dpsQueries;

    @InjectMocks
    private NodeArtifactMoOperations artifactOperations;

    @InjectMocks
    @Spy
    private UploadArtifactUseCase uploadArtifactUseCase;

    @Mock
    private DpsOperations dps;

    @Mock
    private NodePluginCapabilityValidationService nodePluginCapabilityValidationService;

    private final List<SchemaData> schemas = new ArrayList<>();
    private final StubbedDpsGenerator dpsGenerator = new StubbedDpsGenerator();
    private final Set<String> supportedUploadTypes = new HashSet<>();
    private final List<String> nodeFileArtifacts = new ArrayList<>();
    private final Set<String> validStates = new HashSet<>();

    @Before
    public void setUp() {
        final DataPersistenceService dpservice = dpsGenerator.getStubbedDps();
        Whitebox.setInternalState(dps, "dps", dpservice);
        Whitebox.setInternalState(uploadArtifactUseCase, "dps", dps);
        Whitebox.setInternalState(uploadArtifactUseCase, "artifactOperations", artifactOperations);
        Whitebox.setInternalState(artifactOperations, "dps", dpservice);
        Whitebox.setInternalState(artifactOperations, "dpsQueries", dpsQueries);
        Whitebox.setInternalState(dpsQueries, "dps", dpservice);

        when(dps.getDataPersistenceService()).thenReturn(dpservice);
        when(nodeTypeMapper.getInternalRepresentationFor(ERBS.toString())).thenReturn(ERBS.toString());
        when(nodeTypeMapper.getInternalEjbQualifier(ERBS.toString())).thenReturn(ERBS.toString());
        when(nodeTypeMapper.getInternalRepresentationFor(RadioNode.toString())).thenReturn(RadioNode.toString());
        when(nodeTypeMapper.getInternalEjbQualifier(RadioNode.toString())).thenReturn(RadioNode.toString());

        schemas.add(ARTIFACTS_SCHEMA_DATA);
        schemas.add(DUMMY_ERBS_SCHEMA_DATA);
        when(schemaService.readSchemas(ERBS.toString())).thenReturn(schemas);

        when(serviceFinder.find(UploadArtifactService.class, ERBS.toString())).thenReturn(uploadResolver);
        when(serviceFinder.find(UploadArtifactService.class, RadioNode.toString())).thenReturn(uploadResolver);
        when(serviceFinder.find(ValidationConfigurationService.class)).thenReturn(validationConfigurationService);
        when(serviceFinder.find(NodePluginCapabilityValidationService.class)).thenReturn(nodePluginCapabilityValidationService);

        supportedUploadTypes.add("preMigrationConfiguration");
        supportedUploadTypes.add("siteBasic");
        supportedUploadTypes.add("virtualNetworkDescriptor");
        supportedUploadTypes.add("siteEquipment");
        supportedUploadTypes.add("configuration");
        supportedUploadTypes.add(LICENSE_FILE);
        nodeFileArtifacts.add("siteBasic");
        nodeFileArtifacts.add("siteEquipment");
        nodeFileArtifacts.add(LICENSE_FILE);
        validStates.add(State.ORDER_COMPLETED.toString());
        validStates.add(State.EXPANSION_FAILED.toString());
        validStates.add(State.ORDER_FAILED.toString());
        validStates.add(State.EXPANSION_SUSPENDED.toString());
        validStates.add(State.PRE_MIGRATION_FAILED.toString());
        validStates.add(State.PRE_MIGRATION_SUSPENDED.toString());
        validStates.add(State.INTEGRATION_IMPORT_CONFIGURATION_SUSPENDED.toString());
        validStates.add(State.EXPANSION_IMPORT_CONFIGURATION_SUSPENDED.toString());
        validStates.add(State.PRE_MIGRATION_IMPORT_CONFIGURATION_SUSPENDED.toString());
        validStates.add(State.MIGRATION_IMPORT_CONFIGURATION_SUSPENDED.toString());

        when(uploadResolver.getSupportedUploadTypes()).thenReturn(supportedUploadTypes);
        when(uploadResolver.isNodeArtifactFile(SITE_BASIC)).thenReturn(true);
        when(uploadResolver.getValidStatesForUpload(anyString())).thenReturn(validStates);
        when(resourceOperations.readArtifactFileFormat(anyString(), anyString().getBytes())).thenReturn(ArtifactFileFormat.BULK_3GPP);
    }

    @Test
    public void when_uploading_a_file_of_configuration_artifact_to_replace_existing_file_on_an_existing_node_then_upload_succeeds() {
        final NodeDescriptor nodeDescriptor = createNodeDescriptorWithArtifacts(State.ORDER_COMPLETED);
        dpsGenerator.generate(nodeDescriptor);
        when(configurationFileValidator.validateFile(anyString(), anyString(), anyString(), any(ArchiveArtifact.class)))
        .thenReturn(Collections.<String>emptyList());

        uploadArtifactUseCase.execute(nodeDescriptor.getNodeFdn(), TN_DATA_BULKCM_XML, FILECONTENT.getBytes());
        verify(resourceOperations).writeArtifact(RAW_LOCATION + TN_DATA_BULKCM_XML, FILECONTENT.getBytes());
    }

    @Test
    public void whenUploadingANetconfFileOfConfigurationArtifactToReplaceExistingFileOnAnExistingNodeThenUploadSucceeds() {
        final NodeDescriptor nodeDescriptor = createNodeDescriptorWithArtifacts(State.ORDER_COMPLETED);
        dpsGenerator.generate(nodeDescriptor);
        when(resourceOperations.readArtifactFileFormat(anyString(), anyString().getBytes())).thenReturn(ArtifactFileFormat.NETCONF);

        uploadArtifactUseCase.execute(nodeDescriptor.getNodeFdn(), TN_DATA_NETCONF_XML, NETCONFFILECONTENT.getBytes());
        verify(resourceOperations).writeArtifact(RAW_LOCATION + TN_DATA_NETCONF_XML, NETCONFFILECONTENT.getBytes());
        verify(validationConfigurationService, never()).validateConfiguration(anyString(), anyString());
    }

    @Test
    public void whenUploadingANetconfFileOfConfigurationArtifactToReplaceExistingFileOnAnExistingRadioNodeForExpansionValidationRequiredThenUploadSucceedsWithValidation() {
        final NodeDescriptor nodeDescriptor = createNodeDescriptorWithArtifactsForExpansion(State.EXPANSION_FAILED);
        dpsGenerator.generate(nodeDescriptor);
        when(resourceOperations.readArtifactFileFormat(anyString(), anyString().getBytes())).thenReturn(ArtifactFileFormat.NETCONF);
        when(resourceService.exists(anyString())).thenReturn(true);
        when(validationConfigurationService.validateDeltaConfiguration(anyString(), anyString())).thenReturn("");
        when(nodePluginCapabilityValidationService.validateCapability(any(), any(), any())).thenReturn(true);

        try {
            uploadArtifactUseCase.execute(nodeDescriptor.getNodeFdn(), TN_DATA_NETCONF_XML, NETCONFFILECONTENT.getBytes("UTF-8"));
            verify(resourceOperations).writeArtifact(RAW_LOCATION + TN_DATA_NETCONF_XML, NETCONFFILECONTENT.getBytes("UTF-8"));
            verify(validationConfigurationService).validateDeltaConfiguration(anyString(), anyString());
        } catch (final UnsupportedEncodingException ex) {
            throw new AssertionError(ex);
        }
    }

    @Test
    public void whenUploadingANetconfFileOfConfigurationArtifactToReplaceExistingFileOnAnExistingRadioNodeForExpansionValidationNotRequiredThenUploadSucceedsWithoutValidation() {
        final NodeDescriptor nodeDescriptor = createNodeDescriptorWithArtifactsForExpansion(State.EXPANSION_SUSPENDED);
        dpsGenerator.generate(nodeDescriptor);
        when(resourceOperations.readArtifactFileFormat(anyString(), anyString().getBytes())).thenReturn(ArtifactFileFormat.NETCONF);
        when(resourceService.exists(anyString())).thenReturn(false);
        when(nodePluginCapabilityValidationService.validateCapability(any(), any(), any())).thenReturn(true);

        try {
            uploadArtifactUseCase.execute(nodeDescriptor.getNodeFdn(), TN_DATA_NETCONF_XML, NETCONFFILECONTENT.getBytes("UTF-8"));
            verify(resourceOperations).writeArtifact(RAW_LOCATION + TN_DATA_NETCONF_XML, NETCONFFILECONTENT.getBytes("UTF-8"));
            verify(validationConfigurationService, never()).validateDeltaConfiguration(anyString(), anyString());
            verify(validationConfigurationService, never()).validateConfiguration(anyString(), anyString());
        } catch (final UnsupportedEncodingException ex) {
            throw new AssertionError(ex);
        }
    }

    @Test
    public void whenUploadingANetconfFileOfConfigurationArtifactToReplaceExistingFileOnAnExistingRadioNodeForGreenFieldNodePluginCapabliitySupportedThenUploadSucceedsWithValidation() {
        final NodeDescriptor nodeDescriptor = createNodeDescriptorWithArtifactsForGreenField(State.ORDER_FAILED);
        dpsGenerator.generate(nodeDescriptor);
        when(resourceOperations.readArtifactFileFormat(anyString(), anyString().getBytes())).thenReturn(ArtifactFileFormat.NETCONF);
        when(resourceService.exists(anyString())).thenReturn(false);
        when(validationConfigurationService.validateConfiguration(anyString(), anyString())).thenReturn("");
        when(nodePluginCapabilityValidationService.validateCapability(any(), any(), any())).thenReturn(true);
        when(validationConfigurationService.validateConfiguration(anyString(), anyString())).thenReturn("Warning message");

        try {
            uploadArtifactUseCase.execute(nodeDescriptor.getNodeFdn(), TN_DATA_NETCONF_XML, NETCONFFILECONTENT.getBytes("UTF-8"));
            verify(resourceOperations).writeArtifact(RAW_LOCATION + TN_DATA_NETCONF_XML, NETCONFFILECONTENT.getBytes("UTF-8"));
            verify(validationConfigurationService).validateConfiguration(anyString(), anyString());
            verify(logger).warn(anyString(), anyString(), anyString());
        } catch (final UnsupportedEncodingException ex) {
            throw new AssertionError(ex);
        }
    }

    @Test
    public void whenUploadingANetconfFileOfConfigurationArtifactToReplaceExistingFileOnAnExistingRadioNodeForGreenFieldNodePluginCapabliityNotSupportedThenUploadSucceedsWithoutValidation() {
        final NodeDescriptor nodeDescriptor = createNodeDescriptorWithArtifactsForGreenField(State.ORDER_FAILED);
        dpsGenerator.generate(nodeDescriptor);
        when(resourceOperations.readArtifactFileFormat(anyString(), anyString().getBytes())).thenReturn(ArtifactFileFormat.NETCONF);
        when(resourceService.exists(anyString())).thenReturn(false);
        when(validationConfigurationService.validateConfiguration(anyString(), anyString())).thenReturn("");
        when(nodePluginCapabilityValidationService.validateCapability(any(), any(), any())).thenReturn(false);

        try {
            uploadArtifactUseCase.execute(nodeDescriptor.getNodeFdn(), TN_DATA_NETCONF_XML, NETCONFFILECONTENT.getBytes("UTF-8"));
            verify(resourceOperations).writeArtifact(RAW_LOCATION + TN_DATA_NETCONF_XML, NETCONFFILECONTENT.getBytes("UTF-8"));
            verify(validationConfigurationService, never()).validateConfiguration(anyString(), anyString());
        } catch (final UnsupportedEncodingException ex) {
            throw new AssertionError(ex);
        }
    }

    @Test
    public void whenUploadingANetconfFileWithImportConfigurationInStrictSequenceToReplaceExistingFileOnAnExistingRadioNodeForGreenFieldThenUploadSucceedsWithoutValidation() {
        final NodeDescriptor nodeDescriptor = createNodeDescriptorWithArtifactsAndStrictSequenceForGreenField(State.ORDER_FAILED);
        dpsGenerator.generate(nodeDescriptor);
        when(resourceOperations.readArtifactFileFormat(anyString(), anyString().getBytes())).thenReturn(ArtifactFileFormat.NETCONF);

        try {
            uploadArtifactUseCase.execute(nodeDescriptor.getNodeFdn(), TN_DATA_NETCONF_XML, NETCONFFILECONTENT.getBytes("UTF-8"));
            verify(resourceOperations).writeArtifact(RAW_LOCATION + TN_DATA_NETCONF_XML, NETCONFFILECONTENT.getBytes("UTF-8"));
            verify(nodePluginCapabilityValidationService, never()).validateCapability(any(), any(), any());
            verify(validationConfigurationService, never()).validateConfiguration(anyString(), anyString());
        } catch (final UnsupportedEncodingException ex) {
            throw new AssertionError(ex);
        }
    }

    @Test
    public void whenUploadingSiteBasicWithImportConfigurationInStrictSequenceToReplaceExistingFileOnAnExistingRadioNodeForGreenFieldThenUploadSucceedsWithValidation() {
        final NodeDescriptor nodeDescriptor = createNodeDescriptorWithArtifactsAndStrictSequenceForGreenField(State.ORDER_FAILED);
        dpsGenerator.generate(nodeDescriptor);
        when(resourceOperations.readArtifactFileFormat(anyString(), anyString().getBytes())).thenReturn(ArtifactFileFormat.NETCONF);
        when(nodePluginCapabilityValidationService.validateCapability(any(), any(), any())).thenReturn(true);

        try {
            uploadArtifactUseCase.execute(nodeDescriptor.getNodeFdn(), SITE_BASIC_XML, NETCONFFILECONTENT.getBytes("UTF-8"));
            verify(resourceOperations).writeArtifact(RAW_LOCATION + SITE_BASIC_XML, NETCONFFILECONTENT.getBytes("UTF-8"));
            verify(validationConfigurationService).validateConfiguration(anyString(), anyString());
        } catch (final UnsupportedEncodingException ex) {
            throw new AssertionError(ex);
        }
    }

    @Test
    public void whenUploadingSiteEquipmentWithImportConfigurationInStrictSequenceToReplaceExistingFileOnAnExistingRadioNodeForGreenFieldThenUploadSucceedsWithValidation() {
        final NodeDescriptor nodeDescriptor = createNodeDescriptorWithArtifactsAndStrictSequenceForGreenField(State.ORDER_FAILED);
        dpsGenerator.generate(nodeDescriptor);
        when(resourceOperations.readArtifactFileFormat(anyString(), anyString().getBytes())).thenReturn(ArtifactFileFormat.NETCONF);
        when(nodePluginCapabilityValidationService.validateCapability(any(), any(), any())).thenReturn(true);

        try {
            uploadArtifactUseCase.execute(nodeDescriptor.getNodeFdn(), SITE_EQUIPMENT_XML, NETCONFFILECONTENT.getBytes("UTF-8"));
            verify(resourceOperations).writeArtifact(RAW_LOCATION + SITE_EQUIPMENT_XML, NETCONFFILECONTENT.getBytes("UTF-8"));
            verify(validationConfigurationService).validateConfiguration(anyString(), anyString());
        } catch (final UnsupportedEncodingException ex) {
            throw new AssertionError(ex);
        }
    }

    @Test
    public void whenUploadingANetconfFileOfPreMigrationConfigurationToReplaceExistingFileOnAnExistingRadioNodeForMigrationNodePluginCapabilitySupportedThenUploadSucceedsWithoutValidation() {
        final NodeDescriptor nodeDescriptor = createNodeDescriptorWithArtifactsForMigration(State.PRE_MIGRATION_FAILED);
        dpsGenerator.generate(nodeDescriptor);
        when(resourceOperations.readArtifactFileFormat(anyString(), anyString().getBytes())).thenReturn(ArtifactFileFormat.NETCONF);
        when(resourceService.exists(anyString())).thenReturn(false);

        try {
            uploadArtifactUseCase.execute(nodeDescriptor.getNodeFdn(), LOCK_G2_CELLS_XML, NETCONFFILECONTENT.getBytes("UTF-8"));
            verify(resourceOperations).writeArtifact(RAW_LOCATION + LOCK_G2_CELLS_XML, NETCONFFILECONTENT.getBytes("UTF-8"));
            verify(validationConfigurationService, never()).validateConfiguration(anyString(), anyString());
        } catch (final UnsupportedEncodingException ex) {
            throw new AssertionError(ex);
        }
    }

    @Test
    public void whenUploadingANetconfFileOfMigrationConfigurationToReplaceExistingFileOnAnExistingRadioNodeForMigrationNodePluginCapabilitySupportedThenUploadSucceedsWithValidation() {
        final NodeDescriptor nodeDescriptor = createNodeDescriptorWithArtifactsForMigration(State.PRE_MIGRATION_FAILED);
        dpsGenerator.generate(nodeDescriptor);
        when(resourceOperations.readArtifactFileFormat(anyString(), anyString().getBytes())).thenReturn(ArtifactFileFormat.NETCONF);
        when(resourceService.exists(anyString())).thenReturn(false);
        when(nodePluginCapabilityValidationService.validateCapability(any(), any(), any())).thenReturn(true);

        try {
            uploadArtifactUseCase.execute(nodeDescriptor.getNodeFdn(), TN_DATA_NETCONF_XML, NETCONFFILECONTENT.getBytes("UTF-8"));
            verify(resourceOperations).writeArtifact(RAW_LOCATION + TN_DATA_NETCONF_XML, NETCONFFILECONTENT.getBytes("UTF-8"));
            verify(validationConfigurationService).validateConfiguration(anyString(), anyString());
        } catch (final UnsupportedEncodingException ex) {
            throw new AssertionError(ex);
        }
    }

    @Test
    public void verifyNodePluginCapabilityValidationServiceisSetCorrectly() {
        uploadArtifactUseCase.init();
        verify(serviceFinder).find(ResourceService.class);
        verify(serviceFinder).find(ValidationConfigurationService.class);
        verify(serviceFinder).find(NodePluginCapabilityValidationService.class);

    }
    @Test
    public void when_uploading_a_file_to_replace_non_existing_file_on_an_existing_node_then_upload_fails() {
        final NodeDescriptor nodeDescriptor = createNodeDescriptorWithArtifacts(State.ORDER_COMPLETED);
        dpsGenerator.generate(nodeDescriptor);

        try {
            uploadArtifactUseCase.execute(nodeDescriptor.getNodeFdn(), NON_EXISTING_FILENAME_XML, FILECONTENT.getBytes());
            fail("An exception should've been thrown here");
        } catch (final ArtifactNotFoundException ex) {
            assertTrue(ex.getMessage().contains("No node artifact found with fileName:" + NON_EXISTING_FILENAME_XML));
        }
    }

    @Test
    public void when_uploading_a_file_that_is_not_a_supported_upload_type_on_an_existing_node_then_upload_fails() {
        final NodeDescriptor nodeDescriptor = createNodeDescriptorWithArtifacts(State.ORDER_COMPLETED);
        dpsGenerator.generate(nodeDescriptor);

        try {
            uploadArtifactUseCase.execute(nodeDescriptor.getNodeFdn(), SITE_INSTALL_XML, FILECONTENT.getBytes());
            fail("An exception should've been thrown here");
        } catch (final UnsupportedArtifactTypeException ex) {
            assertTrue(ex.getMessage().contains("Upload not supported for file type"));
        }
    }

    @Test
    public void when_uploading_a_file_that_does_not_require_artifact_to_be_generated_then_upload_succeeds() {
        final NodeDescriptor nodeDescriptor = createNodeDescriptorWithArtifacts(State.ORDER_COMPLETED);
        dpsGenerator.generate(nodeDescriptor);
        doThrow(new UnsupportedOperationException()).when(uploadResolver).createGeneratedArtifact(VNF_DESCRIPTOR, nodeDescriptor.getNodeFdn());
        uploadArtifactUseCase.execute(nodeDescriptor.getNodeFdn(), VNF_DESCRIPTOR_XML, FILECONTENT.getBytes());
        verify(resourceOperations).writeArtifact(RAW_LOCATION + VNF_DESCRIPTOR_XML, FILECONTENT.getBytes());
    }

    @Test
    public void when_uploading_an_amos_script_that_is_contained_in_project_then_upload_succeeds() {
        final NodeDescriptor nodeDescriptor = createNodeDescriptorWithArtifacts(State.ORDER_COMPLETED);
        dpsGenerator.generate(nodeDescriptor);
        when(resourceOperations.readArtifactFileFormat(anyString(), anyString().getBytes())).thenReturn(ArtifactFileFormat.AMOS_SCRIPT);
        when(resourceService.exists(anyString())).thenReturn(true);

        uploadArtifactUseCase.execute(nodeDescriptor.getNodeFdn(), AMOS_SCRIPT_MOS, AMOS_SCRIPT_CONTENT.getBytes());
    }

    @Test
    public void when_uploading_an_amos_script_that_is_not_contained_in_project_then_upload_fails() {
        final NodeDescriptor nodeDescriptor = createNodeDescriptorWithArtifacts(State.ORDER_COMPLETED);
        dpsGenerator.generate(nodeDescriptor);
        when(resourceOperations.readArtifactFileFormat(anyString(), anyString().getBytes())).thenReturn(ArtifactFileFormat.AMOS_SCRIPT);
        when(resourceService.exists(anyString())).thenReturn(false);

        try {
            uploadArtifactUseCase.execute(nodeDescriptor.getNodeFdn(), AMOS_SCRIPT_MOS, AMOS_SCRIPT_CONTENT.getBytes());
        } catch (final Exception ex) {
            assertTrue(ex.getMessage().contains("Upload not supported"));
        }
    }

    @Test
    public void when_uploading_a_file_and_any_exception_is_thrown_then_exception_is_instance_of_UnsupportedOperationException() {
        final NodeDescriptor nodeDescriptor = createNodeDescriptorWithArtifacts(State.ORDER_COMPLETED);
        dpsGenerator.generate(nodeDescriptor);
        doThrow(new ValidationException(new UnsupportedOperationException())).when(uploadResolver).createGeneratedArtifact(VNF_DESCRIPTOR, nodeDescriptor.getNodeFdn());
        uploadArtifactUseCase.execute(nodeDescriptor.getNodeFdn(), VNF_DESCRIPTOR_XML, FILECONTENT.getBytes());
    }

    @Test(expected = ValidationException.class)
    public void whenUploadingAFile_andFileFailsImportServiceValidation_thenValidationExceptionIsPropagated() {
        final NodeDescriptor nodeDescriptor = createNodeDescriptorWithArtifacts(State.ORDER_COMPLETED);
        dpsGenerator.generate(nodeDescriptor);

        final List<String> validationErrors = new ArrayList<>();
        validationErrors.add("error");

        when(configurationFileValidator.validateFile(anyString(), anyString(), anyString(), any(ArchiveArtifact.class)))
        .thenReturn(validationErrors);

        uploadArtifactUseCase.execute(nodeDescriptor.getNodeFdn(), TN_DATA_NETCONF_XML, FILECONTENT.getBytes());
    }

    @Test(expected = ValidationException.class)
    public void when_uploading_a_file_that_fails_validation_then_upload_fails() {
        final NodeDescriptor nodeDescriptor = createNodeDescriptorWithArtifacts(State.ORDER_COMPLETED);
        dpsGenerator.generate(nodeDescriptor);
        when(schemaService.readSchema(ERBS.toString(), NODE_IDENTIFIER_VALUE, SITE_BASIC)).thenReturn(DUMMY_ERBS_SCHEMA_DATA);
        doThrow(new SchemaValidationException("Error")).when(xmlValidator).validateAgainstSchema(FILECONTENT, DUMMY_ERBS_SCHEMA_DATA.getData());

        uploadArtifactUseCase.execute(nodeDescriptor.getNodeFdn(), SITE_BASIC_XML, FILECONTENT.getBytes());
    }

    @Test
    public void when_uploading_a_file_that_does_not_have_a_schema_then_upload_succeeds() {
        final NodeDescriptor nodeDescriptor = createNodeDescriptorWithArtifacts(State.ORDER_COMPLETED);
        dpsGenerator.generate(nodeDescriptor);
        doThrow(new ArtifactDataNotFoundException("Error")).when(schemaService).readSchema(ERBS.toString(), NODE_IDENTIFIER_VALUE, SITE_BASIC);

        uploadArtifactUseCase.execute(nodeDescriptor.getNodeFdn(), SITE_BASIC_XML, FILECONTENT.getBytes());
        verify(resourceOperations).writeArtifact(RAW_LOCATION + SITE_BASIC_XML, FILECONTENT.getBytes());
    }

    @Test
    public void whenUploadingAFileMismatchTheOriginalFileFormatThenUploadFails() {
        final NodeDescriptor nodeDescriptor = createNodeDescriptorWithArtifacts(State.ORDER_COMPLETED);
        dpsGenerator.generate(nodeDescriptor);

        try {
            uploadArtifactUseCase.execute(nodeDescriptor.getNodeFdn(), TN_DATA_NETCONF_XML, FILECONTENT.getBytes());
            fail("An exception should've been thrown here");
        } catch (final ValidationException ex) {
            assertTrue(ex.getMessage().contains("is not the same as the original format"));
        }
    }

    @Test(expected = IllegalUploadNodeStateException.class)
    public void when_uploading_a_file_and_node_is_not_in_correct_state_then_upload_fails() {
        final NodeDescriptor nodeDescriptor = createNodeDescriptorWithArtifacts(State.BIND_COMPLETED);
        dpsGenerator.generate(nodeDescriptor);

        uploadArtifactUseCase.execute(nodeDescriptor.getNodeFdn(), SITE_BASIC_XML, FILECONTENT.getBytes());
    }

    @Test
    public void whenUploadingLicenseFileThenUploadSucceeds() {
        final NodeDescriptor nodeDescriptor = createNodeDescriptorWithArtifacts(State.ORDER_FAILED);
        dpsGenerator.generate(nodeDescriptor);

        final Set<String> state = new HashSet<>();
        state.add(State.ORDER_FAILED.toString());
        state.add(State.ORDER_ROLLBACK_FAILED.toString());
        when(uploadResolver.getValidStatesForUpload(LICENSE_FILE)).thenReturn(state);
        uploadArtifactUseCase.execute(nodeDescriptor.getNodeFdn(), LICENSE_KEY_FILE_ZIP, FILECONTENT.getBytes());

        verify(resourceOperations).writeArtifact(RAW_LOCATION + LICENSE_KEY_FILE_ZIP, FILECONTENT.getBytes());
    }

    private NodeDescriptor createNodeDescriptorWithArtifacts(final State state) {
        return createDefaultNode(ERBS)
                .withArtifact(SITE_BASIC, RAW_LOCATION + SITE_BASIC_XML, null, ArtifactFileFormat.BULK_3GPP.name())
                .withArtifact(SITE_EQUIPMENT, RAW_LOCATION + SITE_EQUIPMENT_XML, null, ArtifactFileFormat.NETCONF.name())
                .withArtifact(SITE_INSTALL, RAW_LOCATION + SITE_INSTALL_XML, null, ArtifactFileFormat.UNKNOWN.name())
                .withArtifact(CONFIGURATION, RAW_LOCATION + TN_DATA_NETCONF_XML, null, ArtifactFileFormat.NETCONF.name())
                .withArtifact(CONFIGURATION, RAW_LOCATION + TN_DATA_BULKCM_XML, null, ArtifactFileFormat.BULK_3GPP.name())
                .withArtifact(CONFIGURATION, RAW_LOCATION + RN_DATA_XML, null, ArtifactFileFormat.BULK_3GPP.name())
                .withArtifact(CONFIGURATION, RAW_LOCATION + AMOS_SCRIPT_MOS, null, ArtifactFileFormat.AMOS_SCRIPT.name())
                .withArtifact(LICENSE_FILE, RAW_LOCATION + LICENSE_KEY_FILE_ZIP, null, ArtifactFileFormat.BULK_3GPP.name())
                .withArtifact(VNF_DESCRIPTOR, RAW_LOCATION + VNF_DESCRIPTOR_XML, null, ArtifactFileFormat.BULK_3GPP.name())
                .withNodeStatus(state).build();
    }

    private NodeDescriptor createNodeDescriptorWithArtifactsForExpansion(final State state) {
        return createDefaultNode(RadioNode)
                .withArtifact(CONFIGURATION, RAW_LOCATION + TN_DATA_NETCONF_XML, null, ArtifactFileFormat.NETCONF.name())
                .withArtifact(CONFIGURATION, RAW_LOCATION + TN_DATA_BULKCM_XML, null, ArtifactFileFormat.NETCONF.name())
                .withArtifact(CONFIGURATION, RAW_LOCATION + RN_DATA_XML, null, ArtifactFileFormat.NETCONF.name())
                .withArtifact(CONFIGURATION, RAW_LOCATION + AMOS_SCRIPT_MOS, null, ArtifactFileFormat.AMOS_SCRIPT.name())
                .withArtifact(LICENSE_FILE, RAW_LOCATION + LICENSE_KEY_FILE_ZIP, null, ArtifactFileFormat.UNKNOWN.name())
                .withArtifact(VNF_DESCRIPTOR, RAW_LOCATION + VNF_DESCRIPTOR_XML, null, ArtifactFileFormat.BULK_3GPP.name())
                .withNodeStatus(state).build();
    }

    private NodeDescriptor createNodeDescriptorWithArtifactsForGreenField(final State state) {
        return createDefaultNode(RadioNode)
                .withArtifact(SITE_BASIC, RAW_LOCATION + SITE_BASIC_XML, null, ArtifactFileFormat.BULK_3GPP.name())
                .withArtifact(SITE_EQUIPMENT, RAW_LOCATION + SITE_EQUIPMENT_XML, null, ArtifactFileFormat.NETCONF.name())
                .withArtifact(CONFIGURATION, RAW_LOCATION + TN_DATA_NETCONF_XML, null, ArtifactFileFormat.NETCONF.name())
                .withArtifact(CONFIGURATION, RAW_LOCATION + TN_DATA_BULKCM_XML, null, ArtifactFileFormat.NETCONF.name())
                .withArtifact(CONFIGURATION, RAW_LOCATION + RN_DATA_XML, null, ArtifactFileFormat.NETCONF.name())
                .withArtifact(CONFIGURATION, RAW_LOCATION + AMOS_SCRIPT_MOS, null, ArtifactFileFormat.AMOS_SCRIPT.name())
                .withArtifact(LICENSE_FILE, RAW_LOCATION + LICENSE_KEY_FILE_ZIP, null, ArtifactFileFormat.UNKNOWN.name())
                .withArtifact(VNF_DESCRIPTOR, RAW_LOCATION + VNF_DESCRIPTOR_XML, null, ArtifactFileFormat.BULK_3GPP.name())
                .withNodeStatus(state).build();
    }

    private NodeDescriptor createNodeDescriptorWithArtifactsForMigration(final State state) {
        return createDefaultNode(RadioNode)
                .withArtifact(SITE_BASIC, RAW_LOCATION + SITE_BASIC_XML, null, ArtifactFileFormat.BULK_3GPP.name())
                .withArtifact(SITE_EQUIPMENT, RAW_LOCATION + SITE_EQUIPMENT_XML, null, ArtifactFileFormat.NETCONF.name())
                .withArtifact(PRE_MIGRATION_CONFIGURATION, RAW_LOCATION + LOCK_G2_CELLS_XML, null, ArtifactFileFormat.NETCONF.name())
                .withArtifact(CONFIGURATION, RAW_LOCATION + TN_DATA_NETCONF_XML, null, ArtifactFileFormat.NETCONF.name())
                .withArtifact(CONFIGURATION, RAW_LOCATION + TN_DATA_BULKCM_XML, null, ArtifactFileFormat.NETCONF.name())
                .withArtifact(CONFIGURATION, RAW_LOCATION + RN_DATA_XML, null, ArtifactFileFormat.NETCONF.name())
                .withArtifact(CONFIGURATION, RAW_LOCATION + AMOS_SCRIPT_MOS, null, ArtifactFileFormat.AMOS_SCRIPT.name()).withNodeStatus(state)
                .build();
    }

    private NodeDescriptor createNodeDescriptorWithArtifactsAndStrictSequenceForGreenField(final State state) {
        return createDefaultNode(RadioNode)
                .withArtifactContainerOption(NodeArtifactContainerAttribute.STRICT.toString(), Boolean.TRUE)
                .withArtifact(SITE_BASIC, RAW_LOCATION + SITE_BASIC_XML, null, ArtifactFileFormat.NETCONF.name())
                .withArtifact(SITE_EQUIPMENT, RAW_LOCATION + SITE_EQUIPMENT_XML, null, ArtifactFileFormat.NETCONF.name())
                .withArtifact(CONFIGURATION, RAW_LOCATION + TN_DATA_NETCONF_XML, null, ArtifactFileFormat.NETCONF.name())
                .withArtifact(CONFIGURATION, RAW_LOCATION + TN_DATA_BULKCM_XML, null, ArtifactFileFormat.NETCONF.name())
                .withArtifact(CONFIGURATION, RAW_LOCATION + RN_DATA_XML, null, ArtifactFileFormat.NETCONF.name())
                .withArtifact(CONFIGURATION, RAW_LOCATION + AMOS_SCRIPT_MOS, null, ArtifactFileFormat.AMOS_SCRIPT.name())
                .withArtifact(LICENSE_FILE, RAW_LOCATION + LICENSE_KEY_FILE_ZIP, null, ArtifactFileFormat.UNKNOWN.name())
                .withArtifact(VNF_DESCRIPTOR, RAW_LOCATION + VNF_DESCRIPTOR_XML, null, ArtifactFileFormat.BULK_3GPP.name())
                .withNodeStatus(state).build();
    }
}
