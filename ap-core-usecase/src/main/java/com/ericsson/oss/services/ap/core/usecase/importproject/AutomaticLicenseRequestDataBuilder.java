/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.importproject;

import java.util.List;
import java.util.function.Consumer;

import com.ericsson.oss.services.ap.core.usecase.view.AutomaticLicenseRequestData;

/**
 * Class used to build an automaticLicenseRequest data object.
 * The automaticLicenseRequest data object contains values used for generation of automatic licenses.
 */

public class AutomaticLicenseRequestDataBuilder {

    protected String groupId;
    protected String hardwareType;
    protected List<String> radioAccessTechnologies;
    protected String softwareLicenseTargetId;

    private AutomaticLicenseRequestDataBuilder(){
    }

    /**
     * Creates a new instance of this builder.
     *
     * @return AutomaticLicenseRequestDataBuilder
     */
    public static AutomaticLicenseRequestDataBuilder newBuilder() {
        return new AutomaticLicenseRequestDataBuilder();
    }

    /**
     * Lambda expression to assign all the {@link AutomaticLicenseRequestData}
     *
     * @param builderConsumer
     *             Consumer for builder
     * @return {@link AutomaticLicenseRequestDataBuilder}
     *             A builder object of type {@link AutomaticLicenseRequestData}.
     */
    public AutomaticLicenseRequestDataBuilder with(
        Consumer<AutomaticLicenseRequestDataBuilder> builderConsumer) {
        builderConsumer.accept(this);
        return this;
    }

    /**
     * Builds and returns the AutomaticLicenseRequestData populated using method "with".
     *
     * @return AutomaticLicenseRequestData
     */
    public AutomaticLicenseRequestData build() {
        return new AutomaticLicenseRequestData()
            .setGroupId(groupId)
            .setHardwareType(hardwareType)
            .setRadioAccessTechnologies(radioAccessTechnologies)
            .setSoftwareLicenseTargetId(softwareLicenseTargetId);
    }
}
