
package com.cdac.enrollmentstation.api;

import com.cdac.enrollmentstation.constant.ApplicationConstant;
import com.cdac.enrollmentstation.constant.PropertyName;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.util.PropertyFile;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author athisii, CDAC
 * Created on 29/03/23
 */
public class LocalCardReaderApiUrl {
    public static final Logger LOGGER = ApplicationLog.getLogger(LocalCardReaderApiUrl.class);

    //Suppress default constructor for noninstantiability
    private LocalCardReaderApiUrl() {
        throw new AssertionError("The LocalCardReaderApiUrl methods must be accessed statically.");
    }

    public static String getInitialize() {
        // return "http://localhost:8088/N_Initialize"
        return requireNonBlank(PropertyFile.getProperty(PropertyName.CARD_API_INITIALIZE));
    }

    public static String getWaitForConnect() {
        // return "http://localhost:8088/N_Wait_for_Connect"
        return requireNonBlank(PropertyFile.getProperty(PropertyName.CARD_API_WAIT_FOR_CONNECT));
    }

    public static String getSelectApp() {
        //  return "http://localhost:8088/N_SelectApp"
        return requireNonBlank(PropertyFile.getProperty(PropertyName.CARD_API_SELECT_APP));
    }

    public static String getReadDataFromNaval() {
        // return "http://localhost:8088/N_readDatafromNaval"
        return requireNonBlank(PropertyFile.getProperty(PropertyName.CARD_API_READ_DATA));
    }

    public static String getStoreDataOnNaval() {
        // return "http://localhost:8088/N_storeDataonNaval"
        return requireNonBlank(PropertyFile.getProperty(PropertyName.CARD_API_STORE_DATA));
    }

    public static String getVerifyCertificate() {
        //  return "http://localhost:8088/N_verifyCertificate"
        return requireNonBlank(PropertyFile.getProperty(PropertyName.CARD_API_VERIFY_CERT));
    }

    public static String getPkiAuth() {
        //  return "http://localhost:8088/N_PKIAuth"
        return requireNonBlank(PropertyFile.getProperty(PropertyName.CARD_API_PKI_AUTH));
    }

    public static String getCardRemoval() {
        //  return "http://localhost:8088/N_Wait_for_Removal"
        return requireNonBlank(PropertyFile.getProperty(PropertyName.CARD_API_WAIT_FOR_REMOVAL));
    }

    public static String getDeInitialize() {
        //   return "http://localhost:8088/N_DeInitialize"
        return requireNonBlank(PropertyFile.getProperty(PropertyName.CARD_API_DE_INITIALIZE));
    }

    public static String getListOfReaders() {
        //   return "http://localhost:8088/listOfReaders"
        return requireNonBlank(PropertyFile.getProperty(PropertyName.CARD_API_LIST_OF_READERS));
    }

    public static String requireNonBlank(String value) {
        if (value == null || value.isBlank()) {
            LOGGER.log(Level.SEVERE, () -> "Property '" + value + "' is empty or not found in " + ApplicationConstant.DEFAULT_PROPERTY_FILE + ". Please add it.");
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }
        return value;
    }


}
