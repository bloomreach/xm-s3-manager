package com.bloomreach.xm.manager.common.util;

public class Util {

    public static String getLastBitFromUrl(final String url){
        return url.replaceFirst(".*/([^/?]+).*", "$1").replace("/", "");
    }

}
