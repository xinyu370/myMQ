package org.example;

import java.util.regex.Pattern;

public class CheckUtil {
    private static final String IPv4_PATTERN =
            "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    private static final String IPv6_PATTERN =
            "^[0-9a-fA-F]{1,4}:" +
                    "([0-9a-fA-F]{1,4}:){1,6}" +
                    "([0-9a-fA-F]{1,4})$";

    private static final Pattern IPv4_REGEX = Pattern.compile(IPv4_PATTERN);
    private static final Pattern IPv6_REGEX = Pattern.compile(IPv6_PATTERN);

    public static String checkAdd(String ipAddress){
        if (ipAddress == null) {
           throw new RuntimeException("ip地址非法");
        }
        // 检查是否为IPv4地址
        if (IPv4_REGEX.matcher(ipAddress).matches()) {
            return ipAddress;
        }
        // 检查是否为IPv6地址
        if (IPv6_REGEX.matcher(ipAddress).matches()) {
            return ipAddress;
        }
        // 既不是IPv4也不是IPv6
        throw new RuntimeException("ip地址非法");
    }
}
