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

import static com.ericsson.oss.services.ap.api.status.State.MIGRATION_FAILED;
import static com.ericsson.oss.services.ap.api.status.State.MIGRATION_IMPORT_CONFIGURATION_SUSPENDED;
import static com.ericsson.oss.services.ap.api.status.State.MIGRATION_STARTED;
import static com.ericsson.oss.services.ap.api.status.State.MIGRATION_SUSPENDED;

import static com.ericsson.oss.services.ap.api.status.State.PRE_MIGRATION_BIND_COMPLETED;
import static com.ericsson.oss.services.ap.api.status.State.PRE_MIGRATION_BIND_STARTED;
import static com.ericsson.oss.services.ap.api.status.State.PRE_MIGRATION_COMPLETED;
import static com.ericsson.oss.services.ap.api.status.State.PRE_MIGRATION_IMPORT_CONFIGURATION_SUSPENDED;
import static com.ericsson.oss.services.ap.api.status.State.PRE_MIGRATION_STARTED;
import static com.ericsson.oss.services.ap.api.status.State.PRE_MIGRATION_SUSPENDED;

import static com.ericsson.oss.services.ap.common.model.NodeAttribute.NODE_TYPE;

import java.util.HashMap;
import java.util.Map;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.exception.IllegalCancelOperationException;
import com.ericsson.oss.services.ap.api.exception.UnsupportedCommandException;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.common.workflow.BusinessKeyGenerator;
import com.ericsson.oss.services.ap.common.workflow.messages.CancelMessage;
import com.ericsson.oss.services.ap.common.workflow.messages.MigrationCancelMessage;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.usecase.qualifier.UseCase;
import com.ericsson.oss.services.wfs.api.WorkflowMessageCorrelationException;

/**
 * Usecase to cancel AP activity for a node.
 */
@UseCase(name = UseCaseName.CANCEL)
public class CancelUseCase extends AbstractWorkflowExecutableUseCase {

    private static final String UNSUPPORTED_COMMAND = "command.not.supported.for.type";
    private static final Map<String, String> validNodeStateForCancelMap = new HashMap<>();

    static {
        validNodeStateForCancelMap.put(PRE_MIGRATION_STARTED.name(), MigrationCancelMessage.getMessageKey());
        validNodeStateForCancelMap.put(PRE_MIGRATION_SUSPENDED.name(), MigrationCancelMessage.getMessageKey());
        validNodeStateForCancelMap.put(PRE_MIGRATION_IMPORT_CONFIGURATION_SUSPENDED.name(), MigrationCancelMessage.getMessageKey());
        validNodeStateForCancelMap.put(PRE_MIGRATION_COMPLETED.name(), MigrationCancelMessage.getMessageKey());
        validNodeStateForCancelMap.put(PRE_MIGRATION_BIND_STARTED.name(), MigrationCancelMessage.getMessageKey());
        validNodeStateForCancelMap.put(PRE_MIGRATION_BIND_COMPLETED.name(), MigrationCancelMessage.getMessageKey());
        validNodeStateForCancelMap.put(MIGRATION_STARTED.name(), MigrationCancelMessage.getMessageKey());
        validNodeStateForCancelMap.put(MIGRATION_SUSPENDED.name(), MigrationCancelMessage.getMessageKey());
        validNodeStateForCancelMap.put(MIGRATION_IMPORT_CONFIGURATION_SUSPENDED.name(), MigrationCancelMessage.getMessageKey());
        validNodeStateForCancelMap.put(MIGRATION_FAILED.name(), MigrationCancelMessage.getMessageKey());
    }
    private final ApMessages apMessages = new ApMessages();

    /**
     * Executes the cancel command. Workflow has to be in a valid state for this command to execute.
     *
     * @param nodeFdn
     *            the FDN of the AP node
     * @throws UnsupportedCommandException
     *             if the command is unsupported for the node type
     */
    @Override
    public void execute(final String nodeFdn) {
        final ManagedObject nodeMo = getNodeMo(nodeFdn);

        if (isSupported(nodeMo)) {
            try {
                sendCorrelationMessage(nodeFdn, getMessageKeyFromNodeState(nodeMo));
            } catch (final WorkflowMessageCorrelationException e) {
                throw new IllegalCancelOperationException(e);
            } catch (final Exception e) {
                throw new ApApplicationException(String.format("Unable to cancel AP activity for node %s", nodeFdn), e);
            }

            logger.info("Cancel initiated for node {}", nodeFdn);
        } else {
            throw new UnsupportedCommandException(apMessages.get(UNSUPPORTED_COMMAND));
        }
    }

    private void sendCorrelationMessage(final String nodeFdn, final String messageKey) throws WorkflowMessageCorrelationException {
        final String wfBusinessKey = BusinessKeyGenerator.generateBusinessKeyFromFdn(nodeFdn);
        wfsInstanceService.correlateMessage(messageKey, wfBusinessKey);
    }

    private boolean isSupported(final ManagedObject nodeMo) {
        final String nodeType = getNodeType(nodeMo);
        final String internalNodeType = nodeTypeMapper.getInternalRepresentationFor(nodeType);
        return getApWorkflowService(internalNodeType).isSupported(CancelMessage.getMessageKey());
    }

    private static String getNodeType(final ManagedObject nodeMo) {
        return nodeMo.getAttribute(NODE_TYPE.toString());
    }

    private String getMessageKeyFromNodeState(final ManagedObject nodeMo) {
        final String currentNodeStatus = nodeMo.getChild(MoType.NODE_STATUS.toString() + "=1")
                .getAttribute(NodeStatusAttribute.STATE.toString());
        final String messageKey = validNodeStateForCancelMap.get(currentNodeStatus);
        return messageKey != null ? messageKey : CancelMessage.getMessageKey();
    }
}
