package com.cdac.enrollmentstation.api;


import com.cdac.enrollmentstation.constant.ApplicationConstant;
import com.cdac.enrollmentstation.constant.PropertyName;
import com.cdac.enrollmentstation.dto.*;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.model.*;
import com.cdac.enrollmentstation.security.Aes256Util;
import com.cdac.enrollmentstation.security.HmacUtil;
import com.cdac.enrollmentstation.security.PkiUtil;
import com.cdac.enrollmentstation.util.PropertyFile;
import com.cdac.enrollmentstation.util.Singleton;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.Key;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author athisii, CDAC
 * Created on 29/03/23
 */

public class MafisServerApi {
    private static final String UNIQUE_KEY_HEADER = "UniqueKey";
    private static final String HASH_KEY_HEADER = "HashKey";

    private static final Logger LOGGER = ApplicationLog.getLogger(MafisServerApi.class);

    //Suppress default constructor for noninstantiability
    private MafisServerApi() {
        throw new AssertionError("The MafisServerApi methods must be accessed statically.");
    }

    /**
     * Fetches single ARCDetails based on e-ARC number.
     * Caller must handle the exception.
     *
     * @param arcNo unique id whose details are to be fetched
     * @return ARCDetails or null on connection timeout
     * @throws GenericException exception on connection timeout, error, json parsing exception etc.
     */

    public static ARCDetails fetchARCDetails(String arcNo) {
        String jsonRequestData;
        try {
            jsonRequestData = Singleton.getObjectMapper().writeValueAsString(new ArcNoReqDto(arcNo));
        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, ApplicationConstant.JSON_WRITE_ER_MSG);
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }
        HttpResponse<String> response = HttpUtil.sendHttpRequest(HttpUtil.createPostHttpRequest(getArcUrl(), jsonRequestData));
        if (response == null) {
            return null;
        }
        ARCDetails arcDetail;
        try {
            arcDetail = Singleton.getObjectMapper().readValue(response.body(), ARCDetails.class);
        } catch (JsonProcessingException ignored) {
            LOGGER.log(Level.SEVERE, ApplicationConstant.JSON_READ_ERR_MSG);
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }
        return arcDetail;
    }

    /**
     * Sends http post request.
     * Caller must handle the exception.
     *
     * @param data request payload
     * @return SaveEnrollmentResDto or null on connection timeout
     * @throws GenericException exception on error, json parsing exception etc.
     */
    public static SaveEnrollmentResDto postEnrollment(String data) {
        // to avoid encrypt/decrypt problems
        data = data.replace("\n", "");
        String receivedData = encryptAndSendToServer(data, getSaveEnrollmentUrl());

        // connection timeout
        if (receivedData == null) {
            return null;
        }

        // response data from server
        SaveEnrollmentResDto saveEnrollmentResDto;
        try {
            saveEnrollmentResDto = Singleton.getObjectMapper().readValue(receivedData, SaveEnrollmentResDto.class);
        } catch (JsonProcessingException ignored) {
            LOGGER.log(Level.SEVERE, ApplicationConstant.JSON_READ_ERR_MSG);
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }
        return saveEnrollmentResDto;
    }

    /**
     * Fetches all contracts based on contractor id and card serial number.
     * Caller must handle the exception.
     *
     * @return ContractResDto or null on connection timeout
     * @throws GenericException exception on error, json parsing exception etc.
     */
    public static ContractResDto fetchContractList(String contractorId, String cardSerialNumber) {
        String jsonRequestData;
        try {
            jsonRequestData = Singleton.getObjectMapper().writeValueAsString(new ContractReqDto(contractorId, cardSerialNumber));
        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, ApplicationConstant.JSON_WRITE_ER_MSG);
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }
        String uuid = Aes256Util.genUuid();


        HashMap<String, String> headers = new HashMap<>();
        headers.put(UNIQUE_KEY_HEADER, uuid);
        HttpResponse<String> response = HttpUtil.sendHttpRequest(HttpUtil.createPostHttpRequest(getContractListUrl(), jsonRequestData, headers));
        // connection timeout
        if (response == null) {
            return null;
        }
        ContractResDto contractResDto;
        try {
            contractResDto = Singleton.getObjectMapper().readValue(response.body(), ContractResDto.class);
        } catch (JsonProcessingException ignored) {
            LOGGER.log(Level.SEVERE, ApplicationConstant.JSON_READ_ERR_MSG);
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }
        return contractResDto;
    }


    /**
     * Fetches all labour based on contractor id and contract id.
     * Caller must handle the exception.
     *
     * @return LabourResDto or null on connection timeout
     * @throws GenericException exception on error, json parsing exception etc.
     */
    public static LabourResDto fetchLabourList(String contractorId, String contractId) {
        String data;
        try {
            data = Singleton.getObjectMapper().writeValueAsString(new LabourReqDto(contractorId, contractId));
            data = data.replace("\n", "");

        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, ApplicationConstant.JSON_WRITE_ER_MSG);
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }
        String receivedData = encryptAndSendToServer(data, getLabourListUrl());

        // connection timeout
        if (receivedData == null) {
            return null;
        }
        // response data from server
        LabourResDto labourResDto;
        try {
            labourResDto = Singleton.getObjectMapper().readValue(receivedData, LabourResDto.class);
        } catch (JsonProcessingException ignored) {
            LOGGER.log(Level.SEVERE, ApplicationConstant.JSON_READ_ERR_MSG);
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }
        return labourResDto;
    }


    /**
     * Update token status
     * Caller must handle the exception.
     *
     * @return UpdateTokenResponse or null on connection timeout
     * @throws GenericException exception on error, json parsing exception etc.
     */
    public static UpdateTokenResponse updateTokenStatus(UpdateToken updateToken) {
        String data;
        try {
            data = Singleton.getObjectMapper().writeValueAsString(updateToken);
        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, ApplicationConstant.JSON_WRITE_ER_MSG);
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }

        HttpResponse<String> response = HttpUtil.sendHttpRequest(HttpUtil.createPostHttpRequest(getTokenUpdateUrl(), data));
        // connection timeout
        if (response == null) {
            return null;
        }
        UpdateTokenResponse updateTokenResponse;
        try {
            updateTokenResponse = Singleton.getObjectMapper().readValue(response.body(), UpdateTokenResponse.class);
        } catch (JsonProcessingException ignored) {
            LOGGER.log(Level.SEVERE, ApplicationConstant.JSON_READ_ERR_MSG);
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }
        return updateTokenResponse;
    }

    private static String encryptAndSendToServer(String data, String url) {
        // assigns random secret key at each call
        String secret = Aes256Util.genUuid();
        Key key = Aes256Util.genKey(secret);
        // for sending base64 encoded encrypted SECRET KEY to server in HEADER
        byte[] pkiEncryptedUniqueKey = PkiUtil.encrypt(secret);
        String base64EncodedPkiEncryptedUniqueKey = Base64.getEncoder().encodeToString(pkiEncryptedUniqueKey);

        // encrypts the actual data passed from the method's argument and encoded to base64
        byte[] encryptedData = Aes256Util.encrypt(data, key);
        String base64EncodedEncryptedData = Base64.getEncoder().encodeToString(encryptedData);

        // hashKey header
        String messageDigest = HmacUtil.genHmacSha256(base64EncodedEncryptedData, secret);

        // need to add unique-key, hash value in request header
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put(UNIQUE_KEY_HEADER, base64EncodedPkiEncryptedUniqueKey);
        headersMap.put(HASH_KEY_HEADER, messageDigest);

        HttpRequest postHttpRequest = HttpUtil.createPostHttpRequest(url, base64EncodedEncryptedData, headersMap);
        HttpResponse<String> httpResponse = HttpUtil.sendHttpRequest(postHttpRequest);
        // connection timeout
        if (httpResponse == null) {
            return null;
        }
        Optional<String> base64EncodedUniqueKeyOptional = httpResponse.headers().firstValue(UNIQUE_KEY_HEADER);

        if (base64EncodedUniqueKeyOptional.isEmpty()) {
            LOGGER.log(Level.SEVERE, "Unique key header not found in http response");
            throw new GenericException("Unique Key not received from server.");
        }
        // received base64 encoded encrypted secret key from server
        byte[] encryptedSecretKey = Base64.getDecoder().decode(base64EncodedUniqueKeyOptional.get());
        secret = PkiUtil.decrypt(encryptedSecretKey);
        key = Aes256Util.genKey(secret);

        // Received base64 encoded encrypted data
        byte[] encryptedResponseBody = Base64.getDecoder().decode(httpResponse.body());
        return Aes256Util.decrypt(encryptedResponseBody, key);
    }

    /**
     * Fetches all units.
     * Caller must handle the exception.
     *
     * @return List<Units> or null on connection timeout
     * @throws GenericException exception on error, json parsing exception etc.
     */
    public static List<Unit> fetchAllUnits() {
        HttpResponse<String> response = HttpUtil.sendHttpRequest(HttpUtil.createGetHttpRequest(getUnitListURL()));
        if (response == null) {
            return null;
        }
        // if this line is reached, response received with status code 200
        UnitListDetails unitListDetails;
        try {
            unitListDetails = Singleton.getObjectMapper().readValue(response.body(), UnitListDetails.class);
        } catch (JsonProcessingException ignored) {
            LOGGER.log(Level.SEVERE, ApplicationConstant.JSON_READ_ERR_MSG);
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }
        if (unitListDetails.getErrorCode() != 0) {
            LOGGER.log(Level.SEVERE, () -> ApplicationConstant.GENERIC_SERVER_ERR_MSG + unitListDetails.getDesc());
            throw new GenericException(unitListDetails.getDesc());
        }
        return unitListDetails.getUnits();
    }


    /**
     * Fetches list of e-ARC based on unitCode.
     * Caller must handle the exception.
     *
     * @return List<ARCDetails> or null on connection timeout
     * @throws GenericException exception on error, json parsing exception etc.
     */

    public static List<ARCDetails> fetchArcListByUnitCode(String unitCode) {
        String jsonRequestData;
        try {
            jsonRequestData = Singleton.getObjectMapper().writeValueAsString(new UnitCodeReqDto(unitCode));
        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, ApplicationConstant.JSON_WRITE_ER_MSG);
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }
        HttpRequest postHttpRequest = HttpUtil.createPostHttpRequest(getDemographicURL(), jsonRequestData);
        HttpResponse<String> httpResponse = HttpUtil.sendHttpRequest(postHttpRequest);
        if (httpResponse == null) {
            return null;
        }
        ARCDetailsList arcDetailsList;
        try {
            arcDetailsList = Singleton.getObjectMapper().readValue(httpResponse.body(), ARCDetailsList.class);
        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, ApplicationConstant.JSON_READ_ERR_MSG);
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }
        if (arcDetailsList.getErrorCode() != 0) {
            LOGGER.log(Level.INFO, () -> ApplicationConstant.GENERIC_SERVER_ERR_MSG + arcDetailsList.getDesc());
            throw new GenericException(arcDetailsList.getDesc());
        }
        return arcDetailsList.getArcDetails();
    }


    public static String getUnitListURL() {
        return getMafisApiUrl() + "/GetAllUnits";
    }

    public static String getDemographicURL() {
        return getMafisApiUrl() + "/GetDemographicDetails";
    }

    /**
     * @return String - MAFIS API home url
     */
    public static String getMafisApiUrl() {
        String mafisServerApi = PropertyFile.getProperty(PropertyName.MAFIS_API_URL);
        if (mafisServerApi == null || mafisServerApi.isBlank()) {
            LOGGER.log(Level.SEVERE, () -> "'mafis.api.url' not found or is empty in " + ApplicationConstant.DEFAULT_PROPERTY_FILE);
            throw new GenericException("'mafis.api.url' not found or is empty in " + ApplicationConstant.DEFAULT_PROPERTY_FILE);
        }

        if (mafisServerApi.endsWith("/")) {
            mafisServerApi = mafisServerApi.substring(0, mafisServerApi.lastIndexOf("/"));
        }
        return mafisServerApi + "/api/EnrollmentStation";
    }

    public static String getEnrollmentStationId() {
        String enrollmentStationId = PropertyFile.getProperty(PropertyName.ENROLLMENT_STATION_ID);
        if (enrollmentStationId == null || enrollmentStationId.isBlank()) {
            throw new GenericException("'enrollment.station.id' not found or is empty in " + ApplicationConstant.DEFAULT_PROPERTY_FILE);
        }
        return enrollmentStationId;
    }

    public static String getEnrollmentStationUnitId() {
        String enrollmentStationUnitId = PropertyFile.getProperty(PropertyName.ENROLLMENT_STATION_UNIT_ID);
        if (enrollmentStationUnitId == null || enrollmentStationUnitId.isBlank()) {
            throw new GenericException("'enrollment.station.unit.id' not found or is empty in " + ApplicationConstant.DEFAULT_PROPERTY_FILE);
        }
        return enrollmentStationUnitId;
    }

    public static String getArcUrl() {
        return getMafisApiUrl() + "/GetDetailsByARCNo";
    }

    public static String getSaveEnrollmentUrl() {
        return getMafisApiUrl() + "/SaveEnrollment";
    }

    public static String getContractListUrl() {
        return getMafisApiUrl() + "/api/EnrollmentStation/GetContractList";
    }

    public static String getLabourListUrl() {
        return getMafisApiUrl() + "/api/EnrollmentStation/GetLabourList";
    }

    public static String getTokenUpdateUrl() {
        return getMafisApiUrl() + "/api/EnrollmentStation/UpdateTokenStatus";
    }

}
