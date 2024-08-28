/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.eoi.templateprocessor;

import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import org.apache.commons.io.IOUtils;


import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class EoiFmVipTemplateProcessor {

    private static final String FM_VIP_TEMPLATE = "/templates/generatedEoiJson/EoiFmVipTemplate.json";
    private static final String EOI_FM_VIP_CONFIGURATION_TAG = "\"%EOI_FM_VIP_CONFIGURATION%\"";
    private static final String EOI_FM_IPADDRESS_TAG = "%EOI_FM_IPADDRESS%";


    public String processTemplate(String jsonContent, final String fmVipAddress) {

        try (final InputStream initialTemplate = getClass().getResourceAsStream(FM_VIP_TEMPLATE)) {
            String generatedDay0Template = convertInputStreamToString(initialTemplate);
            String updatedLdapTemplate = updateTemplate(generatedDay0Template, fmVipAddress);
            return jsonContent.replace(EOI_FM_VIP_CONFIGURATION_TAG, updatedLdapTemplate);

        } catch (final Exception e) {
            throw new ApApplicationException(e.getMessage(), e);
        }
    }

    private String updateTemplate(final String rawTemplate, final String fmVipAddress)
    {
        return rawTemplate.replace(EOI_FM_IPADDRESS_TAG, fmVipAddress);
    }


    private static String convertInputStreamToString(final InputStream input) throws IOException {
        return IOUtils.toString(input, StandardCharsets.UTF_8);
    }
}
