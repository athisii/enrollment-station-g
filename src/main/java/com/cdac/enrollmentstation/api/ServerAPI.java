package com.cdac.enrollmentstation.api;


import com.cdac.enrollmentstation.constant.HttpHeader;
import com.cdac.enrollmentstation.dto.ARCNoReqDto;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLogNew;
import com.cdac.enrollmentstation.model.ARCDetails;
import com.cdac.enrollmentstation.util.Singleton;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.net.http.HttpRequest.BodyPublishers;
import static java.net.http.HttpResponse.BodyHandlers;

public class ServerAPI {
    private static final Logger LOGGER = ApplicationLogNew.getLogger(ServerAPI.class);
    private static final HttpClient httpClient;

    static {
        httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    //disables instantiation of this class.
    private ServerAPI() {
    }

    /**
     * Fetches single ARCDetails based on ARC unique number.
     * Caller must handle exceptions
     *
     * @param url   url of the API.
     * @param arcNo unique id whose details are to be fetched
     * @return ARCDetails
     * @throws IOException if connection timeout or socket timeout or io read/write error occurs
     */

    public static ARCDetails fetchARCDetails(String url, String arcNo) throws IOException {
        int noOfRetries = 3;
        var requestDto = new ARCNoReqDto(arcNo);
        var jsonRequest = Singleton.getObjectMapper().writeValueAsString(requestDto); // converts to JSON string.
        var postRequest = HttpRequest.newBuilder().uri(URI.create(url)).POST(BodyPublishers.ofString(jsonRequest)).header(HttpHeader.CONTENT_TYPE, "application/json; utf-8").header(HttpHeader.ACCEPT, "application/json").timeout(Duration.ofSeconds(20)).build();

        HttpResponse<String> postResponse = null;
        // tries 10secs connection timeout for 3 times if not connected
        while (noOfRetries > 0) {
            try {
                postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
                if (postResponse.statusCode() == 200) {
                    break;
                }
            } catch (HttpConnectTimeoutException connectTimeoutException) {
                LOGGER.log(Level.INFO, String.format("%s", (4 - noOfRetries) + " Retrying connection."));
                noOfRetries--;
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        // check value of noOfRetries and throw exception, so that UI can be updated accordingly
        if (noOfRetries <= 0) {
            throw new HttpConnectTimeoutException("Connection timeout. Failed to connect to server.");
        }

        // if this line is reached, response received with status code 200
        ARCDetails arcDetail = Singleton.getObjectMapper().readValue(postResponse.body(), ARCDetails.class);

        // check if errorCode is set. If yes, throw exception, so that UI can be updated accordingly
        if (Integer.parseInt(arcDetail.getErrorCode()) != 0) {
            throw new GenericException("Error in retrieving details. Please try again");
        }

        //successfully fetched
        return arcDetail;
    }


    //Test Code
    public static void main(String[] args) {
        String uiMessage = ""; // text to be displayed on UI if error occurs
        ARCDetails arcDetail = null;
        try {
            arcDetail = fetchARCDetails("http://localhost:8080/arc-details", "1111-AAAA");
            LOGGER.log(Level.INFO, arcDetail::toString);
        } catch (GenericException | HttpConnectTimeoutException ex) {
            uiMessage = ex.getMessage();
        } catch (HttpTimeoutException ex) {
            uiMessage = "Server is taking too long to response";
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error in IO operation");
            uiMessage = "Something went wrong. Please try again";
        }
        if (arcDetail == null) {
            LOGGER.log(Level.SEVERE, uiMessage);
        }
    }
}
