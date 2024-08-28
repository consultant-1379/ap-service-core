/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.util.string;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MimVersionComparatorTest {

    private MimVersionComparator mimVersionComparator;

    @Before
    public void setup() {
        mimVersionComparator = new MimVersionComparator();
    }

    @Test
    public void testCompareStringsResultLessThanZero() {
        final int result = mimVersionComparator.compare("aaa", "bbb");
        assertTrue(result < 0);
    }

    @Test
    public void testCompareStringsResultGreaterThanZero() {
        final int result = mimVersionComparator.compare("bbb", "aaa");
        assertTrue(result > 0);
    }

    @Test
    public void testCompareStringsResultEqualToZero() {
        final int result = mimVersionComparator.compare("bbb", "bbb");
        assertTrue(result == 0);
    }

    @Test
    public void testCompareIntegerResultLessThanZero() {
        final int result = mimVersionComparator.compare("9", "21");
        assertTrue(result < 0);
    }

    @Test
    public void testCompareIntegerResultGreaterThanZero() {
        final int result = mimVersionComparator.compare("21", "9");
        assertTrue(result > 0);
    }

    @Test
    public void testCompareIntegerResultEqualToZero() {
        final int result = mimVersionComparator.compare("21", "21");
        assertTrue(result == 0);
    }

    @Test
    public void testCompareStringIntegerWhenIntegerResultLessThanZero() {
        final int result = mimVersionComparator.compare("abc.9", "abc.21");
        assertTrue(result < 0);
    }

    @Test
    public void testCompareStringIntegerWhenIntegerResultGreaterThanZero() {
        final int result = mimVersionComparator.compare("abc.21", "abc.9");
        assertTrue(result > 0);
    }

    @Test
    public void testCompareStringIntegerWhenIntegerResultEqualToZero() {
        final int result = mimVersionComparator.compare("abc.21", "abc.21");
        assertTrue(result == 0);
    }

    @Test
    public void testCompareStringIntegerWhenStringResultLessThanZero() {
        final int result = mimVersionComparator.compare("21.aaa", "21.bbb");
        assertTrue(result < 0);
    }

    @Test
    public void testCompareStringIntegerWhenStringResultGreaterThanZero() {
        final int result = mimVersionComparator.compare("21.bbb", "21.aaa");
        assertTrue(result > 0);
    }

    @Test
    public void testCompareStringIntegerWhenStringResultEqualToZero() {
        final int result = mimVersionComparator.compare("21.aaa", "21.aaa");
        assertTrue(result == 0);
    }

    @Test
    public void testCompareIntegerAgainstString() {
        final int result = mimVersionComparator.compare("21", "aaa");
        assertTrue(result < 0);
    }

    @Test
    public void testCompareStringsWhenFirstStringLessPartsThanSecond() {
        final int result = mimVersionComparator.compare("aaa.bbb.ccc", "aaa.bbb.ccc.ddd");
        assertTrue(result < 0);
    }

    @Test
    public void testCompareStringsWhenFirstStringMorePartsThanSecond() {
        final int result = mimVersionComparator.compare("aaa.bbb.ccc.ddd", "aaa.bbb.ccc");
        assertTrue(result > 0);
    }

    @Test
    public void testCompareResultZeroWhenMixedDotAndUnderscoreSeparatorsStrings() {
        final int result = mimVersionComparator.compare("aaa.bbb_ccc.ddd", "aaa_bbb.ccc_ddd");
        assertTrue(result == 0);
    }

    @Test
    public void testCompareResultGreaterThanZeroWhenMixedDotAndUnderscoreSeparatorsStrings() {
        final int result = mimVersionComparator.compare("aaa.bbb_ccc.243", "aaa_bbb.ccc_4");
        assertTrue(result > 0);
    }

    @Test
    public void testCompareResultLessThanZeroWhenMixedDotAndUnderscoreSeparatorsStrings() {
        final int result = mimVersionComparator.compare("aaa.bbb_9.ddd", "aaa_bbb.10_ddd");
        assertTrue(result < 0);
    }

    @Test
    public void testWhenNumber10ThenConvertsToLetterJ() {
        final String result = MimVersionComparator.convert("10.1.260");
        assertTrue(result.equalsIgnoreCase("j.1.260"));
    }

    @Test
    public void testCovertNA() {
        final String result = MimVersionComparator.convert("e.1.260");
        assertTrue(result.equalsIgnoreCase("e.1.260"));
    }
}
