package com.urbanairship.push;

/**
 * Generic global storage class
 * <p> 
 * Stores apid, accessible via getApid()
 *
 * @see #getApid()
 */
public class UA {
    private static String apid;

    /**
     * @param apid
     */
    public static void setApid(String apid) {
        UA.apid = apid;
    }

    /**
     * @return  String apid
     */
    public static String getApid() {
        return apid;
    }
}
