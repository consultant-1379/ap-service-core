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

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.api.exception.HwIdAlreadyBoundException;
import com.ericsson.oss.services.ap.api.exception.HwIdInvalidFormatException;
import com.ericsson.oss.services.ap.api.exception.UnsupportedCommandException;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.api.status.StateTransitionEvent;
import com.ericsson.oss.services.ap.api.status.StateTransitionManagerLocal;
import com.ericsson.oss.services.ap.api.workflow.AutoProvisioningWorkflowService;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.cm.TransactionalExecutor;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.Namespace;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.ericsson.oss.services.ap.common.workflow.BusinessKeyGenerator;
import com.ericsson.oss.services.ap.common.workflow.messages.BindMessage;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.usecase.bind.HardwareSerialNumberValidator;
import com.ericsson.oss.services.ap.core.usecase.qualifier.UseCase;
import com.ericsson.oss.services.wfs.api.WorkflowMessageCorrelationException;
import com.ericsson.oss.services.wfs.jee.api.WorkflowInstanceServiceLocal;
import com.ericsson.oss.services.ap.core.usecase.bind.Attributes;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.Callable;
import java.text.SimpleDateFormat;

import static com.ericsson.oss.services.ap.common.model.NodeAttribute.NODE_TYPE;

/**
 * Usecase to Bind a node.
 */
@UseCase(name = UseCaseName.BIND)
public class BindUseCase {

    private static final String UNSUPPORTED_COMMAND = "command.not.supported.for.type";

    private static final String HWID_ALREADY_USED = "hwid.already.used";

    private static final String HWID_ALREADY_USED_DUPLICATE = "hwid.already.used.duplicate";

    private final ApMessages apMessages = new ApMessages();

    private ServiceFinderBean serviceFinder = new ServiceFinderBean(); // NOPMD

    private TransactionalExecutor executor = new TransactionalExecutor(); // NOPMD

    @Inject
    private DpsOperations dps;

    private StateTransitionManagerLocal stateTransitionManager;

    private WorkflowInstanceServiceLocal wfsInstanceService;

    @Inject
    private DpsQueries dpsQueries;

    @Inject
    private Logger logger;

    @Inject
    private NodeTypeMapper nodeTypeMapper;

    @PostConstruct
    public void init() {
        stateTransitionManager = new ServiceFinderBean().find(StateTransitionManagerLocal.class);
        wfsInstanceService = new ServiceFinderBean().find(WorkflowInstanceServiceLocal.class);
    }

    private static String getDateFormat() {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return dateFormat.format(new Date());
    }

    /**
     * Executes the bind for the specified node and hardware serial number.
     *
     * @param nodeFdn
     *            the FDN of node in AP model
     * @param hardwareSerialNumber
     *            associated hardware serial number for the node
     * @throws ApServiceException
     *             if there is an unexpected error executing the bind
     * @throws HwIdAlreadyBoundException
     *             if hardwareSerialNumber is already bound to another node
     * @throws HwIdInvalidFormatException
     *             if hardwareSerialNumber is not in the correct format
     */
    public void execute(final String nodeFdn, final String hardwareSerialNumber) {
        logger.info("Bind started for node {}, with hardware serial number {}", nodeFdn, hardwareSerialNumber);
        final ManagedObject nodeMo = getNodeMo(nodeFdn);

        if (!isSupported(nodeMo)) {
            throw new UnsupportedCommandException(apMessages.get(UNSUPPORTED_COMMAND));
        }

        if(!validateHwSerialToBind(nodeFdn, hardwareSerialNumber)){
            return;
        }

        final boolean migrationNode = isMigrationNode(nodeFdn);
        if (migrationNode) {
            stateTransitionManager.validateAndSetNextState(nodeFdn, StateTransitionEvent.PRE_MIGRATION_BIND_STARTED);
        } else {
            stateTransitionManager.validateAndSetNextState(nodeFdn, StateTransitionEvent.BIND_STARTED);
        }
        executeBindNode(nodeFdn, hardwareSerialNumber);

        if (isBindFailed(nodeFdn)) {
            setNodeStateToBindFailedState(nodeFdn, nodeMo, hardwareSerialNumber);
        }
        logger.info("Bind completed for node {} with hardware serial number {}", nodeFdn, hardwareSerialNumber);
    }

    private boolean validateHwSerialToBind(final String nodeFdn, final String hardwareSerialNumber){
        final Set<String> bindNodeErrors = new HashSet<>();
        boolean validHardwareSerialToBindFromAP= isValidHardwareSerialToBindFromAP(nodeFdn, hardwareSerialNumber, bindNodeErrors);
        final ManagedObject nodeMo = getNodeMo(nodeFdn);
        boolean validHardwareSerialToBindWithENMNodes = isValidHardwareSerialToBindWithENMNodes(getNodeType(nodeMo), hardwareSerialNumber, bindNodeErrors);
        final List<String> bindErrors = getBindErrors(bindNodeErrors);
        if(!bindErrors.isEmpty()){
            throw new HwIdAlreadyBoundException(bindErrors,String.format("hardwareSerialNumber %s already bound", hardwareSerialNumber));
        }
        return validHardwareSerialToBindFromAP && validHardwareSerialToBindWithENMNodes;
    }

    private void setNodeStateToBindFailedState(String nodeFdn, ManagedObject nodeMo, String hardwareSerialNumber) {
        if (isValidStateTransitionToBindFailed(nodeMo, StateTransitionEvent.BIND_FAILED)) {
            stateTransitionManager.validateAndSetNextState(nodeFdn, StateTransitionEvent.BIND_FAILED);
            logger.error("Bind failed for node: {} with hardware serial number: {}. State Changed to BIND_FAILED", nodeFdn, hardwareSerialNumber);
        }
        if (isValidStateTransitionToBindFailed(nodeMo, StateTransitionEvent.PRE_MIGRATION_BIND_FAILED)) {
            stateTransitionManager.validateAndSetNextState(nodeFdn, StateTransitionEvent.PRE_MIGRATION_BIND_FAILED);
            logger.error("Bind failed for migration node: {} with hardware serial number: {}. State Changed to PRE MIGRATION BIND FAILED", nodeFdn, hardwareSerialNumber);
        }
        throw new ApApplicationException(String.format("Bind failed for node: %s with hardware serial number: %s", nodeFdn, hardwareSerialNumber));
    }

    private boolean isMigrationNode(String nodeFdn) {
        final ManagedObject apNodeMo = getNodeMo(nodeFdn);
        if (apNodeMo.getAttribute(NodeAttribute.IS_NODE_MIGRATION.toString()) != null) {
            return (boolean) apNodeMo.getAttribute(NodeAttribute.IS_NODE_MIGRATION.toString());
        }
        return false;
    }


    private boolean isValidStateTransitionToBindFailed(final ManagedObject nodeMo, final StateTransitionEvent stateTransitionEvent) {
        return stateTransitionManager.isValidStateTransition(
            nodeMo.getChild(MoType.NODE_STATUS.toString() + "=1").getAttribute(NodeStatusAttribute.STATE.toString()),
            stateTransitionEvent);
    }

    private void executeBindNode(final String nodeFdn, final String hardwareSerialNumber) {
        try {
            sendBindCorrelationMessage(nodeFdn, hardwareSerialNumber);
        } catch (final ApServiceException e) {
            throw e;
        } catch (final Exception e) {
            throw new ApApplicationException(String.format("Unable to initiate bind for node %s with hardware serial number %s", nodeFdn,
                hardwareSerialNumber), e);
        }
    }

    private boolean isValidHardwareSerialToBindFromAP(final String nodeFdn, final String hardwareSerialNumber,final Set<String> bindNodeErrors) {
        if (!HardwareSerialNumberValidator.isValidSerialNumber(hardwareSerialNumber)) {
            throw new HwIdInvalidFormatException(String.format("The hardware serial number %s is not valid", hardwareSerialNumber));
        }

        final String nodeFdnWithMatchingHwId = findNodeFdnWithMatchingHwId(hardwareSerialNumber);
        if (nodeFdn.equalsIgnoreCase(nodeFdnWithMatchingHwId)) {
            return false;
        }

        if (nodeFdnWithMatchingHwId != null) {
            final String nodeNameWithMatchingHwId = FDN.get(nodeFdnWithMatchingHwId).getRdnValue();
            logger.info("Node name with MatchingHwId from AP dps:{}", nodeNameWithMatchingHwId);
            recordNodeBindError(bindNodeErrors, HWID_ALREADY_USED, hardwareSerialNumber, nodeNameWithMatchingHwId);
            return false;
        }
        return true;
    }

    private ManagedObject getNodeMo(final String nodeFdn) {
        try {
            return dps.getDataPersistenceService().getLiveBucket().findMoByFdn(nodeFdn);
        } catch (final Exception e) {
            throw new ApApplicationException(String.format("Error reading MO for %s", nodeFdn), e);
        }
    }

    private boolean isSupported(final ManagedObject nodeMo) {
        final String nodeType = getNodeType(nodeMo);
        final String internalNodeType = nodeTypeMapper.getInternalEjbQualifier(nodeType);
        return getApWorkflowService(internalNodeType).isSupported(BindMessage.getMessageKey());
    }

    private static String getNodeType(final ManagedObject nodeMo) {
        return nodeMo.getAttribute(NODE_TYPE.toString());
    }

    private AutoProvisioningWorkflowService getApWorkflowService(final String nodeType) {
        return serviceFinder.find(AutoProvisioningWorkflowService.class, nodeType.toLowerCase(Locale.US));
    }

    private String findNodeFdnWithMatchingHwId(final String hardwareSerialNumber) {
        logger.info("Request sent to dps to find MOs with same HwID from AP at:{}", getDateFormat());
        final Iterator<ManagedObject> mosWithMatchingHwId = dpsQueries.findMosWithAttributeValue(NodeAttribute.HARDWARE_SERIAL_NUMBER.toString(),
            hardwareSerialNumber, Namespace.AP.toString(), MoType.NODE.toString()).execute();
        logger.info("Response received from AP dps at:{}", getDateFormat());
        return mosWithMatchingHwId.hasNext() ? mosWithMatchingHwId.next().getFdn() : null;
    }

    private boolean isValidHardwareSerialToBindWithENMNodes(final String nodeType, final String hardwareSerialNumber, final Set<String> bindNodeErrors) {

        Attributes attributes = new Attributes(nodeTypeMapper.toOssRepresentation(nodeType));

        logger.info("Request sent to dps to find MOs with same HwID from ENM at:{}", getDateFormat());
        final Iterator<ManagedObject> mosWithMatchingHwId = dpsQueries.findMosWithAttributeValue(attributes.getAttribute(),
            hardwareSerialNumber, attributes.getNamespace(), attributes.getMoType()).execute();
        logger.info("Response received from ENM dps at:{}", getDateFormat());

        final List<String> nodeNamesWithMatchingHwId = new ArrayList<>();
        while (mosWithMatchingHwId.hasNext()){
            final String fdn = mosWithMatchingHwId.next().getFdn();
            final String nodeNameInEnm = new FDN(fdn).getNodeName();
            nodeNamesWithMatchingHwId.add(nodeNameInEnm);
            recordNodeBindError( bindNodeErrors, HWID_ALREADY_USED_DUPLICATE, hardwareSerialNumber, nodeNameInEnm);
        }
        if (nodeNamesWithMatchingHwId.isEmpty()) {
            logger.info("Node names with matching HwId from ENM:{}",nodeNamesWithMatchingHwId);
            return true;
        }

        return false;
    }

    private void sendBindCorrelationMessage(final String nodeFdn, final String hardwareSerialNumber) throws WorkflowMessageCorrelationException {
        final String wfBusinessKey = BusinessKeyGenerator.generateBusinessKeyFromFdn(nodeFdn);
        wfsInstanceService.correlateMessage(BindMessage.getMessageKey(), wfBusinessKey, BindMessage.getMessageVariables(hardwareSerialNumber));
    }

    private boolean isBindFailed(final String nodeFdn) {
        final String nodeState = getStateFromNodeStatusMoInTransaction(nodeFdn);
        logger.info("After bind execution, the state of node {} is changed to {}", nodeFdn, nodeState);
        return !(State.BIND_COMPLETED.toString().equals(nodeState) || State.PRE_MIGRATION_BIND_COMPLETED.toString().equals(nodeState));
    }

    private String getStateFromNodeStatusMoInTransaction(final String nodeFdn) {
        final Callable<String> callable = () -> {
            final ManagedObject nodeStatusMo = dps.getDataPersistenceService().getLiveBucket().findMoByFdn(nodeFdn + ",NodeStatus=1");
            return nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString());
        };

        try {
            return executor.execute(callable);
        } catch (final Exception e) {
            throw new ApServiceException(String.format("Error finding State from NodeStatus MO of Node %s", nodeFdn), e);
        }
    }

    private final void recordNodeBindError( final Set<String> bindNodeErrors, final String key, final String... args) {
        final String bindErrorMessage = apMessages.format(key, (Object[]) args);
        logger.error(bindErrorMessage);
        bindNodeErrors.add(bindErrorMessage);
    }

    private List<String> getBindErrors(final Set<String> bindNodeErrors) {
        final List<String> allErrors = new ArrayList<>();
        allErrors.addAll(bindNodeErrors);
        return Collections.unmodifiableList(allErrors);
    }

}
