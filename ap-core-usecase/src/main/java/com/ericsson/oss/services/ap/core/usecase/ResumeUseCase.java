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

import static com.ericsson.oss.services.ap.common.model.NodeAttribute.NODE_TYPE;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.exception.IllegalResumeOperationException;
import com.ericsson.oss.services.ap.api.exception.UnsupportedCommandException;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.common.workflow.BusinessKeyGenerator;
import com.ericsson.oss.services.ap.common.workflow.messages.ResumeMessage;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.usecase.qualifier.UseCase;
import com.ericsson.oss.services.wfs.api.WorkflowMessageCorrelationException;

/**
 * Usecase to resume integration for a node.
 */
@UseCase(name = UseCaseName.RESUME)
public class ResumeUseCase extends AbstractWorkflowExecutableUseCase {

    private static final String UNSUPPORTED_COMMAND = "command.not.supported.for.type";

    private final ApMessages apMessages = new ApMessages();

    public void execute(final String nodeFdn) {
        final ManagedObject nodeMo = getNodeMo(nodeFdn);
        if (isSupported(nodeMo)) {
            try {
                sendCorrelationMessage(nodeFdn);
            } catch (final WorkflowMessageCorrelationException e) {
                throw new IllegalResumeOperationException(e);
            } catch (final Exception e) {
                throw new ApApplicationException(String.format("Unable to resume integration for node %s", nodeFdn), e);
            }
            logger.info("Resume integration initiated for node {}", nodeFdn);
        }
        else {
            throw new UnsupportedCommandException(apMessages.get(UNSUPPORTED_COMMAND));
        }

    }

    private boolean isSupported(final ManagedObject nodeMo) {
        final String nodeType = nodeMo.getAttribute(NODE_TYPE.toString());
        final String internalNodeType = nodeTypeMapper.getInternalRepresentationFor(nodeType);
        return getApWorkflowService(internalNodeType).isSupported(ResumeMessage.getMessageKey());
    }

    private void sendCorrelationMessage(final String nodeFdn) throws WorkflowMessageCorrelationException {
        final String wfBusinessKey = BusinessKeyGenerator.generateBusinessKeyFromFdn(nodeFdn);
        wfsInstanceService.correlateMessage(ResumeMessage.getMessageKey(), wfBusinessKey);
    }
}
