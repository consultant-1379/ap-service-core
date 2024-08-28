/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.view;

import java.util.Collections;
import java.util.List;

import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.common.model.ProjectAttribute;
import com.ericsson.oss.services.ap.common.util.string.FDN;

/**
 * Sorting utility methods for {@link MoData} RDN values.
 */
public final class MoDataComparator {

    private MoDataComparator() {

    }

    /**
     * Sort a list of {@link MoData} so that they are in alphabetical order by name (i.e RDN value).
     *
     * @param moDatasToBeSorted
     *            the list to be sorted
     */
    public static void sortByName(final List<MoData> moDatasToBeSorted) {
        Collections.sort(moDatasToBeSorted, (final MoData firstMoData, final MoData secondMoData) -> {
            final String moName1 = FDN.get(firstMoData.getFdn()).getRdnValue();
            final String moName2 = FDN.get(secondMoData.getFdn()).getRdnValue();
            return moName1.compareTo(moName2);
        });
    }

    /**
     * Sort a list of {@link MoData} by attribute.
     *
     * @param moDatas
     *            the list to be sorted
     * @param attribute
     *            the attribute that the list will be sorted by
     * @param <T>
     *            the comparable object class
     *
     */
    public static <T extends Comparable> void sortByAttribute(final List<MoData> moDatas, final ProjectAttribute attribute) {
        Collections.sort(moDatas, (final MoData moData1, final MoData moData2) -> {
            final T moData1Attribute = (T) moData1.getAttributes().get(attribute.toString());
            final T moData2Attribute = (T) moData2.getAttributes().get(attribute.toString());
            return moData1Attribute.compareTo(moData2Attribute);
        });
    }
}
