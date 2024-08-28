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

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Build Email data with subject, content, fromEmailAddress, toEmailAddress and smtpHost.
 */
public class EmailDataBuilder {
    private final MimeMessage emailDetails;

    /**
     * Constructs the MimeMessage
     */
    public EmailDataBuilder() {
        final Properties emailSystemProperties = System.getProperties();
        emailSystemProperties.setProperty("mail.smtp.host", EmailProperties.SMTPHOST);
        final Session session = Session.getDefaultInstance(emailSystemProperties);
        emailDetails = new MimeMessage(session);
    }

    /**
     * Set the subject of Email
     *
     * @param subject
     *            the subject to set
     * @throws MessagingException
     */
    public EmailDataBuilder setSubject(final String subject) throws MessagingException {
        emailDetails.setSubject(subject);
        return this;
    }

    /**
     * Set the content of Email
     *
     * @param messageText
     *            the messageText to set
     * @throws MessagingException
     */
    public EmailDataBuilder setMessageText(final String messageText) throws MessagingException {
        final Multipart emailContent = new MimeMultipart();
        final MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(messageText, "text/html");
        emailContent.addBodyPart(htmlPart);
        emailDetails.setContent(emailContent);
        return this;
    }

    /**
     * Set the recipients of Email
     *
     * @param toEmailAddresses
     *            the toEmailAddresses to set
     * @throws MessagingException
     */
    public EmailDataBuilder setToEmailAddresses(final InternetAddress[] toEmailAddresses) throws MessagingException {
        emailDetails.setRecipients(Message.RecipientType.BCC, toEmailAddresses);
        return this;
    }

    /**
     * Set the from Email Address of Email
     *
     * @param fromEmailAddress
     *            the fromEmailAddress to set
     * @throws MessagingException
     */
    public EmailDataBuilder setFromEmailAddress(final String fromEmailAddress) throws MessagingException {
        emailDetails.setFrom(new InternetAddress(fromEmailAddress));
        return this;
    }

    /**
     * Builds the email MimeMessage object.
     *
     * @return the email MimeMessage object
     */
    public MimeMessage build() {
        return emailDetails;
    }

}
