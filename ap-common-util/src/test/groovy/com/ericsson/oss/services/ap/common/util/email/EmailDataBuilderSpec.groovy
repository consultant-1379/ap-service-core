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
package com.ericsson.oss.services.ap.common.util.email

import javax.inject.Inject
import javax.mail.MessagingException
import javax.mail.Message.RecipientType
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

import com.ericsson.cds.cdi.support.spock.CdiSpecification

/**
 * Unit tests for {@link EmailDataBuilder}.
 */
class EmailDataBuilderSpec extends CdiSpecification  {

    @Inject
    private EmailSender emailNotification

    private MimeMessage emailDetails
    private final String sourceSubject = "WorkOrder 12312414F / Project lciadm100_apTafProject3 / Node LTE01dg2ERBS00017 / EXPANSION_COMPLETED"
    private final String sourceContent = "EXPANSION_COMPLETED"
    private final String fromEmailAddress = "ENM Auto Provisioning <enmautoprovisioning@ericsson.com>"
    private final String toEmailAddresses = "example1.expansion@ericsson.com,example2.expansion@ericsson.com"
    private final InternetAddress[] sourceToEmailAddressesList = parseEmailAddress(toEmailAddresses)

    def "Email Details are set, check parameters are correct. Throw out exception as emailrelay is unavailable in UT"() {
        given: "Email Deatils are set"
        emailDetails = new EmailDataBuilder()
                .setFromEmailAddress(fromEmailAddress)
                .setToEmailAddresses(sourceToEmailAddressesList)
                .setSubject(sourceSubject)
                .setMessageText(sourceContent)
                .build()
        when: "Processor sets email with details"
        emailNotification.send(emailDetails)
        then:
        emailDetails.getFrom().toString().equals("[" + EmailProperties.NOREPLYEMAILADDRESS + "]")
        emailDetails.getRecipients(RecipientType.BCC).toString().contains(sourceToEmailAddressesList[0].toString())
        emailDetails.getRecipients(RecipientType.BCC).toString().contains(sourceToEmailAddressesList[1].toString())
        emailDetails.getSubject().equals(sourceSubject)
        emailDetails.getContent().getBodyPart(0).getContent().toString().equals(sourceContent)
        MessagingException unknownHostException = thrown()
        unknownHostException.cause.toString().contains("java.net.UnknownHostException: emailrelay")
    }

    def "Only Email Subject is set, throw exception"() {
        given: "Email Subject is set"
        emailDetails = new EmailDataBuilder().setSubject(sourceSubject).build()
        when: "Processor set email details with Email Subject only"
        emailNotification.send(emailDetails)
        then:
        emailDetails.getSubject().equals(sourceSubject)
        MessagingException sendFailedException = thrown()
        sendFailedException.toString().contains("No recipient addresses")
    }

    def "Only Email Content is set, throw exception"() {
        given: "Email Content is set"
        emailDetails = new EmailDataBuilder().setMessageText(sourceContent).build()
        when: "Processor set email details with Email Content only"
        emailNotification.send(emailDetails)
        then:
        emailDetails.getContent().getBodyPart(0).getContent().toString().equals(sourceContent)
        MessagingException sendFailedException = thrown()
        sendFailedException.toString().contains("No recipient addresses")
    }

    def "Only Email FromAddress is set, throw exception"() {
        given: "Email FromAddress is set"
        emailDetails = new EmailDataBuilder().setFromEmailAddress(fromEmailAddress).build()
        when: "Processor set email details with Email FromEmailAddress only"
        emailNotification.send(emailDetails)
        then:
        emailDetails.getFrom().toString().equals("[" + EmailProperties.NOREPLYEMAILADDRESS + "]")
        MessagingException sendFailedException = thrown()
        sendFailedException.toString().contains("No recipient addresses")
    }

    def "Only Email ToAddress is set, throw exception"() {
        given: "Email ToAddress is set"
        emailDetails = new EmailDataBuilder().setToEmailAddresses(sourceToEmailAddressesList).build()
        when: "Processor set email details with Email ToAddress only"
        emailNotification.send(emailDetails)
        then:
        emailDetails.getRecipients(RecipientType.BCC).toString().contains(sourceToEmailAddressesList[0].toString())
        emailDetails.getRecipients(RecipientType.BCC).toString().contains(sourceToEmailAddressesList[1].toString())
        MessagingException unknownHostException = thrown()
        unknownHostException.cause.toString().contains("java.net.UnknownHostException: emailrelay")
    }

    def parseEmailAddress(final String emailAddresses){
        final String[] splitAdresses = emailAddresses.split(",");
        final InternetAddress[] toEmail = new InternetAddress[splitAdresses.length];
        for (int i = 0; i < splitAdresses.length; i++) {
            toEmail[i] = new InternetAddress(splitAdresses[i].trim());
        }
        return toEmail
    }
}
