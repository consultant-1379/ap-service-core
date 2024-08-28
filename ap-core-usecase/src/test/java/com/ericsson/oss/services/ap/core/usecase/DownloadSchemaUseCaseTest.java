/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
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
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.VALID_NODE_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ericsson.oss.itpf.modeling.modelservice.ModelService;
import com.ericsson.oss.itpf.modeling.modelservice.typed.TypedModelAccess;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.edt.EnumDataTypeSpecification;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.target.TargetTypeInformation;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.exception.UnsupportedNodeTypeException;
import com.ericsson.oss.services.ap.api.resource.ResourceService;
import com.ericsson.oss.services.ap.api.schema.SchemaData;
import com.ericsson.oss.services.ap.api.schema.SchemaService;
import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for {@code DownloadSchemaUseCase}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DownloadSchemaUseCaseTest {

    @Mock
    private SchemaService schemaService;

    @Mock
    private ResourceService resourceService;

    @Mock
    private ModelReader modelReader;

    @Mock
    private EnumDataTypeSpecification enumSpecification;

    @Mock
    private EnumDataTypeSpecification neTypeEnum;

    @Mock
    private ModelService modelService;

    @Mock
    private TargetTypeInformation targetTypeInformation;

    @Mock
    private TypedModelAccess typedModelAccess;

    @InjectMocks
    private final DownloadSchemaUseCase downloadSchemaUseCase = new DownloadSchemaUseCase();

    @Before
    public void setUp() {
        final List<SchemaData> erbsSchemas = new ArrayList<>();
        erbsSchemas.add(new SchemaData("Artifacts.xsd", "SCHEMA", NODE_IDENTIFIER_VALUE, "schema_1".getBytes(), "/schema_location"));
        erbsSchemas.add(new SchemaData("AutoIntegration.xsd", "SCHEMA", NODE_IDENTIFIER_VALUE, "schema_2".getBytes(), "/schema_location"));

        final List<SchemaData> erbsSamples = new ArrayList<>();
        erbsSamples.add(new SchemaData("erbs_sample.zip", "SAMPLE", NODE_IDENTIFIER_VALUE, "sample_1".getBytes(), "/schema_location"));
        erbsSamples.add(new SchemaData("erbs_batch_sample.zip", "SAMPLE", NODE_IDENTIFIER_VALUE, "sample_2".getBytes(), "/schema_location"));

        final List<SchemaData> rbsSchemas = new ArrayList<>();
        rbsSchemas.add(new SchemaData("Security.xsd", "SCHEMA", NODE_IDENTIFIER_VALUE, "schema_1".getBytes(), "/schema_location"));
        final List<SchemaData> rbsSamples = new ArrayList<>();
        rbsSamples.add(new SchemaData("rbs_sample.zip", "SAMPLE", NODE_IDENTIFIER_VALUE, "sample_1".getBytes(), "/schema_location"));

        final Map<String, List<SchemaData>> allSchemas = new HashMap<>();
        allSchemas.put(VALID_NODE_TYPE, erbsSchemas);
        allSchemas.put("RBS", rbsSchemas);
        final Map<String, List<SchemaData>> allSamples = new HashMap<>();
        allSamples.put(VALID_NODE_TYPE, erbsSamples);
        allSamples.put("RBS", rbsSchemas);

        when(modelService.getTypedAccess()).thenReturn(typedModelAccess);
        when(typedModelAccess.getModelInformation(TargetTypeInformation.class)).thenReturn(targetTypeInformation);
        when(schemaService.readSchemas(VALID_NODE_TYPE)).thenReturn(erbsSchemas);
        when(schemaService.readSamples(VALID_NODE_TYPE)).thenReturn(erbsSamples);
        when(schemaService.readSchemas()).thenReturn(allSchemas);
        when(schemaService.readSamples()).thenReturn(allSamples);
        final List<String> listOfNodeTypes = new ArrayList<>();
        listOfNodeTypes.add(VALID_NODE_TYPE);
        listOfNodeTypes.add("RBS");
        when(enumSpecification.getMemberNames()).thenReturn(listOfNodeTypes);
        final List<String> listOfNeTypes = new ArrayList<>();
        listOfNeTypes.add(VALID_NODE_TYPE);
        listOfNeTypes.add("RBS");
        when(neTypeEnum.getMemberNames()).thenReturn(listOfNeTypes);
        when(modelReader.getSupportedNodeTypes()).thenReturn(listOfNeTypes);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void when_writing_zip_file_all_schemas_and_samples_for_node_type_are_included() {
        downloadSchemaUseCase.execute(VALID_NODE_TYPE);
        final ArgumentCaptor<Map> fileContents = ArgumentCaptor.forClass(Map.class);

        verify(resourceService).writeContentsToZip(anyString(), fileContents.capture());

        final Map<String, byte[]> fileContentsByPath = fileContents.getValue();
        assertThat(fileContentsByPath).hasSize(4);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void when_writing_zip_file_all_schemas_and_samples_for_all_node_types_are_included() {
        downloadSchemaUseCase.execute("");
        final ArgumentCaptor<Map> fileContents = ArgumentCaptor.forClass(Map.class);

        verify(resourceService).writeContentsToZip(anyString(), fileContents.capture());

        final Map<String, byte[]> fileContentsByPath = fileContents.getValue();
        assertThat(fileContentsByPath).hasSize(4);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void when_writing_zip_the_file_path_for_schema_files_is_as_expected() {
        downloadSchemaUseCase.execute(VALID_NODE_TYPE);

        final ArgumentCaptor<Map> fileContents = ArgumentCaptor.forClass(Map.class);

        verify(resourceService).writeContentsToZip(anyString(), fileContents.capture());

        final Map<String, byte[]> fileContentsByPath = fileContents.getValue();

        assertThat(fileContentsByPath)
                .containsKeys("schemas/" + NODE_IDENTIFIER_VALUE + "/Artifacts.xsd", "schemas/" + NODE_IDENTIFIER_VALUE + "/AutoIntegration.xsd");
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void when_writing_zip_the_file_path_for_sample_files_is_as_expected() {
        downloadSchemaUseCase.execute(VALID_NODE_TYPE);

        final ArgumentCaptor<Map> fileContents = ArgumentCaptor.forClass(Map.class);

        verify(resourceService).writeContentsToZip(anyString(), fileContents.capture());

        final Map<String, byte[]> fileContentsByPath = fileContents.getValue();

        assertThat(fileContentsByPath).containsKeys("samples/erbs_sample.zip", "samples/erbs_batch_sample.zip");
    }

    @Test
    public void when_writing_zip_file_the_output_directory_is_as_expected() {
        final Path expectedDownloadDir = Paths.get(DirectoryConfiguration.getDownloadDirectory());

        downloadSchemaUseCase.execute(VALID_NODE_TYPE);

        final ArgumentCaptor<String> downloadFilePath = ArgumentCaptor.forClass(String.class);

        verify(resourceService).writeContentsToZip(downloadFilePath.capture(), anyMapOf(String.class, byte[].class));

        assertEquals(expectedDownloadDir.toString(), new File(downloadFilePath.getValue()).getParent());
    }

    @Test
    public void when_usecase_executes_the_returned_file_id_matches_the_name_of_created_zip_file() {
        final String fileId = downloadSchemaUseCase.execute(VALID_NODE_TYPE);
        final ArgumentCaptor<String> downloadFilePath = ArgumentCaptor.forClass(String.class);
        verify(resourceService).writeContentsToZip(downloadFilePath.capture(), anyMapOf(String.class, byte[].class));
        assertEquals(new File(downloadFilePath.getValue()).getName(), fileId);
    }

    @Test(expected = UnsupportedNodeTypeException.class)
    public void whenExecutingUseCase_andNodeTypeIsInvalid_thenUnsupportedNodeTypeExceptionIsThrown() {
        downloadSchemaUseCase.execute("invalidNodeType");
    }

    @Test(expected = ApApplicationException.class)
    public void whenExecutingUseCase_andNodeTypeIsNotInstalled_thenApApplicationExceptionIsThrown() {
        when(targetTypeInformation.getTargetTypeVersionInformation(TargetTypeInformation.CATEGORY_NODE, "vPP"))
            .thenThrow(new ApApplicationException(""));
        downloadSchemaUseCase.execute("vPP");
    }

    @Test(expected = UnsupportedNodeTypeException.class)
    public void whenExecutingUseCase_andNodeTypeIsNotInModel_thenUnsupportedNodeTypeExceptionIsThrown() {
        when(modelReader.getSupportedNodeTypes()).thenReturn(Collections.<String> emptyList());
        downloadSchemaUseCase.execute(VALID_NODE_TYPE);
    }
}
