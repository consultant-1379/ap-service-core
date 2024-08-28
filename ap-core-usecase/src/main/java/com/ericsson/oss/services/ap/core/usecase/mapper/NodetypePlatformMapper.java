/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.ap.core.usecase.mapper;

import java.util.HashMap;
import java.util.Map;

public class NodetypePlatformMapper {
    static final Map<String, String> nodetypePlatformMap = new HashMap<>();
    private static final String ER6000 = "ER6000";
    private static final String ECIM = "ECIM";
    private static final String CPP = "CPP";
    private static final String FRONTHAUL6000 = "FRONTHAUL-6000";

    static {
        nodetypePlatformMap.put("RadioNode", ECIM);
        nodetypePlatformMap.put("Controller6610", ECIM);
        nodetypePlatformMap.put("Router6672", ER6000);
        nodetypePlatformMap.put("Router6675", ER6000);
        nodetypePlatformMap.put("Router6673", ER6000);
        nodetypePlatformMap.put("Router6674", ER6000);
        nodetypePlatformMap.put("Router6x71", ER6000);
        nodetypePlatformMap.put("Router6000-2", ER6000);
        nodetypePlatformMap.put("MSRBS_V1", ECIM);
        nodetypePlatformMap.put("ERBS", CPP);
        nodetypePlatformMap.put("RBS", CPP);
        nodetypePlatformMap.put("FRONTHAUL-6000", FRONTHAUL6000);
    }
    public static String getPlatformFromNodeType(final String nodeType) {
        return nodetypePlatformMap.get(nodeType);
    }

}
