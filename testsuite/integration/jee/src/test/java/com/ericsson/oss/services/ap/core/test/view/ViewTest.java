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
package com.ericsson.oss.services.ap.core.test.view;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.core.test.ServiceCoreTest;
import com.ericsson.oss.services.ap.core.test.steps.ViewTestSteps;

import cucumber.api.java.en.Then;

public abstract class ViewTest extends ServiceCoreTest {

    @Inject
    protected ViewTestSteps viewSteps;

    private List<MoData> returnedData;
    private List<ManagedObject> currentData;

    private final String nameProperty;
    private final String[] otherProperties;

    public ViewTest(final String nameProperty, final String... otherProperties) {
        this.nameProperty = nameProperty;
        this.otherProperties = otherProperties;
    }

    @Then("^all returned data should match$")
    public void validate_returned_data() {
        for (int i = 0; i < returnedData.size(); i++) {
            assertThat(returnedData.get(i).getAttribute(nameProperty))
                .as("Current viewed object's name")
                .isEqualTo(currentData.get(i).getName());

            for (final String propertyName : otherProperties) {
                assertThat(returnedData.get(i).getAttributes())
                    .as("Current viewed object's " + propertyName)
                    .containsEntry(
                        propertyName,
                        currentData.get(i).getAttribute(propertyName));
            }
        }
    }

    protected MoData withReturnedData(final MoData returnedData) {
        this.returnedData = Arrays.asList(returnedData);
        return returnedData;
    }

    protected List<MoData> withReturnedData(final List<MoData> returnedData) {
        this.returnedData = returnedData;
        return returnedData;
    }

    protected List<MoData> getReturnedData() {
        return returnedData;
    }

    protected ManagedObject withCurrentData(final ManagedObject currentData) {
        this.currentData = Arrays.asList(currentData);
        return currentData;
    }

    protected List<ManagedObject> withCurrentData(final List<ManagedObject> currentData) {
        this.currentData = currentData;
        return currentData;
    }
}
