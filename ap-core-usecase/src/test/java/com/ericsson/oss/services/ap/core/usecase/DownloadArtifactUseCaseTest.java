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

import static com.ericsson.oss.services.ap.api.status.State.INTEGRATION_COMPLETED;
import static com.ericsson.oss.services.ap.api.status.State.ORDER_COMPLETED;
import static com.ericsson.oss.services.ap.common.model.MoType.NODE_STATUS;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME;
import static com.ericsson.oss.services.ap.model.NodeType.ERBS;
import static com.ericsson.oss.services.ap.model.NodeType.vPP;
import static com.ericsson.oss.services.ap.common.model.NodeAttribute.NODE_TYPE;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.exception.general.DpsPersistenceException;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.ArtifactBaseType;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.exception.IllegalDownloadArtifactException;
import com.ericsson.oss.services.ap.api.exception.NodeNotFoundException;
import com.ericsson.oss.services.ap.api.resource.ResourceService;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactDetails;
import com.ericsson.oss.services.ap.common.artifacts.generated.GeneratedArtifactHandler;
import com.ericsson.oss.services.ap.common.artifacts.raw.RawArtifactHandler;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.download.DownloadArtifactService;
import com.ericsson.oss.services.ap.api.exception.UnsupportedCommandException;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;

/**
 * Unit tests for {@link DownloadArtifactUseCase}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DownloadArtifactUseCaseTest {

    private static final String DUMMY_ARTIFACT_TYPE = "DummyArtifactType";
    private static final String DUMMY_ARTIFACT_NAME = "DummyArtifactName";
    private static final String DUMMY_ARTIFACT_NAME_WITH_EXTENSION = DUMMY_ARTIFACT_NAME + ".xml";
    private static final String DUMMY_ARTIFACT_NAME_WITH_EXTENSION_SUFFIX = String.format("\\d*_%s_%s_\\d*.xml", NODE_NAME, DUMMY_ARTIFACT_NAME);
    private static final String DUMMY_ARTIFACT_CONTENT = "DummyArtifactContent";
    private static final String DOWNLOADED_RAW_ARTIFACTS_SUFFIX = String.format("\\d*_%s_rawArtifacts_\\d*.zip", NODE_NAME);
    private static final String STATE_ATTRIBUTE_NAME = "state";
    private static final String TYPE_ERBS = ERBS.toString().toLowerCase();
    private static final String TYPE_VPP = vPP.toString().toLowerCase();


    @Mock
    private DpsOperations dps;

    @Mock
    private DataBucket liveBucket;

    @Mock
    private ManagedObject nodeMo;

    @Mock
    private ManagedObject nodeStatusMo;

    @Mock
    private NodeTypeMapper nodeTypeMapper;

    @Mock
    private RawArtifactHandler rawArtifactHandler;

    @Mock
    private GeneratedArtifactHandler generatedArtifactHandler;

    @Mock
    private ResourceService resourceService;

    @Mock
    private DataPersistenceService dpsService;

    @Mock
    private ServiceFinderBean serviceFinder;

    @Mock
    private DownloadArtifactService downloadResolver;

    @InjectMocks
    private final DownloadArtifactUseCase downloadArtifactUseCase = new DownloadArtifactUseCase();

    @Before
    public void setUp() {
        when(dps.getDataPersistenceService()).thenReturn(dpsService);
        when(dps.getDataPersistenceService().getLiveBucket()).thenReturn(liveBucket);
        when(liveBucket.findMoByFdn(NODE_FDN)).thenReturn(nodeMo);
        when(nodeMo.getFdn()).thenReturn(NODE_FDN);
        when(nodeMo.getChild(NODE_STATUS.toString() + "=1")).thenReturn(nodeStatusMo);
        when(nodeStatusMo.getAttribute(STATE_ATTRIBUTE_NAME)).thenReturn(ORDER_COMPLETED.toString());

        when(nodeMo.getAttribute(NODE_TYPE.toString())).thenReturn(TYPE_ERBS);
        when(nodeTypeMapper.getInternalEjbQualifier(TYPE_ERBS)).thenReturn(TYPE_ERBS);
        when(serviceFinder.find(DownloadArtifactService.class, TYPE_ERBS)).thenReturn(downloadResolver);
        when(downloadResolver.isOrderedArtifactSupported()).thenReturn(true);
}

    @Test
    public void when_raw_artifacts_zip_downloaded_then_download_successful() {
        when(resourceService.supportsWriteOperations(anyString())).thenReturn(true);

        final ArtifactDetails rawArtifact = new ArtifactDetails.ArtifactBuilder()
            .apNodeFdn(NODE_FDN)
            .type(DUMMY_ARTIFACT_TYPE)
            .name(DUMMY_ARTIFACT_NAME_WITH_EXTENSION)
            .artifactContent(DUMMY_ARTIFACT_CONTENT)
            .build();

        final List<ArtifactDetails> rawArtifacts = new ArrayList<>();
        rawArtifacts.add(rawArtifact);
        rawArtifacts.add(rawArtifact);
        when(rawArtifactHandler.readAllForNode(NODE_FDN)).thenReturn(rawArtifacts);

        final String uniqueFileId = downloadArtifactUseCase.execute(NODE_FDN, ArtifactBaseType.RAW);
        assertTrue(uniqueFileId.matches(DOWNLOADED_RAW_ARTIFACTS_SUFFIX));
    }

    @Test
    public void when_downloading_raw_artifacts_and_two_artifacts_have_the_same_type_then_both_artifacts_are_added_to_zip_file() {
        final ArtifactDetails firstArtifact = new ArtifactDetails.ArtifactBuilder()
            .apNodeFdn(NODE_FDN)
            .name("first.xml")
            .type(DUMMY_ARTIFACT_TYPE)
            .artifactContent(DUMMY_ARTIFACT_CONTENT)
            .build();

        final ArtifactDetails secondArtifact = new ArtifactDetails.ArtifactBuilder()
            .apNodeFdn(NODE_FDN)
            .name("second.xml")
            .type(DUMMY_ARTIFACT_TYPE)
            .artifactContent(DUMMY_ARTIFACT_CONTENT)
            .build();

        final List<ArtifactDetails> artifactsWithSameType = new ArrayList<>();
        artifactsWithSameType.add(firstArtifact);
        artifactsWithSameType.add(secondArtifact);
        when(resourceService.supportsWriteOperations(anyString())).thenReturn(true);
        when(rawArtifactHandler.readAllForNode(NODE_FDN)).thenReturn(artifactsWithSameType);

        downloadArtifactUseCase.execute(NODE_FDN, ArtifactBaseType.RAW);

        verify(resourceService).writeContentsToZip(anyString(), argThat(new IsMapOfArtifactDetailsWithTwoElements()));
    }

    @Test
    public void when_download_generated_artifact_xml_file_successful_then_file_has_correct_filename_with_node_name_and_timestamp() throws IOException {
        when(resourceService.supportsWriteOperations(anyString())).thenReturn(true);

        final ArtifactDetails generatedArtifact = new ArtifactDetails.ArtifactBuilder()
            .name(DUMMY_ARTIFACT_NAME_WITH_EXTENSION)
            .exportable(true)
            .artifactContent(DUMMY_ARTIFACT_CONTENT)
            .build();

        final List<ArtifactDetails> generatedArtifacts = new ArrayList<>();
        generatedArtifacts.add(generatedArtifact);
        when(generatedArtifactHandler.readAllForNode(NODE_FDN)).thenReturn(generatedArtifacts);

        final String uniqueFileId = downloadArtifactUseCase.execute(NODE_FDN, ArtifactBaseType.GENERATED);
        assertTrue(uniqueFileId.matches(DUMMY_ARTIFACT_NAME_WITH_EXTENSION_SUFFIX));
    }

    @Test(expected = IllegalDownloadArtifactException.class)
    public void when_download_artifact_not_permitted_then_illegal_download_artifact_exception_thrown() {
        when(nodeStatusMo.getAttribute(STATE_ATTRIBUTE_NAME)).thenReturn(INTEGRATION_COMPLETED.toString());
        downloadArtifactUseCase.execute(NODE_FDN, ArtifactBaseType.RAW);
    }

    @Test(expected = ApApplicationException.class)
    public void when_download_directory_does_not_support_write_operations_then_ap_application_exception_thrown() {
        when(resourceService.supportsWriteOperations(anyString())).thenReturn(false);
        downloadArtifactUseCase.execute(NODE_FDN, ArtifactBaseType.RAW);
    }

    @Test(expected = ApApplicationException.class)
    public void when_no_artifact_found_then_download_failed() {
        when(resourceService.supportsWriteOperations(anyString())).thenReturn(true);
        when(generatedArtifactHandler.readAllForNode(NODE_FDN)).thenReturn(Collections.<ArtifactDetails> emptyList());

        downloadArtifactUseCase.execute(NODE_FDN, ArtifactBaseType.GENERATED);
    }

    @Test(expected = UnsupportedCommandException.class)
    public void when_vnf_ordered_artifact_downloaded_then_unsupported_command_exception_thrown() {
        when(nodeMo.getAttribute(NODE_TYPE.toString())).thenReturn(TYPE_VPP);
        when(nodeTypeMapper.getInternalEjbQualifier(TYPE_VPP)).thenReturn(TYPE_VPP);
        when(serviceFinder.find(DownloadArtifactService.class, TYPE_VPP)).thenReturn(downloadResolver);
        when(downloadResolver.isOrderedArtifactSupported()).thenReturn(false);

        downloadArtifactUseCase.execute(NODE_FDN, ArtifactBaseType.GENERATED);
    }

    @Test(expected = NodeNotFoundException.class)
    public void when_node_not_found_then_download_failed() {
        when(resourceService.supportsWriteOperations(anyString())).thenReturn(true);
        doThrow(NodeNotFoundException.class).when(rawArtifactHandler).readAllForNode(NODE_FDN);

        downloadArtifactUseCase.execute(NODE_FDN, ArtifactBaseType.RAW);
    }

    @Test(expected = ApApplicationException.class)
    public void when_exception_occurred_then_download_failed() {
        when(resourceService.supportsWriteOperations(anyString())).thenReturn(true);
        doThrow(DpsPersistenceException.class).when(rawArtifactHandler).readAllForNode(NODE_FDN);

        downloadArtifactUseCase.execute(NODE_FDN, ArtifactBaseType.RAW);
    }

    @Test(expected = ApApplicationException.class)
    public void when_unexpected_exception_occurred_then_download_failed() {
        when(resourceService.supportsWriteOperations(anyString())).thenReturn(true);
        doThrow(Exception.class).when(rawArtifactHandler).readAllForNode(NODE_FDN);

        downloadArtifactUseCase.execute(NODE_FDN, ArtifactBaseType.RAW);
    }

    private class IsMapOfArtifactDetailsWithTwoElements extends ArgumentMatcher<Map<String, byte[]>> {

        @Override
        @SuppressWarnings("unchecked")
        public boolean matches(final Object map) {
            return ((Map<String, byte[]>) map).size() == 2;
        }
    }
}
