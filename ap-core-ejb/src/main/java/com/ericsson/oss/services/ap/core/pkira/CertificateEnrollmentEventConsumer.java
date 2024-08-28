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

import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.sdk.eventbus.model.annotation.Modeled;
import com.ericsson.oss.itpf.security.pki.ra.model.events.CertificateEnrollmentStatus;

/**
 * This class consumes messages sent from Security (PKI-RA component) informing of the status of Certificate Enrollment (IPsec and OAM). The node's AP
 * status will be updated to report the certificate enrollment progress.
 */
@Singleton
@LocalBean
public class CertificateEnrollmentEventConsumer {

    private static final String CERTIFICATE_ENROLLMENT_EVENT_URN = "//global/ClusteredCertificateEnrollmentStatusTopic/1.0.0";

    @Inject
    private Logger logger;

    @Inject
    private CertificateEnrollmentEventProcessor certificateEnrollmentEventProcessor;

    /**
     * Consumes a notification from PKI-RA and updates the node's AP status to reflect the certificate enrollment status.
     *
     * @param certificateEnrollmentStatus
     *            notification from PKI RA that contains a serial number identifying the node.
     */
    public void listenToCertificateEnrollmentStatusNotifications(@Observes @Modeled(eventUrn = CERTIFICATE_ENROLLMENT_EVENT_URN) final CertificateEnrollmentStatus certificateEnrollmentStatus) {
        try {
            certificateEnrollmentEventProcessor.processNotification(certificateEnrollmentStatus);
        } catch (final Exception e) {
            logger.warn("Error processing notification -> {}", certificateEnrollmentStatus, e);
        }
    }
}
