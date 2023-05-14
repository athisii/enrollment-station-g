package com.cdac.enrollmentstation.api;

import com.cdac.enrollmentstation.dto.*;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.util.Singleton;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.cdac.enrollmentstation.constant.ApplicationConstant.GENERIC_ERR_MSG;

/**
 * @author athisii, CDAC
 * Created on 28/03/23
 */
public class LocalCardReaderApi {
    private static final Logger LOGGER = ApplicationLog.getLogger(LocalCardReaderApi.class);


    //Suppress default constructor for noninstantiability
    private LocalCardReaderApi() {
        throw new AssertionError("The LocalCardReaderApi methods must be accessed statically.");
    }

    // throws GenericException
    // Caller must handle the exception
    public static CRDeInitializeResDto getDeInitialize() {
        HttpRequest httpRequest = HttpUtil.createGetHttpRequest(LocalCardReaderApiUrl.getDeInitialize());
        HttpResponse<String> response = HttpUtil.sendHttpRequest(httpRequest);
        // api not configured properly
        if (response == null) {
            return null;
        }
        try {
            return Singleton.getObjectMapper().readValue(response.body(), CRDeInitializeResDto.class);
        } catch (JsonProcessingException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException(GENERIC_ERR_MSG);
        }
    }

    // throws GenericException
    // Caller must handle the exception
    public static CRInitializeResDto getInitialize() {
        HttpRequest httpRequest = HttpUtil.createGetHttpRequest(LocalCardReaderApiUrl.getInitialize());
        HttpResponse<String> response = HttpUtil.sendHttpRequest(httpRequest);
        // api not configured properly
        if (response == null) {
            return null;
        }
        try {
            return Singleton.getObjectMapper().readValue(response.body(), CRInitializeResDto.class);
        } catch (JsonProcessingException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException(GENERIC_ERR_MSG);
        }
    }

    // throws GenericException
    // Caller must handle the exception
    public static CRWaitForConnectResDto postWaitForConnect(String data) {
        HttpRequest httpRequest = HttpUtil.createPostHttpRequest(LocalCardReaderApiUrl.getWaitForConnect(), data);
        HttpResponse<String> response = HttpUtil.sendHttpRequest(httpRequest);
        // api not configured properly
        if (response == null) {
            return null;
        }
        try {
            return Singleton.getObjectMapper().readValue(response.body(), CRWaitForConnectResDto.class);
        } catch (JsonProcessingException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException(GENERIC_ERR_MSG);
        }
    }

    // throws GenericException
    // Caller must handle the exception
    public static CRSelectAppResDto postSelectApp(String data) {
        HttpRequest httpRequest = HttpUtil.createPostHttpRequest(LocalCardReaderApiUrl.getSelectApp(), data);
        HttpResponse<String> response = HttpUtil.sendHttpRequest(httpRequest);
        // api not configured properly
        if (response == null) {
            return null;
        }
        try {
            return Singleton.getObjectMapper().readValue(response.body(), CRSelectAppResDto.class);
        } catch (JsonProcessingException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException(GENERIC_ERR_MSG);
        }
    }

    // throws GenericException
    // Caller must handle the exception
    public static CRReadDataResDto postReadData(String data) {
        HttpRequest httpRequest = HttpUtil.createPostHttpRequest(LocalCardReaderApiUrl.getReadDataFromNaval(), data);
        HttpResponse<String> response = HttpUtil.sendHttpRequest(httpRequest);
        // api not configured properly
        if (response == null) {
            return null;
        }
        try {
            return Singleton.getObjectMapper().readValue(response.body(), CRReadDataResDto.class);
        } catch (JsonProcessingException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException(GENERIC_ERR_MSG);
        }
    }
}
