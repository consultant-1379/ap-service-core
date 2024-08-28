/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.HARDWARE_SERIAL_NUMBER_VALUE;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_FDN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.bind.BatchBindResult;
import com.ericsson.oss.services.ap.api.exception.HwIdAlreadyBoundException;
import com.ericsson.oss.services.ap.api.exception.HwIdInvalidFormatException;
import com.ericsson.oss.services.ap.api.exception.InvalidNodeStateException;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.cm.DpsQueries.DpsQueryExecutor;

/**
 * Unit tests for {@link BatchBindUseCase}.
 */
@RunWith(MockitoJUnitRunner.class)
public class BatchBindUseCaseTest {

    private static final String NODE_NAME_TO_BIND = "Node4";
    private static final String NODE1_FDN = PROJECT_FDN + ",Node=Node1";
    private static final String NODE2_FDN = PROJECT_FDN + ",Node=Node2";
    private static final String NODE3_FDN = PROJECT_FDN + ",Node=Node3";
    private static final String HARDWARE_SERIAL_NUMBER_VALUE_2 = HARDWARE_SERIAL_NUMBER_VALUE + "2";

    @Mock
    private DpsQueries dpsQueries;

    @Mock
    private DpsQueryExecutor<ManagedObject> dpsQueryExecutor;

    @Mock
    private BindUseCase bindUseCase;

    @Mock
    private Logger logger; // NOPMD

    @Mock
    private ManagedObject nodeMo;

    @InjectMocks
    private BatchBindUseCase batchBindUsecase;

    private final List<ManagedObject> emptyMos = new ArrayList<>();
    private final List<ManagedObject> nodeMos = new ArrayList<>();

    @Before
    public void setup() {
        nodeMos.add(nodeMo);

        when(dpsQueries.findMoByName("Node1", "Node", "ap")).thenReturn(dpsQueryExecutor);
        when(dpsQueries.findMoByName("Node2", "Node", "ap")).thenReturn(dpsQueryExecutor);
        when(dpsQueries.findMoByName("Node3", "Node", "ap")).thenReturn(dpsQueryExecutor);
        when(dpsQueryExecutor.execute()).thenReturn(nodeMos.iterator()).thenReturn(nodeMos.iterator()).thenReturn(nodeMos.iterator());
        when(nodeMo.getFdn()).thenReturn(NODE1_FDN).thenReturn(NODE2_FDN).thenReturn(NODE3_FDN);
    }

    @Test
    public void when_valid_csv_file_without_header_then_bind_succeeds_for_all_nodes_in_the_batch() {
        final byte[] csvFile = readCsvFile("valid_csv_without_headers.csv");
        final BatchBindResult bindResult = batchBindUsecase.execute("file.csv", csvFile);
        assertTrue(bindResult.isSuccessful());
        assertEquals(3, bindResult.getTotalBinds());
    }

    @Test
    public void when_valid_csv_file_with_comments_then_bind_succeeds_for_all_nodes_in_the_batch() {
        final byte[] csvFile = readCsvFile("valid_csv_with_comments.csv");
        final BatchBindResult bindResult = batchBindUsecase.execute("file.csv", csvFile);
        assertTrue(bindResult.isSuccessful());
        assertEquals(3, bindResult.getTotalBinds());
    }

    @Test
    public void when_valid_csv_file_with_quoted_values_then_bind_succeeds_for_all_nodes_in_the_batch() {
        final byte[] csvFile = readCsvFile("valid_csv_with_quoted_values.csv");
        final BatchBindResult bindResult = batchBindUsecase.execute("file.csv", csvFile);
        assertTrue(bindResult.isSuccessful());
        assertEquals(3, bindResult.getTotalBinds());
    }

    @Test
    public void when_valid_csv_file_with_whitespaces_then_bind_succeeds_for_all_nodes_in_the_batch() {
        final byte[] csvFile = readCsvFile("valid_csv_with_whitespaces.csv");
        final BatchBindResult bindResult = batchBindUsecase.execute("file.csv", csvFile);
        assertTrue(bindResult.isSuccessful());
        assertEquals(3, bindResult.getTotalBinds());
    }

    @Test
    public void when_empty_csv_file_then_bind_succeeds() {
        final byte[] csvFile = readCsvFile("empty.csv");
        final BatchBindResult bindResult = batchBindUsecase.execute("file.csv", csvFile);
        assertTrue(bindResult.isSuccessful());
    }

    @Test
    public void when_empty_lines_in_csv_file_then_bind_succeeds_for_all_nodes_in_the_batch() {
        final byte[] csvFile = readCsvFile("valid_csv_with_empty_lines.csv");
        final BatchBindResult bindResult = batchBindUsecase.execute("file.csv", csvFile);
        assertTrue(bindResult.isSuccessful());
        assertEquals(3, bindResult.getTotalBinds());
    }

    @Test
    public void when_tab_separated_delimiter_in_csv_file_then_bind_fails_due_to_missing_serial_number() {
        final byte[] csvFile = readCsvFile("invalid_csv_with_tab_delimiters.csv");
        final BatchBindResult bindResult = batchBindUsecase.execute("file.csv", csvFile);
        assertTrue(bindResult.isFailed());
        assertEquals("Line 1 - Missing mandatory value for hardware serial number", bindResult.getFailedBindMessages().get(0));
    }

    @Test
    public void when_no_serial_number_for_node_in_csv_then_bind_succeeds_for_other_nodes() {
        final byte[] csvFile = readCsvFile("invalid_csv_with_missing_hwId.csv");
        final BatchBindResult bindResult = batchBindUsecase.execute("file.csv", csvFile);
        assertTrue(bindResult.isPartial());
        assertEquals("Line 1 - Missing mandatory value for hardware serial number", bindResult.getFailedBindMessages().get(0));
    }

    @Test
    public void when_no_value_for_nodename_in_csv_then_bind_succeeds_for_all_other_nodes() {
        final byte[] csvFile = readCsvFile("invalid_csv_with_missing_nodename.csv");
        final BatchBindResult bindResult = batchBindUsecase.execute("file.csv", csvFile);
        assertTrue(bindResult.isPartial());
        assertEquals("Line 1 - Missing mandatory value for node name", bindResult.getFailedBindMessages().get(0));
    }

    @Test
    public void when_single_invalid_node_name_in_csv_then_bind_succeeds_for_all_other_nodes() {
        final byte[] csvFile = readCsvFile("valid_csv_without_headers.csv");
        when(dpsQueryExecutor.execute()).thenReturn(emptyMos.iterator()).thenReturn(nodeMos.iterator()).thenReturn(nodeMos.iterator());
        when(nodeMo.getFdn()).thenReturn(NODE2_FDN).thenReturn(NODE3_FDN);

        final BatchBindResult bindResult = batchBindUsecase.execute("file.csv", csvFile);

        assertTrue(bindResult.isPartial());
        assertEquals("Line 1 - Node does not exist", bindResult.getFailedBindMessages().get(0));
    }

    @Test
    public void when_single_node_not_in_valid_state_then_bind_succeeds_for_all_other_nodes() {
        final byte[] csvFile = readCsvFile("valid_csv_without_headers.csv");
        doThrow(new InvalidNodeStateException("error", "ORDER_COMPLETED")).when(bindUseCase).execute(NODE1_FDN, HARDWARE_SERIAL_NUMBER_VALUE);

        final BatchBindResult bindResult = batchBindUsecase.execute("file.csv", csvFile);

        assertTrue(bindResult.isPartial());
        assertEquals("Line 1 - Node is not in the correct state to perform the operation [Order Completed]",
                bindResult.getFailedBindMessages().get(0));
    }

    @Test
    public void when_single_node_with_hwId_already_bound_then_bind_succeeds_for_all_other_nodes() {
        final byte[] csvFile = readCsvFile("valid_csv_without_headers.csv");
        doThrow(new HwIdAlreadyBoundException("msg", NODE_NAME_TO_BIND)).when(bindUseCase).execute(NODE1_FDN, HARDWARE_SERIAL_NUMBER_VALUE);

        final BatchBindResult bindResult = batchBindUsecase.execute("file.csv", csvFile);

        assertTrue(bindResult.isPartial());
        final String errorMessage = String.format("Line 1 - The hardware serial number %s is already bound to node %s in AP",
                HARDWARE_SERIAL_NUMBER_VALUE, NODE_NAME_TO_BIND);
        assertEquals(errorMessage, bindResult.getFailedBindMessages().get(0));
    }

    @Test
    public void when_single_node_with_hwId_invalid_format_then_bind_succeeds_for_all_other_nodes() {
        final byte[] csvFile = readCsvFile("valid_csv_without_headers.csv");
        doThrow(new HwIdInvalidFormatException("msg")).when(bindUseCase).execute(NODE1_FDN, HARDWARE_SERIAL_NUMBER_VALUE);

        final BatchBindResult bindResult = batchBindUsecase.execute("file.csv", csvFile);

        assertTrue(bindResult.isPartial());
        assertEquals(String.format("Line 1 - The hardware serial number %s is not valid", HARDWARE_SERIAL_NUMBER_VALUE),
                bindResult.getFailedBindMessages().get(0));
    }

    @Test
    public void when_bind_error_for_last_entry_in_csv_which_has_no_EOL_or_CR_then_error_message_contains_the_correct_line_number() {
        final byte[] csvFile = readCsvFile("valid_csv_with_no_EOL_or_CR.csv");
        doThrow(new HwIdAlreadyBoundException("msg", NODE_NAME_TO_BIND)).when(bindUseCase).execute(NODE1_FDN, HARDWARE_SERIAL_NUMBER_VALUE);
        doThrow(new HwIdAlreadyBoundException("msg", NODE_NAME_TO_BIND)).when(bindUseCase).execute(NODE2_FDN, HARDWARE_SERIAL_NUMBER_VALUE_2);

        final BatchBindResult bindResult = batchBindUsecase.execute("file.csv", csvFile);

        final String errorMessage = String.format("Line 2 - The hardware serial number %s is already bound to node %s in AP",
                HARDWARE_SERIAL_NUMBER_VALUE_2, NODE_NAME_TO_BIND);
        assertEquals(errorMessage, bindResult.getFailedBindMessages().get(1));
    }

    @Test
    public void when_bind_error_for_entry_proceeding_two_commented_lines_then_error_message_contains_the_correct_line_number() {
        final byte[] csvFile = readCsvFile("valid_csv_with_comments.csv");
        doThrow(new HwIdAlreadyBoundException("msg", NODE_NAME_TO_BIND)).when(bindUseCase).execute(NODE1_FDN, HARDWARE_SERIAL_NUMBER_VALUE);

        final BatchBindResult bindResult = batchBindUsecase.execute("file.csv", csvFile);

        final String errorMessage = String.format("Line 4 - The hardware serial number %s is already bound to node %s in AP",
                HARDWARE_SERIAL_NUMBER_VALUE, NODE_NAME_TO_BIND);
        assertEquals(errorMessage, bindResult.getFailedBindMessages().get(0));
    }

    private byte[] readCsvFile(final String csvFilename) {
        try (final InputStream is = this.getClass().getResourceAsStream("/bind/" + csvFilename)) {
            return IOUtils.toByteArray(is);
        } catch (final IOException e) {
            return null;
        }
    }
}
