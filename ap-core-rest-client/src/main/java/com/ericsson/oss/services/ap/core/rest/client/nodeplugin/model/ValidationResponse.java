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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Class containing response of the validation result.
 * The status can be SUCCESS, FAILED or WARNING
 * The validationResults is a list of validation result data for each configuration.
 * The message contains additional description for validation status.
 */
public class ValidationResponse implements Serializable {

    private static final String VALIDATION_RESPONSE = "ValidationResponse{";

    private static final String CONSTANT_MESSAGE = "message=";

    private static final String VALIDATION_RESULTS = ", validationResults=";

    private static final String CONSTANT_STATUS = ", status=";

    private static final String CLOSING_BRACKET = "}";

    /**
     * Constant used in hashCode method.
     */
    private static final int NUMERICAL_CONSTANT = 31;

    private String message;

    private List<ValidationResults> validationResults = new ArrayList<>();

    private String status;

    private int statusCode;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(final int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public List<ValidationResults> getValidationResults() {
        return validationResults;
    }

    public void setValidationResults(final List<ValidationResults> validationResults) {
        this.validationResults = validationResults;
    }

    public String getStatus() { return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return VALIDATION_RESPONSE +
            CONSTANT_MESSAGE + message + '\'' +
            VALIDATION_RESULTS + validationResults +
            CONSTANT_STATUS + status + '\'' +
            CLOSING_BRACKET;
    }

    @Override
    public int hashCode() {
        int result = message != null ? message.hashCode() : 0;
        result = NUMERICAL_CONSTANT * result + (validationResults != null ? validationResults.hashCode() : 0);
        result = NUMERICAL_CONSTANT * result + (status != null ? status.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(final Object object) { //NOSONAR
            final ValidationResponse validationResponse = (ValidationResponse)object;
            if(validationResponse!=null)
            {
                for (ValidationResults validationResult : validationResponse.getValidationResults()) {
                    if (!this.getValidationResults().contains(validationResult)) {
                        return false;
                    }
                }
                return Objects.equals(validationResponse.getStatus(), this.getStatus()) &&
                    Objects.equals(validationResponse.getMessage(), this.getMessage());
            }
        return false;
    }
}
