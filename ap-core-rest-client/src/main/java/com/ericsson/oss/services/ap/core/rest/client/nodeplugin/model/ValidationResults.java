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
 * Class containing the validation results. It contains:
 * The result of validation: SUCCESS, WARNING or FAILED
 * A list of validation details.
 * The name of configuration.
 */
public class ValidationResults implements Serializable {

    private String result;

    private List<ValidationDetails> validationDetails = new ArrayList<>();

    private String configurationName;

    /**
     * Constant used in hashCode method.
     */
    private static final int NUMERICAL_CONSTANT = 31;

    private static final String VALIDATION_RESPONSE = "ValidationResponse{";

    private static final String CONSTANT_RESULT = "result= ";

    private static final String VALIDATION_DETAILS = ", validationDetails= ";

    private static final String CONFIGURATION_NAME = ", configurationName= ";

    private static final String CLOSING_BRACKET = "}";

    public String getResult() {
        return result;
    }

    public void setResult(final String result) {
        this.result = result;
    }

    public List<ValidationDetails> getValidationDetails() {
        return validationDetails;
    }

    public void setValidationDetails(final List<ValidationDetails> validationDetails) {
        this.validationDetails = validationDetails;
    }

    public String getConfigurationName() {
        return configurationName;
    }

    public void setConfigurationName(final String configurationName) {
        this.configurationName = configurationName;
    }

    @Override
    public String toString() {
        return VALIDATION_RESPONSE +
            CONSTANT_RESULT + result + '\'' +
            VALIDATION_DETAILS + validationDetails +
            CONFIGURATION_NAME + configurationName + '\'' +
            CLOSING_BRACKET;
    }

    @Override
    public boolean equals(final Object object) {
        if (ValidationResults.class.isInstance(object)) {
            final ValidationResults validationResults = (ValidationResults)object;
            if(validationResults!=null)
            {
                for (ValidationDetails validationDetail : validationResults.getValidationDetails()) {
                    if (!this.getValidationDetails().contains(validationDetail)) {
                        return false;
                    }
                }
            }
            return Objects.equals(validationResults.getConfigurationName(), this.getConfigurationName()) && //NOSONAR
                    Objects.equals(validationResults.getResult(), this.getResult());

        }
        return false;
    }

    @Override
    public int hashCode() {
        int result1 = result != null ? result.hashCode() : 0;
        result1 = NUMERICAL_CONSTANT * result1 + (validationDetails != null ? validationDetails.hashCode() : 0);
        result1 = NUMERICAL_CONSTANT * result1 + (configurationName != null ? configurationName.hashCode() : 0);
        return result1;
    }
}
