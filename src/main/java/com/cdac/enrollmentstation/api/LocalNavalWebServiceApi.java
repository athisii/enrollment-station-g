package com.cdac.enrollmentstation.api;

import com.cdac.enrollmentstation.dto.CRApiResDto;
import com.cdac.enrollmentstation.dto.CRReadDataResDto;
import com.cdac.enrollmentstation.dto.CRWaitForConnectResDto;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.util.Singleton;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author athisii, CDAC
 * Created on 28/03/23
 */
public class LocalNavalWebServiceApi {
    private static final Logger LOGGER = ApplicationLog.getLogger(LocalNavalWebServiceApi.class);
    public static final String ERR_MSG = "Error occurred while parsing json data.";


    //Suppress default constructor for noninstantiability
    private LocalNavalWebServiceApi() {
        throw new AssertionError("The LocalNavalWebServiceApi methods must be accessed statically.");
    }

    // throws GenericException
    // Caller must handle the exception
    public static CRApiResDto getDeInitialize() {
        HttpRequest httpRequest = HttpUtil.createGetHttpRequest(LocalNavalWebServiceApiUrl.getDeInitialize());
        HttpResponse<String> response = HttpUtil.sendHttpRequest(httpRequest);
        try {
            return Singleton.getObjectMapper().readValue(response.body(), CRApiResDto.class);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException(ERR_MSG);
        }
    }

    // throws GenericException
    // Caller must handle the exception
    public static CRApiResDto getInitialize() {
        HttpRequest httpRequest = HttpUtil.createGetHttpRequest(LocalNavalWebServiceApiUrl.getInitialize());
        HttpResponse<String> response = HttpUtil.sendHttpRequest(httpRequest);
        try {
            return Singleton.getObjectMapper().readValue(response.body(), CRApiResDto.class);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException(ERR_MSG);
        }
    }

    // throws GenericException
    // Caller must handle the exception
    public static CRWaitForConnectResDto postWaitForConnect(String data) {
        HttpRequest httpRequest = HttpUtil.createPostHttpRequest(LocalNavalWebServiceApiUrl.getWaitForConnect(), data);
        HttpResponse<String> response = HttpUtil.sendHttpRequest(httpRequest);
        try {
            return Singleton.getObjectMapper().readValue(response.body(), CRWaitForConnectResDto.class);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException(ERR_MSG);
        }
    }

    // throws GenericException
    // Caller must handle the exception
    public static CRApiResDto postSelectApp(String data) {
        HttpRequest httpRequest = HttpUtil.createPostHttpRequest(LocalNavalWebServiceApiUrl.getSelectApp(), data);
        HttpResponse<String> response = HttpUtil.sendHttpRequest(httpRequest);
        try {
            return Singleton.getObjectMapper().readValue(response.body(), CRApiResDto.class);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException(ERR_MSG);
        }
    }

    // throws GenericException
    // Caller must handle the exception
    public static CRReadDataResDto postReadData(String data) {
        HttpRequest httpRequest = HttpUtil.createPostHttpRequest(LocalNavalWebServiceApiUrl.getReadDataFromNaval(), data);
        HttpResponse<String> response = HttpUtil.sendHttpRequest(httpRequest);
        try {
            return Singleton.getObjectMapper().readValue(response.body(), CRReadDataResDto.class);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException(ERR_MSG);
        }
    }

    // throws GenericException
    // Caller must handle the exception
    public static CRApiResDto postStoreData(String data) {
        HttpRequest httpRequest = HttpUtil.createPostHttpRequest(LocalNavalWebServiceApiUrl.getStoreDataOnNaval(), data);
        HttpResponse<String> response = HttpUtil.sendHttpRequest(httpRequest);
        try {
            return Singleton.getObjectMapper().readValue(response.body(), CRApiResDto.class);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException(ERR_MSG);
        }
    }

    // throws GenericException
    // Caller must handle the exception
    public static CRApiResDto postVerifyCertificate(String data) {
        HttpRequest httpRequest = HttpUtil.createPostHttpRequest(LocalNavalWebServiceApiUrl.getVerifyCertificate(), data);
        HttpResponse<String> response = HttpUtil.sendHttpRequest(httpRequest);
        try {
            return Singleton.getObjectMapper().readValue(response.body(), CRApiResDto.class);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException(ERR_MSG);
        }
    }

    // throws GenericException
    // Caller must handle the exception
    public static CRApiResDto postPkiAuth(String data) {
        HttpRequest httpRequest = HttpUtil.createPostHttpRequest(LocalNavalWebServiceApiUrl.getPkiAuth(), data);
        HttpResponse<String> response = HttpUtil.sendHttpRequest(httpRequest);
        try {
            return Singleton.getObjectMapper().readValue(response.body(), CRApiResDto.class);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException(ERR_MSG);
        }
    }

    // throws GenericException
    // Caller must handle the exception
    public static CRApiResDto postWaitForRemoval(String data) {
        HttpRequest httpRequest = HttpUtil.createPostHttpRequest(LocalNavalWebServiceApiUrl.getWaitForRemoval(), data);
        HttpResponse<String> response = HttpUtil.sendHttpRequest(httpRequest);
        try {
            return Singleton.getObjectMapper().readValue(response.body(), CRApiResDto.class);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException(ERR_MSG);
        }
    }
}
