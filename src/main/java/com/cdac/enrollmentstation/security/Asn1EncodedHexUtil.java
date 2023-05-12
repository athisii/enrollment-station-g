/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdac.enrollmentstation.security;


import com.cdac.enrollmentstation.constant.ApplicationConstant;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;

import java.io.ByteArrayInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Asn1EncodedHexUtil {
    private static final Logger LOGGER = ApplicationLog.getLogger(Asn1EncodedHexUtil.class);


    public enum CardDataIndex {
        CHIP_SERIAL_NUMBER(0),
        CARD_NUMBER(1),
        CARD_TYPE_ID(2),
        USER_CATEGORY_ID(3),
        NAME(4),
        SERVICE(5),
        PIN_NUMBER(6),
        UNIQUE_ID(7),
        RANK(8),
        DESIGNATION(9),
        GROUP(10),
        DATE_OF_BIRTH(11),
        UNIT(12),
        ZONE_ACCESS(13),
        DATE_ISSUED(14),
        PLACE_ISSUED(15),
        BLOOD_GROUP(16),
        NATIONALITY(17),
        ISSUED_BY(18),
        FIRMS_NAME(19),
        GENDER(20),
        SPONSOR_PHONE_NUMBER(21),
        SPONSOR_NAME(22),
        SPONSOR_RANK(23),
        SPONSORS_UNIT(24),
        RELATION(25);
        private final int value;

        CardDataIndex(int val) {
            value = val;
        }

        public int getValue() {
            return value;
        }
    }

    //Suppress default constructor for noninstantiability
    private Asn1EncodedHexUtil() {
        throw new AssertionError("The Asn1EncodedHexUtil methods must be accessed statically.");
    }

    // throws GenericException
    // Caller must handle the exception
    public static String extractFromStaticAns1EncodedHex(byte[] bytes, CardDataIndex cardDataIndex) {
        try {
            ASN1InputStream asn1InputStream = new ASN1InputStream(new ByteArrayInputStream(bytes));
            ASN1Primitive asn1Primitive = asn1InputStream.readObject();
            ASN1Sequence asn1Sequence = ASN1Sequence.getInstance(asn1Primitive);
            return asn1Sequence.getObjectAt(cardDataIndex.getValue()).toString();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }
    }
}
