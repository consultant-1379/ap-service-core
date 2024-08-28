/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.util.string;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for {@link IpAddressUtils}.
 */
public class IpAddressUtilsTest {

    private static final String INVALID_IP_ADDRESS = "invalidIpAddress";
    private static final String IPV4_ADDRESS = "192.168.1.100";
    private static final String IPV6_ADDRESS_EXPANDED = "2001:0db8:0a0b:12f0:0000:0000:0000:0001";
    private static final String IPV6_ADDRESS_NORMALIZED = "2001:db8:a0b:12f0:0:0:0:1";

    @Test
    public void whenCheckingIpv4AndAddressIsIpv4ThenReturnTrue() {
        final boolean result = IpAddressUtils.isIpv4Address(IPV4_ADDRESS);
        assertTrue(result);
    }

    @Test
    public void whenCheckingIpv4AndAddressIsIpv6ThenReturnFalse() {
        final boolean result = IpAddressUtils.isIpv4Address(IPV6_ADDRESS_EXPANDED);
        assertFalse(result);
    }

    @Test
    public void whenCheckingIpv4AndAddressIsNullThenReturnFalse() {
        final boolean result = IpAddressUtils.isIpv4Address(null);
        assertFalse(result);
    }

    @Test
    public void whenCheckingIpv6AndAddressIsIpv4ThenReturnFalse() {
        final boolean result = IpAddressUtils.isIpv6Address(IPV4_ADDRESS);
        assertFalse(result);
    }

    @Test
    public void whenCheckingIpv6AndAddressIsExpandedIpv6ThenReturnTrue() {
        final boolean result = IpAddressUtils.isIpv6Address(IPV6_ADDRESS_EXPANDED);
        assertTrue(result);
    }

    @Test
    public void whenCheckingIpv6AndAddressIsCompressedIpv6ThenReturnTrue() {
        final boolean result = IpAddressUtils.isIpv6Address(IPV6_ADDRESS_NORMALIZED);
        assertTrue(result);
    }

    @Test
    public void whenCheckingIpv6AndAddressIsNullThenReturnFalse() {
        final boolean result = IpAddressUtils.isIpv6Address(null);
        assertFalse(result);
    }

    @Test
    public void whenCompressingIpv6AndAddressIsExpandedIpv6ThenReturnNormalisedAddress() {
        final String result = IpAddressUtils.compressIpv6Address(IPV6_ADDRESS_EXPANDED);
        assertEquals(IPV6_ADDRESS_NORMALIZED, result);
    }

    @Test
    public void whenCompressingIpv6AndAddressStartsWithDoubleColonThenReturnNormalisedAddress() {
        final String result = IpAddressUtils.compressIpv6Address("::0db8:0a0b:12f0:a000:b000");
        assertEquals("0:0:0:db8:a0b:12f0:a000:b000", result);
    }

    @Test
    public void whenCompressingIpv6AndAddressContainsDoubleColonThenReturnNormalisedAddress() {
        final String result = IpAddressUtils.compressIpv6Address("2001:0db8:0a0b:12f0::0001");
        assertEquals("2001:db8:a0b:12f0:0:0:0:1", result);
    }

    @Test
    public void whenCompressingIpv6AndAddressEndsWithDoubleColonThenReturnNormalisedAddress() {
        final String result = IpAddressUtils.compressIpv6Address("2001:0db8:0a0b:12f0::");
        assertEquals("2001:db8:a0b:12f0:0:0:0:0", result);
    }

    @Test
    public void whenCompressingIpv6AndAddressIsIpv4ThenReturnInputAddress() {
        final String result = IpAddressUtils.compressIpv6Address(IPV4_ADDRESS);
        assertEquals(IPV4_ADDRESS, result);
    }

    @Test
    public void whenCheckingValidIpAddressAndAddressIsIpv4ThenReturnTrue() {
        final boolean result = IpAddressUtils.isValidIpAddress(IPV4_ADDRESS);
        assertTrue(result);
    }

    @Test
    public void whenCheckingValidIpAddressAndAddressIsIpv6ThenReturnTrue() {
        final boolean result = IpAddressUtils.isValidIpAddress(IPV6_ADDRESS_EXPANDED);
        assertTrue(result);
    }

    @Test
    public void whenCheckingValidIpAddressAndAddressIsNotValidThenReturnFalse() {
        final boolean result = IpAddressUtils.isValidIpAddress(INVALID_IP_ADDRESS);
        assertFalse(result);
    }

    @Test
    public void whenCheckingValidIpAddressAndAddressIsNullThenReturnFalse() {
        final boolean result = IpAddressUtils.isValidIpAddress(null);
        assertFalse(result);
    }
}
