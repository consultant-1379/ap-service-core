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
package com.ericsson.oss.service.ap.core.pkira

import static com.ericsson.oss.itpf.security.pki.ra.model.edt.CertificateEnrollmentStatusType.CERTIFICATE_SENT
import static com.ericsson.oss.itpf.security.pki.ra.model.edt.CertificateEnrollmentStatusType.FAILURE
import static com.ericsson.oss.itpf.security.pki.ra.model.edt.CertificateEnrollmentStatusType.START
import static com.ericsson.oss.itpf.security.pki.ra.model.edt.CertificateEnrollmentStatusType.SUCCESS
import static com.ericsson.oss.services.ap.api.status.State.BIND_COMPLETED
import static com.ericsson.oss.services.ap.api.status.State.BIND_STARTED
import static com.ericsson.oss.services.ap.api.status.State.HARDWARE_REPLACE_BIND_COMPLETED
import static com.ericsson.oss.services.ap.api.status.State.HARDWARE_REPLACE_FAILED
import static com.ericsson.oss.services.ap.api.status.State.HARDWARE_REPLACE_STARTED
import static com.ericsson.oss.services.ap.api.status.State.HARDWARE_REPLACE_SUSPENDED
import static com.ericsson.oss.services.ap.api.status.State.INTEGRATION_FAILED
import static com.ericsson.oss.services.ap.api.status.State.INTEGRATION_STARTED
import static com.ericsson.oss.services.ap.api.status.State.MIGRATION_FAILED
import static com.ericsson.oss.services.ap.api.status.State.MIGRATION_STARTED
import static com.ericsson.oss.services.ap.api.status.State.ORDER_COMPLETED
import static com.ericsson.oss.services.ap.api.status.State.ORDER_STARTED
import static com.ericsson.oss.services.ap.api.status.State.PRE_MIGRATION_COMPLETED
import static com.ericsson.oss.services.ap.api.status.State.PRE_MIGRATION_STARTED

import com.ericsson.cds.cdi.support.rule.ImplementationClasses
import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.security.pki.ra.model.events.CertificateEnrollmentStatus
import com.ericsson.oss.service.ap.core.common.test.AbstractNodeStatusSpec
import com.ericsson.oss.services.ap.api.cluster.APServiceClusterMember
import com.ericsson.oss.services.ap.api.status.StatusEntryManagerLocal
import com.ericsson.oss.services.ap.api.status.StatusEntryProgress
import com.ericsson.oss.services.ap.common.model.NodeAttribute
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute
import com.ericsson.oss.services.ap.common.test.util.assertions.CommonAssertionsSpec
import com.ericsson.oss.services.ap.common.test.util.setup.MoCreatorSpec
import com.ericsson.oss.services.ap.core.pkira.CertificateEnrollmentEventConsumer
import com.ericsson.oss.services.ap.core.status.StatusEntryManagerEjb

class CertificateEnrollmentEventConsumerSpec extends AbstractNodeStatusSpec {

    private static final String ISSUER_NAME_OAM = "NE_OAM_CA"
    private static final String ISSUER_NAME_IPSEC = "NE_IPsec_CA"
    private static final String TASK_NAME_OAM = "Enroll OAM Certificate"
    private static final String TASK_NAME_IPSEC = "Enroll IPSec Certificate"

    @ImplementationClasses
    private static final def definedClasses = [StatusEntryManagerEjb]

    @MockedImplementation
    private APServiceClusterMember apServiceClusterMembership

    @MockedImplementation
    private StatusEntryManagerLocal statusEntryManager

    @ObjectUnderTest
    private CertificateEnrollmentEventConsumer certificateEnrollmentEventConsumer

    private Map<String, Object> statusAttributes = new HashMap<String, Object>()

    @Override
    def setup() {
        apServiceClusterMembership.isMasterNode() >> true
        statusAttributes.clear()
        assert definedClasses != null // work around for sonar
    }

    def "Certificate enrollment progress is reported started in AP when valid notifications are received"() {

        given: "that an AP node exist"
            final ManagedObject apNodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)
            statusAttributes.put(NodeStatusAttribute.STATE.toString(), initialNodeState.toString())
            MoCreatorSpec.createNodeStatusMo(NODE_FDN, apNodeMo, statusAttributes)

        when: "A certificate enrollment notification is received"
            final CertificateEnrollmentStatus certificateEnrollmentStatus = newCertificateEnrollmentStatus(issuerName,
                certificateEnrollmentStatusType)
            certificateEnrollmentEventConsumer.listenToCertificateEnrollmentStatusNotifications(certificateEnrollmentStatus)

        then:
            assertStatusEntries(statusEntryManager.getAllStatusEntries(NODE_FDN), taskName, StatusEntryProgress.STARTED.toString())
            CommonAssertionsSpec.assertIntegrationState(dps, NODE_FDN, updatedNodeState.toString())

        where:
            initialNodeState   | issuerName         | certificateEnrollmentStatusType || taskName         | updatedNodeState
            ORDER_COMPLETED    | ISSUER_NAME_OAM    | START                           || TASK_NAME_OAM    | INTEGRATION_STARTED
            BIND_STARTED       | ISSUER_NAME_IPSEC  | START                           || TASK_NAME_IPSEC  | INTEGRATION_STARTED
            INTEGRATION_FAILED | ISSUER_NAME_OAM    | START                           || TASK_NAME_OAM    | INTEGRATION_STARTED
            ORDER_STARTED      | ISSUER_NAME_OAM    | START                           || TASK_NAME_OAM    | ORDER_STARTED
            ORDER_STARTED      | ISSUER_NAME_IPSEC  | START                           || TASK_NAME_IPSEC  | ORDER_STARTED
    }

    def "Certificate enrollment progress is reported in AP when valid notifications are received for hardware replace"() {

        given: "that an AP node exist"
            final ManagedObject apNodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)
            statusAttributes.put(NodeStatusAttribute.STATE.toString(), initialNodeState.toString())
            MoCreatorSpec.createNodeStatusMo(NODE_FDN, apNodeMo, statusAttributes)
            apNodeMo.setAttribute(NodeAttribute.IS_HARDWARE_REPLACE_NODE.toString(), true)

        when: "A certificate enrollment notification is received"
            final CertificateEnrollmentStatus certificateEnrollmentStatus = newCertificateEnrollmentStatus(issuerName,
                certificateEnrollmentStatusType)
            certificateEnrollmentEventConsumer.listenToCertificateEnrollmentStatusNotifications(certificateEnrollmentStatus)

        then:
            assertStatusEntries(statusEntryManager.getAllStatusEntries(NODE_FDN), taskName, StatusEntryProgress.STARTED.toString())
            CommonAssertionsSpec.assertIntegrationState(dps, NODE_FDN, updatedNodeState.toString())

        where:
            initialNodeState                | issuerName         | certificateEnrollmentStatusType || taskName         | updatedNodeState
            HARDWARE_REPLACE_STARTED        | ISSUER_NAME_IPSEC  | START                           || TASK_NAME_IPSEC  | HARDWARE_REPLACE_STARTED
            HARDWARE_REPLACE_FAILED         | ISSUER_NAME_OAM    | START                           || TASK_NAME_OAM    | HARDWARE_REPLACE_STARTED
            HARDWARE_REPLACE_BIND_COMPLETED | ISSUER_NAME_IPSEC  | START                           || TASK_NAME_IPSEC  | HARDWARE_REPLACE_STARTED
            BIND_STARTED                    | ISSUER_NAME_OAM    | START                           || TASK_NAME_OAM    | BIND_STARTED
            HARDWARE_REPLACE_FAILED         | ISSUER_NAME_IPSEC  | SUCCESS                         || TASK_NAME_IPSEC  | HARDWARE_REPLACE_STARTED
            HARDWARE_REPLACE_BIND_COMPLETED | ISSUER_NAME_OAM    | SUCCESS                         || TASK_NAME_OAM    | HARDWARE_REPLACE_STARTED

    }

    def "Certificate enrollment progress is reported started in AP when valid notifications are received for migration node"() {

        given: "that an AP node exist"
        final ManagedObject apNodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)
        statusAttributes.put(NodeStatusAttribute.STATE.toString(), initialNodeState.toString())
        MoCreatorSpec.createNodeStatusMo(NODE_FDN, apNodeMo, statusAttributes)
        apNodeMo.setAttribute(NodeAttribute.IS_NODE_MIGRATION.toString(), true)

        when: "certificate enrollment notification is received"
        final CertificateEnrollmentStatus certificateEnrollmentStatus = newCertificateEnrollmentStatus(issuerName,
                certificateEnrollmentStatusType)
        certificateEnrollmentEventConsumer.listenToCertificateEnrollmentStatusNotifications(certificateEnrollmentStatus)

        then:
        assertStatusEntries(statusEntryManager.getAllStatusEntries(NODE_FDN), taskName, StatusEntryProgress.STARTED.toString())
        CommonAssertionsSpec.assertIntegrationState(dps, NODE_FDN, updatedNodeState.toString())

        where:
        initialNodeState        | issuerName        | certificateEnrollmentStatusType || taskName        | updatedNodeState
        PRE_MIGRATION_COMPLETED | ISSUER_NAME_OAM   | START                           || TASK_NAME_OAM   | MIGRATION_STARTED
        BIND_STARTED            | ISSUER_NAME_IPSEC | START                           || TASK_NAME_IPSEC | MIGRATION_STARTED
        MIGRATION_FAILED        | ISSUER_NAME_OAM   | START                           || TASK_NAME_OAM   | MIGRATION_STARTED
        PRE_MIGRATION_STARTED   | ISSUER_NAME_OAM   | START                           || TASK_NAME_OAM   | PRE_MIGRATION_STARTED
    }

    def "Certificate enrollment progress is reported integration as already in correct state in AP when valid notifications are received"() {

        given: "that an AP node exist"
            final ManagedObject apNodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)
            statusAttributes.put(NodeStatusAttribute.STATE.toString(), state.toString())
            MoCreatorSpec.createNodeStatusMo(NODE_FDN, apNodeMo, statusAttributes)

        when: "A certificate enrollment notification is received"
            final CertificateEnrollmentStatus certificateEnrollmentStatus = newCertificateEnrollmentStatus(issuerName,
                certificateEnrollmentStatusType)
            certificateEnrollmentEventConsumer.listenToCertificateEnrollmentStatusNotifications(certificateEnrollmentStatus)

        then:
            assertStatusEntries(statusEntryManager.getAllStatusEntries(NODE_FDN), taskName, StatusEntryProgress.STARTED.toString())
            CommonAssertionsSpec.assertIntegrationState(dps, NODE_FDN, state.toString())

        where:
            state               | issuerName        | certificateEnrollmentStatusType || taskName
            INTEGRATION_STARTED | ISSUER_NAME_OAM   | START                           || TASK_NAME_OAM
    }

    def "Certificate enrollment progress is reported started but state not updated if hardware replace in some specific stage when valid notifications are received"() {

        given: "that an AP node exist"
            Map<String, Object> hardwareReplaceAttribute = new HashMap<String, Object>()
            hardwareReplaceAttribute.put(NodeAttribute.IS_HARDWARE_REPLACE_NODE.toString(), true)
            final ManagedObject apNodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo, hardwareReplaceAttribute)
            statusAttributes.put(NodeStatusAttribute.STATE.toString(), state.toString())
            MoCreatorSpec.createNodeStatusMo(NODE_FDN, apNodeMo, statusAttributes)

        when: "A certificate enrollment notification is received"
            final CertificateEnrollmentStatus certificateEnrollmentStatus = newCertificateEnrollmentStatus(issuerName,
                certificateEnrollmentStatusType)
            certificateEnrollmentEventConsumer.listenToCertificateEnrollmentStatusNotifications(certificateEnrollmentStatus)

        then:
            assertStatusEntries(statusEntryManager.getAllStatusEntries(NODE_FDN), taskName, StatusEntryProgress.STARTED.toString())
            CommonAssertionsSpec.assertIntegrationState(dps, NODE_FDN, updatedNodeState.toString())

        where:
            state                       | issuerName         | certificateEnrollmentStatusType || taskName        | updatedNodeState
            INTEGRATION_STARTED         | ISSUER_NAME_IPSEC  | START                           || TASK_NAME_IPSEC | INTEGRATION_STARTED
            BIND_COMPLETED              | ISSUER_NAME_OAM    | START                           || TASK_NAME_OAM   | BIND_COMPLETED
            HARDWARE_REPLACE_SUSPENDED  | ISSUER_NAME_OAM    | START                           || TASK_NAME_OAM   | HARDWARE_REPLACE_SUSPENDED
    }

    def "Certificate enrollment progress is reported failed and state not updated if hardware replace in some specific stage when valid notifications are received"() {

        given: "that an AP node exist"
            Map<String, Object> hardwareReplaceAttribute = new HashMap<String, Object>()
            hardwareReplaceAttribute.put(NodeAttribute.IS_HARDWARE_REPLACE_NODE.toString(), true)
            final ManagedObject apNodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo, hardwareReplaceAttribute)
            statusAttributes.put(NodeStatusAttribute.STATE.toString(), state.toString())
            MoCreatorSpec.createNodeStatusMo(NODE_FDN, apNodeMo, statusAttributes)

        when: "A certificate enrollment notification is received"
            final CertificateEnrollmentStatus certificateEnrollmentStatus = newCertificateEnrollmentStatus(issuerName,
                certificateEnrollmentStatusType)
            certificateEnrollmentEventConsumer.listenToCertificateEnrollmentStatusNotifications(certificateEnrollmentStatus)

        then:
            assertStatusEntries(statusEntryManager.getAllStatusEntries(NODE_FDN), taskName, StatusEntryProgress.STARTED.toString())
            CommonAssertionsSpec.assertIntegrationState(dps, NODE_FDN, updatedNodeState.toString())

        where:
            state                       | issuerName         | certificateEnrollmentStatusType || taskName        | updatedNodeState
            INTEGRATION_STARTED         | ISSUER_NAME_IPSEC  | FAILURE                         || TASK_NAME_IPSEC | INTEGRATION_STARTED
            BIND_STARTED                | ISSUER_NAME_OAM    | FAILURE                         || TASK_NAME_OAM   | BIND_STARTED
            HARDWARE_REPLACE_SUSPENDED  | ISSUER_NAME_OAM    | FAILURE                         || TASK_NAME_OAM   | HARDWARE_REPLACE_SUSPENDED
    }

    def "Certificate enrollment progress is reported completed in AP when valid notifications are received"() {

        given: "that an AP node exist"
            final ManagedObject apNodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)
            statusAttributes.put(NodeStatusAttribute.STATE.toString(), state.toString())
            MoCreatorSpec.createNodeStatusMo(NODE_FDN, apNodeMo, statusAttributes)

        when: "A certificate enrollment notification is received"
            final CertificateEnrollmentStatus certificateEnrollmentStatus = newCertificateEnrollmentStatus(issuerName,
                certificateEnrollmentStatusType)
            certificateEnrollmentEventConsumer.listenToCertificateEnrollmentStatusNotifications(certificateEnrollmentStatus)

        then:
            assertStatusEntries(statusEntryManager.getAllStatusEntries(NODE_FDN), taskName, StatusEntryProgress.COMPLETED.toString())
            CommonAssertionsSpec.assertIntegrationState(dps, NODE_FDN, updatedNodeState.toString())

        where:
            state               | issuerName         | certificateEnrollmentStatusType || taskName         | updatedNodeState
            BIND_COMPLETED      | ISSUER_NAME_OAM    | SUCCESS                         || TASK_NAME_OAM    | INTEGRATION_STARTED
            BIND_STARTED        | ISSUER_NAME_IPSEC  | SUCCESS                         || TASK_NAME_IPSEC  | INTEGRATION_STARTED
            INTEGRATION_FAILED  | ISSUER_NAME_OAM    | SUCCESS                         || TASK_NAME_OAM    | INTEGRATION_STARTED
            ORDER_STARTED       | ISSUER_NAME_OAM    | SUCCESS                         || TASK_NAME_OAM    | ORDER_STARTED
            ORDER_STARTED       | ISSUER_NAME_IPSEC  | SUCCESS                         || TASK_NAME_IPSEC  | ORDER_STARTED
    }

    def "Certificate enrollment progress is reported completed in AP when valid notifications are received for migration node"() {

        given: "that an AP node exist"
        final ManagedObject apNodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)
        statusAttributes.put(NodeStatusAttribute.STATE.toString(), state.toString())
        MoCreatorSpec.createNodeStatusMo(NODE_FDN, apNodeMo, statusAttributes)
        apNodeMo.setAttribute(NodeAttribute.IS_NODE_MIGRATION.toString(), true)

        when: "certificate enrollment notification is received"
        final CertificateEnrollmentStatus certificateEnrollmentStatus = newCertificateEnrollmentStatus(issuerName,
                certificateEnrollmentStatusType)
        certificateEnrollmentEventConsumer.listenToCertificateEnrollmentStatusNotifications(certificateEnrollmentStatus)

        then:
        assertStatusEntries(statusEntryManager.getAllStatusEntries(NODE_FDN), taskName, StatusEntryProgress.COMPLETED.toString())
        CommonAssertionsSpec.assertIntegrationState(dps, NODE_FDN, updatedNodeState.toString())

        where:
        state                 | issuerName        | certificateEnrollmentStatusType || taskName        | updatedNodeState
        BIND_COMPLETED        | ISSUER_NAME_OAM   | SUCCESS                         || TASK_NAME_OAM   | MIGRATION_STARTED
        BIND_STARTED          | ISSUER_NAME_IPSEC | SUCCESS                         || TASK_NAME_IPSEC | MIGRATION_STARTED
        MIGRATION_FAILED      | ISSUER_NAME_OAM   | SUCCESS                         || TASK_NAME_OAM   | MIGRATION_STARTED
        PRE_MIGRATION_STARTED | ISSUER_NAME_OAM   | SUCCESS                         || TASK_NAME_OAM   | PRE_MIGRATION_STARTED
        PRE_MIGRATION_STARTED | ISSUER_NAME_IPSEC | SUCCESS                         || TASK_NAME_IPSEC | PRE_MIGRATION_STARTED
    }

    def "Certificate enrollment progress is reported failed in AP when failed notifications are received"() {

        given: "that an AP node exist"
            final ManagedObject apNodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)
            statusAttributes.put(NodeStatusAttribute.STATE.toString(), state.toString())
            MoCreatorSpec.createNodeStatusMo(NODE_FDN, apNodeMo, statusAttributes)

        when: "A certificate enrollment notification is received"
            final CertificateEnrollmentStatus certificateEnrollmentStatus = newCertificateEnrollmentStatus(issuerName,
                certificateEnrollmentStatusType)
            certificateEnrollmentEventConsumer.listenToCertificateEnrollmentStatusNotifications(certificateEnrollmentStatus)

        then:
            assertStatusEntries(statusEntryManager.getAllStatusEntries(NODE_FDN), taskName, StatusEntryProgress.FAILED.toString())
            CommonAssertionsSpec.assertIntegrationState(dps, NODE_FDN, updatedNodeState.toString())

        where:
            state               | issuerName         | certificateEnrollmentStatusType || taskName         | updatedNodeState
            BIND_STARTED        | ISSUER_NAME_OAM    | FAILURE                         || TASK_NAME_OAM    | INTEGRATION_FAILED
            ORDER_STARTED       | ISSUER_NAME_IPSEC  | FAILURE                         || TASK_NAME_IPSEC  | ORDER_STARTED
            BIND_STARTED        | ISSUER_NAME_IPSEC  | FAILURE                         || TASK_NAME_IPSEC  | INTEGRATION_FAILED
    }

    def "Certificate enrollment progress is reported failed in AP when failed notifications are received for migration node"() {

        given: "that an AP node exist"
        final ManagedObject apNodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)
        statusAttributes.put(NodeStatusAttribute.STATE.toString(), state.toString())
        MoCreatorSpec.createNodeStatusMo(NODE_FDN, apNodeMo, statusAttributes)
        apNodeMo.setAttribute(NodeAttribute.IS_NODE_MIGRATION.toString(), true)

        when: "certificate enrollment notification is received"
        final CertificateEnrollmentStatus certificateEnrollmentStatus = newCertificateEnrollmentStatus(issuerName,
                certificateEnrollmentStatusType)
        certificateEnrollmentEventConsumer.listenToCertificateEnrollmentStatusNotifications(certificateEnrollmentStatus)

        then:
        assertStatusEntries(statusEntryManager.getAllStatusEntries(NODE_FDN), taskName, StatusEntryProgress.FAILED.toString())
        CommonAssertionsSpec.assertIntegrationState(dps, NODE_FDN, updatedNodeState.toString())

        where:
        state                 | issuerName        | certificateEnrollmentStatusType || taskName        | updatedNodeState
        BIND_STARTED          | ISSUER_NAME_OAM   | FAILURE                         || TASK_NAME_OAM   | MIGRATION_FAILED
        PRE_MIGRATION_STARTED | ISSUER_NAME_IPSEC | FAILURE                         || TASK_NAME_IPSEC | PRE_MIGRATION_STARTED
        BIND_STARTED          | ISSUER_NAME_IPSEC | FAILURE                         || TASK_NAME_IPSEC | MIGRATION_FAILED
    }

    def "Certificate DEBUG enrollment progress is not reported back to AP when invalid notifications are received"() {

        given: "that an AP node exist"
            final ManagedObject apNodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)
            statusAttributes.put(NodeStatusAttribute.STATE.toString(), state.toString())
            MoCreatorSpec.createNodeStatusMo(NODE_FDN, apNodeMo, statusAttributes)

        when: "A certificate enrollment notification is received"
            final CertificateEnrollmentStatus certificateEnrollmentStatus = newCertificateEnrollmentStatus(issuerName,
                certificateEnrollmentStatusType)
            certificateEnrollmentEventConsumer.listenToCertificateEnrollmentStatusNotifications(certificateEnrollmentStatus)

        then:
            assertNoOfStatusEntries(statusEntryManager.getAllStatusEntries(NODE_FDN), 0)
            CommonAssertionsSpec.assertIntegrationState(dps, NODE_FDN, state.toString())

        where:
            state               | issuerName         | certificateEnrollmentStatusType
            BIND_STARTED        | ISSUER_NAME_OAM    | null
            BIND_STARTED        | ISSUER_NAME_OAM    | CERTIFICATE_SENT
            BIND_STARTED        | ISSUER_NAME_IPSEC  | CERTIFICATE_SENT
    }

    def "A certificate enrollment notification is ignored if the node is not managed by AP"() {

        when: "An certificate enrollment notification is received for a node that is not managed by AP"
            CertificateEnrollmentStatus certificateEnrollmentStatus = new CertificateEnrollmentStatus()
            certificateEnrollmentStatus.setNodeName("not a valid node name")
            certificateEnrollmentEventConsumer.listenToCertificateEnrollmentStatusNotifications(certificateEnrollmentStatus)

        then: "No task is reported as started, completed or failed"
            assertNoOfStatusEntries(statusEntryManager.getAllStatusEntries(NODE_FDN), 0)
    }

    def newCertificateEnrollmentStatus(issuerName, certificateEnrollmentStatusType) {
        CertificateEnrollmentStatus certificateEnrollmentStatus = new CertificateEnrollmentStatus()
        certificateEnrollmentStatus.setNodeName(NODE_NAME)
        certificateEnrollmentStatus.setIssuerName(issuerName)
        certificateEnrollmentStatus.certificateEnrollmentStatusType = certificateEnrollmentStatusType
        return certificateEnrollmentStatus
    }
}
