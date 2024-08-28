
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

package com.ericsson.oss.services.ap.core.usecase.validation.eoi;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.IP_ADDRESS;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class EoiValidateUniqueIPAddressPerNodeInfoTest {


    @InjectMocks
    @Spy
    EoiValidateIpAddressUniquePerNode eoiValidateIpAddressUniquePerNode = new EoiValidateIpAddressUniquePerNode();

    private ValidationContext context;

    private static final String NODE_NAME1 = "NodeName1";
    private static final String NODE_NAME2 = "NodeName2";

    private static final Map<String, Object> networkElement = new HashMap<>();
    private static final Map<String, Object> networkElement1 = new HashMap<>();
    private static final Map<String, Object> networkElements = new HashMap<>();
    final Map<String, Map<String, Object>> validationTarget = new HashMap<>();

    private static final String MESSAGE_NODE_DUPLICATE_IP = "Duplicate IP address %s";
    private static final String DYNAMIC_IP_ADDRESS = "0.0.0.0";

    @Test
    public void whenNodeInfoIpAddressIsUniqueAndValidThenValidationIsSuccessful() {
        networkElement.put("ipAddress", IP_ADDRESS);
        final List list = Arrays.asList(networkElement);
        networkElements.put("networkelements", list);

        validationTarget.put(EoiProjectTargetKey.REQUEST_CONTENT.toString(), networkElements);
        context = new ValidationContext("Import", validationTarget);
        eoiValidateIpAddressUniquePerNode.execute(context);
        assertTrue(context.getValidationErrors().isEmpty());

    }

    @Test
    public void whenNodeInfoIpAddressAlreadyExistsInProjectThenValidationFailsWithDuplicateIpAddressMessage() {
        networkElement.put("nodeName", NODE_NAME1);
        networkElement.put("ipAddress", IP_ADDRESS);
        networkElement1.put("nodeName", NODE_NAME2);
        networkElement1.put("ipAddress", IP_ADDRESS);
        final List finalList = new ArrayList();
        finalList.add(networkElement);
        finalList.add(networkElement1);
        networkElements.put("networkelements", finalList);

        validationTarget.put(EoiProjectTargetKey.REQUEST_CONTENT.toString(), networkElements);


        context = new ValidationContext("Import", validationTarget);

        final String expectedMessage = String.format(MESSAGE_NODE_DUPLICATE_IP, IP_ADDRESS);

        final boolean isValidationSuccessful = eoiValidateIpAddressUniquePerNode.execute(context);

        assertFalse(isValidationSuccessful);
        assertEquals(String.format("%s", expectedMessage), context.getValidationErrors().get(0));
    }

    @Test
    public void whenMultipeNodesWithDynamicIpAddressThenValidationSucceeds() {
        networkElement.put("nodeName", NODE_NAME1);
        networkElement.put("ipAddress", DYNAMIC_IP_ADDRESS);
        networkElement1.put("nodeName", NODE_NAME2);
        networkElement1.put("ipAddress", DYNAMIC_IP_ADDRESS);
        final List finalList = new ArrayList();
        finalList.add(networkElement);
        finalList.add(networkElement1);
        networkElements.put("networkelements", finalList);

        validationTarget.put(EoiProjectTargetKey.REQUEST_CONTENT.toString(), networkElements);


        context = new ValidationContext("Import", validationTarget);

        eoiValidateIpAddressUniquePerNode.execute(context);

        assertTrue(context.getValidationErrors().isEmpty());
    }


}
