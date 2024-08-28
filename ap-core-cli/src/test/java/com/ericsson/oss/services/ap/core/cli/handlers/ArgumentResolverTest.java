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
package com.ericsson.oss.services.ap.core.cli.handlers;

import static com.ericsson.oss.services.ap.common.model.Namespace.AP;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_FDN;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.cli.CommandLine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.exception.general.DpsPersistenceException;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.api.exception.NodeNotFoundException;
import com.ericsson.oss.services.ap.api.exception.ProjectNotFoundException;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.cm.DpsQueries.DpsQueryExecutor;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;

/**
 * Unit tests for {@link ArgumentResolver}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ArgumentResolverTest {

    private static final String PROJECT_OPTION = "p";
    private static final String NODE_OPTION = "n";

    @Mock
    private CommandLine commandOptions;

    @Mock
    private DpsQueries dpsQueries;

    @Mock
    private DpsQueryExecutor<ManagedObject> dpsQueryExecutor;

    @Mock
    private Logger logger; // NOPMD

    @Mock
    private SystemRecorder recorder; // NOPMD

    @InjectMocks
    private final ArgumentResolver argumentResolver = new ArgumentResolver();

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionThrownWhenNodeOrProjectOptionNotInCommand() {
        when(commandOptions.hasOption(PROJECT_OPTION)).thenReturn(false);
        when(commandOptions.hasOption(NODE_OPTION)).thenReturn(false);
        argumentResolver.resolveFdn(commandOptions, CommandLogName.VIEW);
    }

    @Test
    public void when_p_option_and_project_exists_then_return_the_project_fdn() {
        final Collection<ManagedObject> projectMos = new ArrayList<>();
        final ManagedObject projectMo = Mockito.mock(ManagedObject.class);
        projectMos.add(projectMo);

        when(commandOptions.hasOption(PROJECT_OPTION)).thenReturn(true);
        when(commandOptions.hasOption(NODE_OPTION)).thenReturn(false);
        when(commandOptions.getOptionValue(PROJECT_OPTION)).thenReturn(NODE_NAME);

        when(dpsQueries.findMoByName(NODE_NAME, MoType.PROJECT.toString(), AP.toString())).thenReturn(dpsQueryExecutor);
        when(dpsQueryExecutor.execute()).thenReturn(projectMos.iterator());
        when(projectMo.getFdn()).thenReturn(PROJECT_FDN);

        final String fdn = argumentResolver.resolveFdn(commandOptions, CommandLogName.VIEW);
        assertEquals(PROJECT_FDN, fdn);
    }

    @Test(expected = ProjectNotFoundException.class)
    public void when_p_option_and_project_does_not_exist_then_throw_ProjectNotFoundException() {
        when(commandOptions.hasOption(PROJECT_OPTION)).thenReturn(true);
        when(commandOptions.hasOption(NODE_OPTION)).thenReturn(false);
        when(commandOptions.getOptionValue(PROJECT_OPTION)).thenReturn(NODE_NAME);
        when(dpsQueries.findMoByName(NODE_NAME, MoType.PROJECT.toString(), AP.toString())).thenReturn(dpsQueryExecutor);
        doReturn(Collections.emptyListIterator()).when(dpsQueryExecutor).execute();

        argumentResolver.resolveFdn(commandOptions, CommandLogName.VIEW);
    }

    @Test
    public void when_n_option_and_node_exists_then_return_the_node_fdn() {
        final Collection<ManagedObject> nodeMos = new ArrayList<>();
        final ManagedObject nodeMo = Mockito.mock(ManagedObject.class);
        nodeMos.add(nodeMo);

        when(commandOptions.hasOption(PROJECT_OPTION)).thenReturn(false);
        when(commandOptions.hasOption(NODE_OPTION)).thenReturn(true);
        when(commandOptions.getOptionValue(NODE_OPTION)).thenReturn(NODE_NAME);

        when(dpsQueries.findMoByName(NODE_NAME, MoType.NODE.toString(), AP.toString())).thenReturn(dpsQueryExecutor);
        when(dpsQueryExecutor.execute()).thenReturn(nodeMos.iterator());
        when(nodeMo.getFdn()).thenReturn(NODE_FDN);

        final String fdn = argumentResolver.resolveFdn(commandOptions, CommandLogName.VIEW);
        assertEquals(NODE_FDN, fdn);
    }

    @Test(expected = NodeNotFoundException.class)
    public void when_n_option_and_node__does_not_exiss_then_throw_NodeNotFoundException() {
        when(commandOptions.hasOption(PROJECT_OPTION)).thenReturn(false);
        when(commandOptions.hasOption(NODE_OPTION)).thenReturn(true);
        when(commandOptions.getOptionValue(NODE_OPTION)).thenReturn(NODE_NAME);

        when(dpsQueries.findMoByName(NODE_NAME, MoType.NODE.toString(), AP.toString())).thenReturn(dpsQueryExecutor);
        doReturn(Collections.emptyListIterator()).when(dpsQueryExecutor).execute();

        argumentResolver.resolveFdn(commandOptions, CommandLogName.VIEW);
    }

    @Test(expected = ApServiceException.class)
    public void when_error_reading_node_mos_then_throw_ApServiceException() {
        when(commandOptions.hasOption(PROJECT_OPTION)).thenReturn(false);
        when(commandOptions.hasOption(NODE_OPTION)).thenReturn(true);
        when(commandOptions.getOptionValue(NODE_OPTION)).thenReturn(NODE_NAME);
        when(dpsQueries.findMoByName(NODE_NAME, MoType.NODE.toString(), AP.toString())).thenReturn(dpsQueryExecutor);
        doThrow(DpsPersistenceException.class).when(dpsQueryExecutor).execute();

        argumentResolver.resolveFdn(commandOptions, CommandLogName.VIEW);
    }
}
