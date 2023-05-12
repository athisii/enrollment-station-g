package com.cdac.enrollmentstation.constant;/*
 * @author athisii, CDAC
 * Created on 02/12/22
 */

public class HttpHeader {
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String ACCEPT = "Accept";

    //Suppress default constructor for noninstantiability
    private HttpHeader() {
        throw new AssertionError("The HttpHeader fields must be accessed statically.");
    }
}
