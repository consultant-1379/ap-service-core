/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase;

import static com.ericsson.oss.services.ap.common.model.MoType.NODE;
import static com.ericsson.oss.services.ap.common.model.MoType.PROJECT;
import static com.ericsson.oss.services.ap.common.model.Namespace.AP;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_FDN;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_NAME;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps;
import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;

/**
 * Unit tests for {@link ViewProjectUseCase}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ViewProjectUseCaseTest {

    @Mock
    private Logger logger; // NOPMD

    @InjectMocks
    private ViewProjectUseCase viewProjectUseCase;

    private RuntimeConfigurableDps configurableDps;

    @Mock
    private DpsOperations dps;

    @Before
    public void setUp() {
        configurableDps = new RuntimeConfigurableDps();
        final DataPersistenceService dpservice = configurableDps.build();
        Whitebox.setInternalState(dps, "dps", dpservice);
        Whitebox.setInternalState(viewProjectUseCase, "dps", dps);
        when(dps.getDataPersistenceService()).thenReturn(dpservice);
        addProjectMo(PROJECT_FDN);
    }

    @Test
    public void when_view_project_with_no_nodes_then_only_project_mo_data_returned() {
        final List<MoData> viewProjectData = viewProjectUseCase.execute(PROJECT_FDN);
        assertEquals(1, viewProjectData.size());
    }

    @Test
    public void when_view_project_then_returned_project_data_contains_all_attributes_in_the_project_model() {
        final MoData projectMo = viewProjectUseCase.execute(PROJECT_FDN).iterator().next();
        assertEquals("value1", projectMo.getAttribute("attr1"));
        assertEquals("value2", projectMo.getAttribute("attr2"));
    }

    @Test
    public void when_view_project_with_nodes_then_project_mo_is_first_in_the_returned_list() {
        addNodeMo(PROJECT_FDN + ",Node=Node1");
        addNodeMo(PROJECT_FDN + ",Node=Node2");

        final List<MoData> viewProjectData = viewProjectUseCase.execute(PROJECT_FDN);
        final MoData firstMo = viewProjectData.iterator().next();

        assertEquals("Project", firstMo.getType());
    }

    @Test
    public void when_view_project_with_nodes_then_returned_project_data_contains_additional_attributes_for_project_name_and_nodeQuantity() {
        addNodeMo(PROJECT_FDN + ",Node=Node1");
        addNodeMo(PROJECT_FDN + ",Node=Node2");
        addNodeMo(PROJECT_FDN + ",Node=Node3");

        final List<MoData> viewProjectData = viewProjectUseCase.execute(PROJECT_FDN);
        final MoData projectMo = viewProjectData.get(0);

        assertEquals(PROJECT_NAME, projectMo.getAttribute("projectName"));
        assertEquals("3", projectMo.getAttribute("nodeQuantity"));
    }

    @Test
    public void when_view_project_with_nodes_then_node_mos_are_returned_in_the_list() {
        addNodeMo(PROJECT_FDN + ",Node=Node1");
        addNodeMo(PROJECT_FDN + ",Node=Node2");
        addNodeMo(PROJECT_FDN + ",Node=Node3");

        final List<MoData> viewProjectData = viewProjectUseCase.execute(PROJECT_FDN);

        assertEquals(4, viewProjectData.size());
    }

    @Test
    public void when_view_project_with_nodes_then_returned_node_mo_data_contains_all_attributes_in_the_node_model() {
        addNodeMo(PROJECT_FDN + ",Node=Node1");

        final List<MoData> viewProjectData = viewProjectUseCase.execute(PROJECT_FDN);
        final MoData nodeMo = viewProjectData.get(1);

        assertEquals("value1", nodeMo.getAttribute("attr1"));
        assertEquals("value2", nodeMo.getAttribute("attr2"));
    }

    @Test
    public void when_view_project_with_nodes_then_returned_node_mo_data_contains_additional_attributes_for_node_name() {
        addNodeMo(PROJECT_FDN + ",Node=Node1");

        final List<MoData> viewProjectData = viewProjectUseCase.execute(PROJECT_FDN);
        final MoData nodeMo = viewProjectData.get(1);

        assertEquals("Node1", nodeMo.getAttribute("nodeName"));
    }

    @Test
    public void whenViewProjectWithTwoNodesAndOneProfileThenNodeMosAreReturnedInTheList() {
        addNodeMo(PROJECT_FDN + ",Node=Node1");
        addNodeMo(PROJECT_FDN + ",Node=Node2");
        addConfigurationProfileMo(PROJECT_FDN + ",ConfigurationProfile=Profile1");

        final List<MoData> viewProjectData = viewProjectUseCase.execute(PROJECT_FDN);

        assertEquals(3, viewProjectData.size());
    }

    private void addNodeMo(final String fdn) {
        final Map<String, Object> nodeAttributes = new HashMap<>();
        nodeAttributes.put("attr1", "value1");
        nodeAttributes.put("attr2", "value2");

        configurableDps.addManagedObject()
            .withFdn(fdn)
            .type(NODE.toString())
            .namespace(AP.toString())
            .version("1.0.0")
            .addAttributes(nodeAttributes)
            .build();
    }

    private void addConfigurationProfileMo(final String fdn) {
        final Map<String, Object> profileAttributes = new HashMap<>();
        profileAttributes.put("attr1", "value1");
        profileAttributes.put("attr2", "value2");

        configurableDps.addManagedObject()
            .withFdn(fdn)
            .type("ConfigurationProfile")
            .namespace(AP.toString())
            .version("1.0.0")
            .addAttributes(profileAttributes)
            .build();
    }

    private ManagedObject addProjectMo(final String fdn) {
        final Map<String, Object> projectAttributes = new HashMap<>();
        projectAttributes.put("attr1", "value1");
        projectAttributes.put("attr2", "value2");

        return configurableDps.addManagedObject()
            .withFdn(fdn)
            .type(PROJECT.toString())
            .namespace(AP.toString())
            .version("1.0.0")
            .addAttributes(projectAttributes)
            .build();
    }
}
