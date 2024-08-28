/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.client.nodeplugin.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Class containing details related to the validation result.
 * The message of the validation warning or error and the suggestion of corrective action which could fix the warning or error.
 */
public class ValidationDetails implements Serializable {

    private String validationMessage;

    private String correctiveAction;

    /**
     * Constant used in hashCode method.
     */
    private static final int NUMERICAL_CONSTANT = 31;

    public String getValidationMessage() {
        return validationMessage;
    }

    public void setValidationMessage(final String validationMessage) {
        this.validationMessage = validationMessage;
    }

    public String getCorrectiveAction() {
        return correctiveAction;
    }

    public void setCorrectiveAction(final String correctiveAction) {
        this.correctiveAction = correctiveAction;
    }

    @Override
    public String toString() {
        return "ValidationDetails{" +
            "validationMessage='" + validationMessage + '\'' +
            ", correctiveAction='" + correctiveAction + '\'' +
            '}';
    }

    @Override
    public boolean equals(final Object object) {//NOSONAR
            final ValidationDetails validationDetails = (ValidationDetails)object;
            if(validationDetails!=null){
                return Objects.equals(validationDetails.getValidationMessage(), this.getValidationMessage()) &&
                    Objects.equals(validationDetails.getCorrectiveAction(), this.getCorrectiveAction());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = validationMessage != null ? validationMessage.hashCode() : 0;
        result = NUMERICAL_CONSTANT * result + (correctiveAction != null ? correctiveAction.hashCode() : 0);
        return result;
    }

}
