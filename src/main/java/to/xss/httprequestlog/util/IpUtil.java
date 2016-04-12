/**
 * Copyright (C) 2016, Dmitry Holodov. All rights reserved.
 */
package to.xss.httprequestlog.util;

import com.google.common.net.InetAddresses;

import java.net.InetAddress;

/**
 * Created by dh on 2016/3/21.
 */
public class IpUtil {
    public static String ipToHostName(String ipString) {
        return ipToHostName(ipFromString(ipString));
    }

    public static String ipToHostName(InetAddress ip) {
        String hostName = null;
        if (ip != null) {
            hostName = ip.getCanonicalHostName();
            if (hostName.equals(ip.getHostAddress())) {
                hostName = null;
            }
        }
        return hostName;
    }

    //
    //  Converts a string to an IP address.  If null or an invalid string is
    //  passed, null is retuned.
    //
    public static InetAddress ipFromString(String ipString) {
        InetAddress ip = null;
        if (ipString != null) {
            try {
                ip = InetAddresses.forString(ipString);
            } catch (IllegalArgumentException ex) {
                //log.debug("IpUtil.ipFromString passed invalid ip: {}", ipString);
            }
        }
        return ip;
    }
}
