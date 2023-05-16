package com.cdac.enrollmentstation.api;

import com.cdac.enrollmentstation.constant.HttpHeader;
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

    private static final int NO_OF_RETRIES = 1;
    private static final int CONNECTION_TIMEOUT_IN_SEC = 5;
    private static final int WRITE_TIMEOUT_IN_SEC = 30;
    private static final ThreadLocal<HttpClient> HTTP_CLIENT_THREAD_LOCAL;

    public enum MethodType {
        POST,
        GET;
    }

    static {
        HTTP_CLIENT_THREAD_LOCAL = ThreadLocal.withInitial(() -> HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(CONNECTION_TIMEOUT_IN_SEC)).build());
    }

    //Suppress default constructor for noninstantiability
    private HttpUtil() {
        throw new AssertionError("The HttpUtil methods must be accessed statically.");
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
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder();
            if (extraHeaders != null) {
                extraHeaders.forEach(builder::header);
            }
            if (MethodType.POST == methodType) {
                builder.POST(HttpRequest.BodyPublishers.ofString(data));
            }
            return builder.uri(URI.create(url))
                    .header(HttpHeader.CONTENT_TYPE, "application/json; utf-8")
                    .header(HttpHeader.ACCEPT, "application/json")
                    .timeout(Duration.ofSeconds(WRITE_TIMEOUT_IN_SEC))
                    .build();
        } catch (RuntimeException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException("Invalid url or ip address. Kindly try again.");
        }

    }


    /**
     * Sends Http request.
     * Caller must handle the exception.
     *
     * @param httpRequest request payload
     * @return HttpResponse<String>  or null if timeout exception occurred
     */

    public static HttpResponse<String> sendHttpRequest(HttpRequest httpRequest) {
        int noOfRetries = NO_OF_RETRIES;
        HttpResponse<String> response = null;
        while (noOfRetries > 0) {
            try {
                response = HTTP_CLIENT_THREAD_LOCAL.get().send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                if (response.statusCode() == 200) {
                    break;
                }
            } catch (IOException ignored) {
                LOGGER.log(Level.SEVERE, String.format("%s", (NO_OF_RETRIES + 1 - noOfRetries) + " Retrying connection."));
                noOfRetries--;
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (RuntimeException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage());
                throw new GenericException("Invalid url or ip address. Kindly try again.");
            }
        }
        // connection timeout - very important
        // based on null value, connection status is determined is some APIs
        if (response == null || noOfRetries == 0) {
            LOGGER.log(Level.SEVERE, "Connection timeout or http response status code is not 200.");
            return null;
        }
        return response;
    }

    public static void removeHttpClientFromThreadLocal() {
        HTTP_CLIENT_THREAD_LOCAL.remove();
    }

}
