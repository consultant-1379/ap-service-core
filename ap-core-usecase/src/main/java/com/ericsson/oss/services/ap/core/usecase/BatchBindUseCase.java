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

import static com.ericsson.oss.services.ap.common.model.Namespace.AP;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import javax.inject.Inject;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.AutoProvisioningService;
import com.ericsson.oss.services.ap.api.bind.BatchBindResult;
import com.ericsson.oss.services.ap.api.bind.BatchBindResult.Builder;
import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.api.exception.HwIdAlreadyBoundException;
import com.ericsson.oss.services.ap.api.exception.HwIdInvalidFormatException;
import com.ericsson.oss.services.ap.api.exception.InvalidNodeStateException;
import com.ericsson.oss.services.ap.api.exception.NodeNotFoundException;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.usecase.qualifier.UseCase;

/**
 * Bind a group of nodes.
 */
@UseCase(name = UseCaseName.BATCH_BIND)
public class BatchBindUseCase {

    private static final String BATCH_BIND_FAILURE_LOG_MESSAGE = "Batch bind failed for node {}: {}";
    private static final String NODE_NAME_MISSING = "nodename.not.set";
    private static final String HW_ID_MISSING = "hwid.not.set";
    private static final String NODE_NOT_FOUND = "node.not.found";
    private static final String INVALID_NODE_STATE = "node.invalid.state";
    private static final String HWID_ALREADY_BOUND = "hwid.already.used";
    private static final String HWID_INVALID_FORMAT = "hwid.invalid.format";
    private static final String GENERAL_BIND_ERROR = "failure.general";
    private static final String ERROR_PARSING_CSV = "failure.parsing.csv";

    private static final CSVFormat CSV_FORMAT = CSVFormat.EXCEL.withIgnoreEmptyLines().withCommentMarker('#');

    private final ApMessages apMessages = new ApMessages();

    @Inject
    private DpsQueries dpsQueries;

    @Inject
    private Logger logger;

    @Inject
    @UseCase(name = UseCaseName.BIND)
    private BindUseCase bindUseCase;

    /**
     * Executes bind for group of nodes defined in a csv file.
     *
     * @param bindCsvFile
     *            the csv file containing the values for node names and associated hardware serial number
     * @param csvFilename
     *            the name of the csv file
     * @return the batch bind result
     * @see AutoProvisioningService#batchBind(String, byte[])
     */
    public BatchBindResult execute(final String csvFilename, final byte[] bindCsvFile) {
        logger.info("Batch bind started -> filename = {}", csvFilename);

        final BatchBindResult.Builder resultBuilder = new BatchBindResult.Builder();

        try (final CSVParser csvParser = CSVParser.parse(new String(bindCsvFile, StandardCharsets.UTF_8), CSV_FORMAT)) {
            final Iterator<CSVRecord> csvRecords = csvParser.iterator();
            final BatchBindResult batchBindResult = bindAllNodesInCsvFile(resultBuilder, csvParser, csvRecords);

            logger.info("Batch bind completed -> filename = {}, successful_binds = {}, failed_binds = {}", csvFilename,
                    batchBindResult.getSuccessfulBinds(), batchBindResult.getFailedBinds());

            return batchBindResult;
        } catch (final IOException e) {
            logger.error("Error parsing bind csv", e);
            throw new ApServiceException(apMessages.get(ERROR_PARSING_CSV), e);
        }
    }

    private BatchBindResult bindAllNodesInCsvFile(final BatchBindResult.Builder resultBuilder, final CSVParser csvParser,
            final Iterator<CSVRecord> csvRecords) {
        long previousLineNumber = 0;

        while (csvRecords.hasNext()) {
            final CSVRecord csvRecord = csvRecords.next();

            final long currentLineNumber = getCurrentLineNumber(previousLineNumber, csvParser.getCurrentLineNumber());
            previousLineNumber = currentLineNumber;

            if (!isNodenameAndSerialNumberSetInCsvRecord(currentLineNumber, csvRecord, resultBuilder)) {
                continue;
            }

            bindNode(currentLineNumber, csvRecord, resultBuilder);
        }

        return resultBuilder.build();
    }

    private static long getCurrentLineNumber(final long previousLineNumber, final long currentLineNumber) {
        // If last line in CSV does not contain EOL or CR then CSVParser does not increment the line number
        return currentLineNumber == previousLineNumber ? currentLineNumber + 1 : currentLineNumber;
    }

    private boolean isNodenameAndSerialNumberSetInCsvRecord(final long currentLineNumber, final CSVRecord csvRecord, final Builder resultBuilder) {
        final String nodeName = csvRecord.get(0);

        if (StringUtils.isEmpty(nodeName)) {
            resultBuilder.withBindFailure(formatBindErrorMessage(currentLineNumber, NODE_NAME_MISSING));
            return false;
        }

        final String hardwareSerialNumber = getHardwareSerialNumberFromCsvRecord(csvRecord);

        if (StringUtils.isEmpty(hardwareSerialNumber)) {
            resultBuilder.withBindFailure(formatBindErrorMessage(currentLineNumber, HW_ID_MISSING));
            return false;
        }

        return true;
    }

    private static String getHardwareSerialNumberFromCsvRecord(final CSVRecord csvRecord) {
        return csvRecord.size() >= 2 ? csvRecord.get(1) : null;
    }

    private String resolveNodeFdn(final String nodeName) {
        final Iterator<ManagedObject> nodeMos = dpsQueries.findMoByName(nodeName, MoType.NODE.toString(), AP.toString()).execute();
        if (!nodeMos.hasNext()) {
            throw new NodeNotFoundException(nodeName);
        }
        return nodeMos.next().getFdn();
    }

    private void bindNode(final long currentLineNumber, final CSVRecord csvRecord, final Builder resultBuilder) {
        final String nodeName = csvRecord.get(0).trim();
        final String hardwareSerialNumber = csvRecord.get(1).trim();

        try {
            final String nodeFdn = resolveNodeFdn(nodeName);
            bindUseCase.execute(nodeFdn, hardwareSerialNumber);
            resultBuilder.withBindSuccess(hardwareSerialNumber, nodeName);
        } catch (final NodeNotFoundException e) {
            logger.warn(BATCH_BIND_FAILURE_LOG_MESSAGE, nodeName, e.getMessage(), e);
            resultBuilder.withBindFailure(formatBindErrorMessage(currentLineNumber, NODE_NOT_FOUND));
        } catch (final InvalidNodeStateException e) {
            logger.warn(BATCH_BIND_FAILURE_LOG_MESSAGE, nodeName, e.getMessage(), e);
            resultBuilder.withBindFailure(formatBindErrorMessage(currentLineNumber, INVALID_NODE_STATE,
                    getStateDisplayName(e.getInvalidNodeState())));
        } catch (final HwIdAlreadyBoundException e) {
            logger.warn(BATCH_BIND_FAILURE_LOG_MESSAGE, nodeName, e.getMessage(), e);
            resultBuilder.withBindFailure(formatBindErrorMessage(currentLineNumber, HWID_ALREADY_BOUND, hardwareSerialNumber, e.getNodename()));
        } catch (final HwIdInvalidFormatException e) {
            logger.warn(BATCH_BIND_FAILURE_LOG_MESSAGE, nodeName, e.getMessage(), e);
            resultBuilder.withBindFailure(formatBindErrorMessage(currentLineNumber, HWID_INVALID_FORMAT, hardwareSerialNumber));
        } catch (final Exception e) {
            logger.error(BATCH_BIND_FAILURE_LOG_MESSAGE, nodeName, e.getMessage(), e);
            resultBuilder.withBindFailure(formatBindErrorMessage(currentLineNumber, GENERAL_BIND_ERROR));
        }
    }

    private String formatBindErrorMessage(final long currentLineNumber, final String message, final Object... messageArgs) {
        return String.format("Line %d - %s", currentLineNumber, apMessages.format(message, messageArgs));
    }

    private static String getStateDisplayName(final String invalidNodeState) {
        final State state = State.getState(invalidNodeState);
        return state.getDisplayName();
    }
}
