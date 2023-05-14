package com.cdac.enrollmentstation.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author athisii, CDAC
 * Created on 22/01/23
 */
public class LocalCardReaderErrMsgUtil {
    private static final Map<String, String> errorMessageMap = new HashMap<>();

    static {
        errorMessageMap.put("0000", "Success");
        errorMessageMap.put("0001", "The card-type given is wrong or unexpected.");
        errorMessageMap.put("0002", "The certificate value given is wrong or unexpected.");
        errorMessageMap.put("0003", "The Operation given is wrong or unexpected.");
        errorMessageMap.put("0005", "The input data given is wrong or unexpected.");
        errorMessageMap.put("0006", "The input data len given is wrong or unexpected.");
        errorMessageMap.put("0007", "The output data given is wrong or unexpected.");
        errorMessageMap.put("0008", "The output data length given is wrong or unexpected.");
        errorMessageMap.put("0009", "The which-trust value given is wrong or unexpected.");
        errorMessageMap.put("000A", "The which-data value given is wrong or unexpected.");
        errorMessageMap.put("000B", "The readerName is not correct.");
        errorMessageMap.put("000C", "The Security status not satisfied");
        errorMessageMap.put("000D", "The prerequisite not satisfied");
        errorMessageMap.put("000E", "Wrong which symmetric key.");
        errorMessageMap.put("000F", "Wrong APDU.");
        errorMessageMap.put("0010", "Wrong Decrypted Challenge.");
        errorMessageMap.put("0011", "Wrong Encrypted Challenge.");
        errorMessageMap.put("0013", "Security Operation is not permitted in current scenario.");
        errorMessageMap.put("0014", "Invalid Handle.");
        errorMessageMap.put("0017", "which-trust and which-certificate are both correct but which-certificate does’t belong to which-trust.");
        errorMessageMap.put("0018", "Reader Already Connected.");
        errorMessageMap.put("0505", "Sequence not followed.");
        errorMessageMap.put("0506", "Invalid kms data.");
        errorMessageMap.put("0507", "Wrong prime.");
        errorMessageMap.put("0508", "License failure.");
        errorMessageMap.put("6E00", "Class not supported.");
        errorMessageMap.put("6D00", "Instruction code not supported.");
        errorMessageMap.put("6B00", "Wrong parameters P1-P2.");
        errorMessageMap.put("6A00", "No information given.");
        errorMessageMap.put("6A80", "Incorrect parameters in the command data field.");
        errorMessageMap.put("6A81", "Function not supported.");
        errorMessageMap.put("6A82", "File or application not found.");
        errorMessageMap.put("6A83", "Record not found.");
        errorMessageMap.put("6A84", "Not enough memory space in the file.");
        errorMessageMap.put("6A85", "Nc inconsistent with TLV structure.");
        errorMessageMap.put("6A86", "Incorrect parameters P1-P2.");
        errorMessageMap.put("6A87", "Nc inconsistent with parameters P1-P2.");
        errorMessageMap.put("6A88", "Reference data not found.");
        errorMessageMap.put("6900", "no information given.");
        errorMessageMap.put("6981", "command incompatible with file structure.");
        errorMessageMap.put("6982", "security status not satisfied.");
        errorMessageMap.put("6983", "authentication method blocked.");
        errorMessageMap.put("6984", "reference data not usable.");
        errorMessageMap.put("6985", "condition of use not satisfied.");
        errorMessageMap.put("6986", "command not allowed(no current EF).");
        errorMessageMap.put("6987", "expected secure messaging data objects missing.");
        errorMessageMap.put("6988", "Incorrect secure messaging data objects.");
        errorMessageMap.put("6700", "Wrong length; no further indication.");
        errorMessageMap.put("6283", "Selected File deactivated.");
        errorMessageMap.put("6285", "Selected file in terminated state.");
        errorMessageMap.put("6284", "File control information not formatted.");
        errorMessageMap.put("6A89", "File already exists.");
        errorMessageMap.put("6300", "No information given.");
        errorMessageMap.put("6600", "SECURITY_ERROR.");
        errorMessageMap.put("6581", "Memory failure.");
        errorMessageMap.put("6A8A", "DF name already exists.");
        errorMessageMap.put("6282", "End of file or record reached before reading Ne bytes.");
        errorMessageMap.put("6100", "MORE_DATA.");
        errorMessageMap.put("6C00", "Wrong Le field; SW2 encodes the exact number of available data bytes (see text below).");
        errorMessageMap.put("6400", "Execution error.");
        errorMessageMap.put("63C0", "No of retries reached 0.");
        errorMessageMap.put("6702", "Wrong length; no further indication.");
    }

    public static String getMessage(String hexadecimalErrorCode) {
        String errorMessage = errorMessageMap.get(hexadecimalErrorCode);
        return errorMessage == null ? "No error message found for: " + hexadecimalErrorCode : errorMessage;
    }


    public static String getMessage(int errorCode) {
        if (errorCode > 0) {
            errorCode = Integer.parseInt("-" + errorCode);
        }
        String hexadecimal = Integer.toHexString(errorCode).toUpperCase();
        return getMessage(hexadecimal.substring(hexadecimal.length() - 4));
    }

    // suppresses for  noninstantiability
    private LocalCardReaderErrMsgUtil() {
        throw new AssertionError("The LocalCardReaderErrMsgUtil fields must be accessed statically.");
    }

}
