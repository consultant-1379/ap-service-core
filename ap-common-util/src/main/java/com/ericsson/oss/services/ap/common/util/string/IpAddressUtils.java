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

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for operations on IP address strings.
 */
public final class IpAddressUtils {

    private static final Logger logger = LoggerFactory.getLogger(IpAddressUtils.class);

    private IpAddressUtils() {

    }

    /**
     * Verifies whether the supplied IP address is of type IPv4.
     *
     * @param ipAddress
     *            the IP address to check
     * @return true if the IP address is of type IPv4
     */
    public static boolean isIpv4Address(final String ipAddress) {
        final InetAddress address = getInetAddress(ipAddress);
        return address != null && !(address instanceof Inet6Address);
    }

    /**
     * Verifies whether the supplied IP address is of type IPv6.
     *
     * @param ipAddress
     *            the IP address to check
     * @return true if the IP address is of type IPv6
     */
    public static boolean isIpv6Address(final String ipAddress) {
        final InetAddress address = getInetAddress(ipAddress);
        return address instanceof Inet6Address;
    }

    /**
     * Verifies whether the supplied IP address is of type IPv4 or IPv6.
     *
     * @param ipAddress
     *            the IP address to check
     * @return true if the IP address is of type IPv4 or IPv6
     */
    public static boolean isValidIpAddress(final String ipAddress) {
        final InetAddress address = getInetAddress(ipAddress);
        return address != null;
    }

    /**
     * Compresses the supplied IPv6 IP address into its normalised form.
     * <p>
     * The IP address <code>2001:0db8:0a0b:12f0:0000:0000:0000:0001</code> will be compressed to <code>2001:db8:a0b:12f0:0:0:0:1</code>.
     * <p>
     * <b>NOTE:</b> If the supplied IP address is not of type IPv6, it will be returned with no change.
     *
     * @param ipAddress
     *            the IP address to compress
     * @return the compressed IPv6 IP address, or the input address if not a valid IPv6 address
     */
    public static String compressIpv6Address(final String ipAddress) {
        final InetAddress address = getInetAddress(ipAddress);

        if (!(address instanceof Inet6Address)) {
            return ipAddress;
        }

        return ((Inet6Address) address).getHostAddress();
    }

    private static InetAddress getInetAddress(final String ipAddress) {
        if (StringUtils.isBlank(ipAddress)) {
            return null;
        }

        try {
            return InetAddress.getByName(ipAddress);
        } catch (final UnknownHostException e) {
            logger.debug("Invalid host for IP address [{}]: {}", ipAddress, e.getMessage(), e);
            return null;
        }
    }
}
