package com.bloomreach.xm.manager.util;

public class Util {

    public static String getLastBitFromUrl(final String url){
        return url.replaceFirst(".*/([^/?]+).*", "$1").replace("/", "");
    }

}
