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
package com.ericsson.oss.services.ap.common.util.email;

import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

/**
 * This class provides a common email sender function.
 */
public final class EmailSender {

    /**
     * Implement send email function
     *
     * @param message
     *            the MimeMessage of Email
     * @throws MessagingException
     */
    public void send(final MimeMessage message) throws MessagingException {
        Transport.send(message);
    }

}
