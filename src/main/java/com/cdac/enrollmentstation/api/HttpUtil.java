package com.cdac.enrollmentstation.api;

import com.cdac.enrollmentstation.constant.HttpHeader;
import com.cdac.enrollmentstation.exception.ConnectionTimeoutException;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author athisii, CDAC
 * Created on 29/03/23
 */
public class HttpUtil {
    private static final Logger LOGGER = ApplicationLog.getLogger(HttpUtil.class);

    private static final int CONNECTION_TIMEOUT_IN_SEC = 5;
    private static final int WRITE_TIMEOUT_IN_SEC = 60;
    private static HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(CONNECTION_TIMEOUT_IN_SEC)).build();

    public enum MethodType {
        POST, GET;
    }

    //Suppress default constructor for noninstantiability
    private HttpUtil() {
        throw new AssertionError("The HttpUtil methods must be accessed statically.");
    }

    public static void buildNewHttpClient() {
        httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(CONNECTION_TIMEOUT_IN_SEC)).build();
    }

    public static HttpRequest createGetHttpRequest(String url) {
        return createHttpRequest(MethodType.GET, url, null, null);
    }

    public static HttpRequest createPostHttpRequest(String url, String data) {
        return createPostHttpRequest(url, data, null);
    }

    public static HttpRequest createPostHttpRequest(String url, String data, Map<String, String> extraHeaders) {
        return createHttpRequest(MethodType.POST, url, data, extraHeaders);

    }

    public static HttpRequest createHttpRequest(MethodType methodType, String url, String data, Map<String, String> extraHeaders) {
        LOGGER.log(Level.INFO, () -> "*** Method: " + methodType.name() + "\n\t *** url: " + url);
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder();
            if (extraHeaders != null) {
                extraHeaders.forEach(builder::header);
            }
            if (MethodType.POST == methodType) {
                builder.POST(HttpRequest.BodyPublishers.ofString(data));
            }
            return builder.uri(URI.create(url)).header(HttpHeader.CONTENT_TYPE, "application/json; utf-8").header(HttpHeader.ACCEPT, "application/json").timeout(Duration.ofSeconds(WRITE_TIMEOUT_IN_SEC)).build();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException("Invalid url or ip address. Kindly try again.");
        }

    }


    /**
     * Sends Http request.
     * Caller must handle the exception.
     *
     * @param httpRequest request payload
     * @return HttpResponse<String>
     * @throws ConnectionTimeoutException - on timeout or response status code not 200
     * @throws GenericException           - on Exception
     */

    public static HttpResponse<String> sendHttpRequest(HttpRequest httpRequest) {
        HttpResponse<String> response = null;
        try {
            response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, () -> "sendHttpRequestError: " + ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, () -> "sendHttpRequestError: " + ex.getMessage());
            throw new GenericException("Invalid url or ip address. Kindly try again.");
        }
        if (response == null || response.statusCode() != 200) {
            if (response != null) {
                int statusCodeFinal = response.statusCode();
                LOGGER.log(Level.SEVERE, () -> "**Status Code: " + statusCodeFinal);
            } else {
                LOGGER.log(Level.SEVERE, "**Connection timeout.");
            }
            throw new ConnectionTimeoutException("Connection timeout or http response status code is not 200.");
        }
        return response;
    }
}
