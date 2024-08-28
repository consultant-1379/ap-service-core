/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.pkira;

import static com.ericsson.oss.services.ap.api.status.State.ORDER_STARTED;
import static com.ericsson.oss.services.ap.api.status.State.HARDWARE_REPLACE_BIND_COMPLETED;
import static com.ericsson.oss.services.ap.api.status.State.HARDWARE_REPLACE_STARTED;
import static com.ericsson.oss.services.ap.api.status.StatusEntryNames.ENROLL_IPSEC_CERTIFICATE;
import static com.ericsson.oss.services.ap.api.status.StatusEntryNames.ENROLL_OAM_CERTIFICATE;
import static com.ericsson.oss.services.ap.common.model.Namespace.AP;
import static java.util.stream.Collectors.toSet;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.itpf.security.pki.ra.model.edt.CertificateEnrollmentStatusType;
import com.ericsson.oss.itpf.security.pki.ra.model.events.CertificateEnrollmentStatus;
import com.ericsson.oss.services.ap.api.cluster.APServiceClusterMember;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.api.status.StatusEntryManagerLocal;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute;
import com.ericsson.oss.services.ap.core.status.NodeStatusMoUpdater;

import javax.ejb.Asynchronous;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;

@Stateless
public class CertificateEnrollmentEventProcessor {

    @Inject
    private DpsQueries dpsQueries;

    @Inject
    private NodeStatusMoUpdater nodeStatusMoUpdater;

    @Inject
    private Logger logger;

    @Inject
    private StatusEntryManagerLocal statusEntryManager;

    @EServiceRef
    private APServiceClusterMember apServiceClusterMember;

    private static final String OAM = "OAM";
    private static final String IPSEC = "IPSEC";
    private static final String EMPTY_STRING = "";

    private static final Set<String> NODE_NOT_ALREADY_UP_STATES = Stream.of(
        State.ORDER_STARTED.toString(),
        State.ORDER_COMPLETED.toString(),
        State.PRE_MIGRATION_STARTED.toString(),
        State.PRE_MIGRATION_COMPLETED.toString(),
        State.BIND_STARTED.toString(),
        State.INTEGRATION_FAILED.toString(),
        State.INTEGRATION_STARTED.toString(),
        State.MIGRATION_STARTED.toString(),
        State.MIGRATION_FAILED.toString(),
        State.BIND_COMPLETED.toString(),
        State.HARDWARE_REPLACE_STARTED.toString(),
        State.HARDWARE_REPLACE_FAILED.toString(),
        State.HARDWARE_REPLACE_BIND_COMPLETED.toString(),
        State.HARDWARE_REPLACE_SUSPENDED.toString()
    ).collect(toSet());

    private static final Set<String> HARDWARE_REPLACE_STATES_FOR_UPDATE = Stream.of(
        State.HARDWARE_REPLACE_STARTED.toString(),
        State.HARDWARE_REPLACE_FAILED.toString(),
        State.HARDWARE_REPLACE_BIND_COMPLETED.toString()
    ).collect(toSet());

    /**
     * Consumes a notification from PKI-RA and updates the node's AP status and status entry to reflect the certificate enrollment status.
     *
     * @param certificateEnrollmentStatus
     *            notification from PKI RA that contains a serial number identifying the node.
     */
    @Lock(LockType.READ)
    @Asynchronous
    public void processNotification(final CertificateEnrollmentStatus certificateEnrollmentStatus) {
        if (!apServiceClusterMember.isMasterNode()) {
            logger.debug("Ignoring CertificateEnrollmentStatus notification, not master node");
            return;
        }
        final CertificateEnrollmentStatusType certificateEnrollmentStatusType = certificateEnrollmentStatus.getCertificateEnrollmentStatusType();
        final String certificateIssuerName = certificateEnrollmentStatus.getissuerName();
        logger.info(
                "Certificate enrollment status notification received from PKI-RA for node name {}, : certificate type {}, : result {}, : issuer name : {}",
                certificateEnrollmentStatus.getNodeName(), certificateEnrollmentStatus.getCertificateType(),
                certificateEnrollmentStatusType, certificateIssuerName);
        final ManagedObject apNodeMo = getApNodeMo(certificateEnrollmentStatus);

        if (isNodeValid(apNodeMo)) {
            switch (certificateEnrollmentStatusType) {
                case START:
                    setNodeStateForEnrollmentStatus(apNodeMo, true);
                    statusEntryManager.taskStarted(apNodeMo.getFdn(), getStatusEntryName(certificateIssuerName));
                    return;
                case SUCCESS:
                    setNodeStateForEnrollmentStatus(apNodeMo, true);
                    statusEntryManager.taskCompleted(apNodeMo.getFdn(), getStatusEntryName(certificateIssuerName));
                    return;
                case FAILURE:
                    setNodeStateForEnrollmentStatus(apNodeMo, false);
                    statusEntryManager.taskFailed(apNodeMo.getFdn(), getStatusEntryName(certificateIssuerName), EMPTY_STRING);
                    return;
                case CERTIFICATE_SENT:
                    logger.debug("CERTIFICATE_SENT status of Certificate Enrollment received for node name {}", certificateEnrollmentStatus.getNodeName());
            }
        }
    }

    private void setNodeStateForEnrollmentStatus(final ManagedObject apNodeMo, final boolean isSuccess) {
        if (isMigrationNode(apNodeMo)) {
            if (!isNodeInPreMigrationStartedState(apNodeMo)) {
                setNodeState(apNodeMo, isSuccess ? State.MIGRATION_STARTED : State.MIGRATION_FAILED);
            }
        } else if (isHardwareReplaceOngoingState(apNodeMo)) {
            setNodeState(apNodeMo, isSuccess ? State.HARDWARE_REPLACE_STARTED : State.HARDWARE_REPLACE_FAILED);
        } else {
            setIntegrationState(isSuccess ? State.INTEGRATION_STARTED : State.INTEGRATION_FAILED, apNodeMo);
        }
    }

    private static boolean isMigrationNode(final ManagedObject apNodeMo) {
        if (apNodeMo.getAttribute(NodeAttribute.IS_NODE_MIGRATION.toString()) != null) {
            return (boolean) apNodeMo.getAttribute(NodeAttribute.IS_NODE_MIGRATION.toString());
        }
        return false;
    }

    private ManagedObject getApNodeMo(final CertificateEnrollmentStatus certificateEnrollmentStatus) {
        final String nodeName = certificateEnrollmentStatus.getNodeName();
        final Iterator<ManagedObject> apNodeMos = dpsQueries.findMoByName(nodeName, MoType.NODE.toString(), AP.toString()).execute();
        return apNodeMos.hasNext() ? apNodeMos.next() : null;
    }

    private boolean isNodeValid(final ManagedObject apNode) {
        if (apNode != null) {
            final ManagedObject apNodeStatusMo = apNode.getChild(MoType.NODE_STATUS + "=1");
            if (apNodeStatusMo != null) {
                final String nodeState = apNodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString());
                return NODE_NOT_ALREADY_UP_STATES.contains(nodeState);
            }
        }
        return false;
    }

    private String getStatusEntryName(final String issuerName) {
        if (issuerName != null) {
            if (issuerName.toUpperCase().contains(IPSEC)) {
                return ENROLL_IPSEC_CERTIFICATE.toString();
            } else if (issuerName.toUpperCase().contains(OAM)) {
                return ENROLL_OAM_CERTIFICATE.toString();
            }
        }
        return EMPTY_STRING;
    }

    private void setIntegrationState(final State state, final ManagedObject apNodeMo) {
        if (!isHardwareReplace(apNodeMo) && !isNodeInOrderStartedState(apNodeMo)) {
            setNodeState(apNodeMo, state);
        }
    }

    private void setNodeState(ManagedObject apNodeMo, State state) {
        if (!state.toString().equals(getNodeState(apNodeMo))) {
            nodeStatusMoUpdater.setState(apNodeMo.getFdn(), NodeStatusAttribute.STATE.toString(), state.name());
        }
    }

    private boolean isNodeInPreMigrationStartedState(final ManagedObject apNodeMo) {
        return State.PRE_MIGRATION_STARTED.toString().equals(getNodeState(apNodeMo));
    }

    private boolean isHardwareReplace(final ManagedObject apNodeMo) {
        return apNodeMo.getAttribute(NodeAttribute.IS_HARDWARE_REPLACE_NODE.toString());
    }

    private boolean isNodeInOrderStartedState(final ManagedObject apNodeMo) {
        return ORDER_STARTED.toString().equals(getNodeState(apNodeMo));
    }

    private boolean isHardwareReplaceOngoingState(final ManagedObject apNodeMo) {
        return HARDWARE_REPLACE_STATES_FOR_UPDATE.contains(getNodeState(apNodeMo));
    }

    private String getNodeState(ManagedObject apNodeMo) {
        final ManagedObject apNodeStatusMo = apNodeMo.getChild(MoType.NODE_STATUS + "=1");
        return apNodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString());
    }

}
