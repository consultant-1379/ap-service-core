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
package com.ericsson.oss.services.ap.api.model.eoi;

public class SnmpConfigurationRequest extends SnmpAuthNoPrivConfigurationRequest {


    private String privPassword;
    private String privAlgo;




    public String getPrivPassword() {
        return privPassword;
    }

    public void setPrivPassword(String privPassword) {
        this.privPassword = privPassword;
    }



    public String getPrivAlgo() {
        return privAlgo;
    }

    public void setPrivAlgo(String privAlgo) {
        this.privAlgo = privAlgo;
    }

    @Override
    public String toString() {
        return "SnmpRequestDTO{" + super.toString()+
                ", privAlgo='" + privAlgo + '\'' +
                '}';
    }

}
