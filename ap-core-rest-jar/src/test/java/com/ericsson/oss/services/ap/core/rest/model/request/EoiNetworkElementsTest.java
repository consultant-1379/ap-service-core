package com.ericsson.oss.services.ap.core.rest.model.request;

import java.util.Map;

import org.junit.Test;

import com.ericsson.oss.services.ap.api.model.eoi.ProjectRequestAttributes;
import com.ericsson.oss.services.ap.core.rest.model.request.EoiNetworkElement.Supervision;

public class EoiNetworkElementsTest {

    @Test
    public void testEoiNetworkElementsData() {

        String enabled = "enabled";

        final EoiNetworkElement eoiNetworkElements = new EoiNetworkElement();

        eoiNetworkElements.setCnfType("Shared-CNF");
        eoiNetworkElements.setIpAddress("10.252.163.11");
        eoiNetworkElements.setModelVersion("23.Q1-R68A25");
        eoiNetworkElements.setNeType("Shared-CNF");
        eoiNetworkElements.setModelVersion("23.Q1-R68A25");
        eoiNetworkElements.setNodeName("5G131vCUCPRI586");
        eoiNetworkElements.setOssPrefix("SubNetwork=AutoProvisioning");
        eoiNetworkElements.setPassword("netsim");
        eoiNetworkElements.setSubjectAltName("");
        final Supervision supervision = eoiNetworkElements.new Supervision();
        supervision.setCm(true);
        supervision.setFm(true);
        supervision.setPm(true);
        eoiNetworkElements.setSupervision(supervision);
        eoiNetworkElements.setTimezone("Europe/Dublin");
        eoiNetworkElements.setUserName("netsim");

        Map<String, Object> eoiNetworkElementsDataMap = EoiNetworkElement.toMap(eoiNetworkElements);
        assert eoiNetworkElementsDataMap != null;

        assert eoiNetworkElementsDataMap.get(ProjectRequestAttributes.NODE_NAME.toString()) == eoiNetworkElements.getNodeName();
        assert eoiNetworkElementsDataMap.get(ProjectRequestAttributes.NODE_TYPE.toString()) == eoiNetworkElements.getNeType();
        assert eoiNetworkElementsDataMap.get(ProjectRequestAttributes.CNF_TYPE.toString()) == eoiNetworkElements.getCnfType();
        assert eoiNetworkElementsDataMap.get(ProjectRequestAttributes.IPADDRESS.toString()) == eoiNetworkElements.getIpAddress();
        assert eoiNetworkElementsDataMap.get(ProjectRequestAttributes.TIME_ZONE.toString()) == eoiNetworkElements.getTimezone();
        assert eoiNetworkElementsDataMap.get(ProjectRequestAttributes.OSS_PREFIX.toString()) == eoiNetworkElements.getOssPrefix();
        assert eoiNetworkElementsDataMap.get(ProjectRequestAttributes.NODE_IDENTIFIER.toString()) == eoiNetworkElements.getModelVersion();
        assert eoiNetworkElementsDataMap.get(ProjectRequestAttributes.USER_NAME.toString()) == eoiNetworkElements.getUserName();
        assert eoiNetworkElementsDataMap.get(ProjectRequestAttributes.PASSWORD.toString()) == eoiNetworkElements.getPassword();
        assert eoiNetworkElementsDataMap.get(ProjectRequestAttributes.SUBJECT_ALT_NAME.toString()) == eoiNetworkElements.getSubjectAltName();
        final Map<String, Object> supervisionAttributes = (Map<String, Object>) eoiNetworkElementsDataMap.get(ProjectRequestAttributes.SUPERVISION_ATTRIBUTES.toString());
        assert supervisionAttributes.get("cm") == enabled;
        assert supervisionAttributes.get("pm") == enabled;
        assert supervisionAttributes.get("fm") == enabled;
        assert eoiNetworkElements.toString().equals(
            "EoiNetworkElement{nodeName='5G131vCUCPRI586', ipAddress='10.252.163.11', neType='Shared-CNF', userName='netsim', password='netsim', ossPrefix='SubNetwork=AutoProvisioning', modelVersion='23.Q1-R68A25', timezone='Europe/Dublin', subjectAltName='', supervision=Supervision [pm=true, fm=true, cm=true], cnfType='Shared-CNF'}");

    }
}
