/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdac.enrollmentstation.security;


import com.cdac.enrollmentstation.model.ContractorStaticFile;
import com.yafred.asn1.runtime.BERReader;

import java.io.ByteArrayInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
//import org.bouncycastle.asn1.*;

/**
 * @author boss
 */
public class ASNtoHexReadFormat {
    public static void main(String args[]) {

//        byte[] encodedDyanamicFile = getEncodedDyanamicFile();
//        System.out.println("ENcoded dynamic File::"+encodedDyanamicFile);
//        
//
//        
//        String encodedbase64Dynamic = Base64.getEncoder().encodeToString(encodedDyanamicFile);
//        
//         System.out.println("Encoded dynamic FILLE :::"+encodedbase64Dynamic);


        String decodedString = "3081A60462434337373333384130313030303330303033303030303030304330323035413541343030313930303230303034423633363336363336333346463337373137373031373132323737303531363037303030303733323630354537353532313032313116023831160830313031313939311608303131303230323102016F160B496E6469616E204E6176791605546563684D02010102012B1305536168696C16046D61726B000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
        byte[] decodedbyte = decodedString.getBytes();
        ByteArrayInputStream bufferIn = new ByteArrayInputStream(decodedbyte);
        BERReader berReader = new BERReader(bufferIn);
        try {
            readPdu(berReader);
            System.out.println("READ::::" + readPdu(berReader));
        } catch (Exception ex) {
            Logger.getLogger(ASNtoHexReadFormat.class.getName()).log(Level.SEVERE, "read error", ex);
        }


//          byte[] encodedDefaultAccessValidity = getEncodedDefaultAccessValidity();
//        System.out.println("ENcoded dynamic File::"+encodedDefaultAccessValidity);
//        
//
//        
//        String encodedbase64DefaultAccessValidity = Base64.getEncoder().encodeToString(encodedDefaultAccessValidity);
//        
//         System.out.println("Encoded dynamic FILLE :::"+encodedbase64DefaultAccessValidity);
//        

    }

    public String readStaticFile(byte[] readData) {
        ByteArrayInputStream bufferIn = new ByteArrayInputStream(readData);
        BERReader berReader = new BERReader(bufferIn);
        try {
            readPdu(berReader);
            System.out.println("READ::::" + readPdu(berReader));
        } catch (Exception ex) {
            Logger.getLogger(ASNtoHexReadFormat.class.getName()).log(Level.SEVERE, "read error", ex);
        }

        return null;
    }

    public static ContractorStaticFile readPdu(com.yafred.asn1.runtime.BERReader reader) throws Exception {
        reader.readTag();
        //reader.mustMatchTag(new byte[] {48}); /* CONSTRUCTED_UNIVERSAL_16 */
        reader.mustMatchTag(new byte[]{51}); /* CONSTRUCTED_UNIVERSAL_16 */
        reader.readLength();
        ContractorStaticFile ret = new ContractorStaticFile();
        read(ret, reader, reader.getLengthValue());
        return ret;
    }

//     public static ContractorStaticFile readPdu(com.yafred.asn1.runtime.ASNValueReader reader) throws Exception {
//            ContractorStaticFile ret = new ContractorStaticFile();
//            read(ret, reader);
//            return ret;
//            }
//    

    public static void read(ContractorStaticFile instance, com.yafred.asn1.runtime.BERReader reader, int length) throws Exception {
        int componentLength = 0;
        if (length == 0) return;
        reader.readTag();
        if (length != -1) length -= reader.getTagLength();
        //reader.mustMatchTag(new byte[] {4}); /* PRIMITIVE_UNIVERSAL_4 */
        reader.mustMatchTag(new byte[]{56}); /* PRIMITIVE_UNIVERSAL_4 */
        reader.readLength();
        if (length != -1) length -= reader.getLengthLength();
        componentLength = reader.getLengthValue();
        instance.setChipSerialNo(reader.readOctetString(componentLength));
        if (length != -1) length -= componentLength;
        if (length == 0) return;
        if (reader.isTagMatched()) {
            reader.readTag();
            if (length != -1) length -= reader.getTagLength();
        }
        //reader.mustMatchTag(new byte[] {22}); /* PRIMITIVE_UNIVERSAL_22 */
        reader.mustMatchTag(new byte[]{48}); /* PRIMITIVE_UNIVERSAL_22 */
        reader.readLength();
        if (length != -1) length -= reader.getLengthLength();
        componentLength = reader.getLengthValue();
        instance.setCardNo(reader.readRestrictedCharacterString(componentLength));
        if (length != -1) length -= componentLength;
        if (length == 0) return;
        if (reader.isTagMatched()) {
            reader.readTag();
            if (length != -1) length -= reader.getTagLength();
        }
        //reader.mustMatchTag(new byte[] {22}); /* PRIMITIVE_UNIVERSAL_22 */
        reader.mustMatchTag(new byte[]{51}); /* PRIMITIVE_UNIVERSAL_22 */
        reader.readLength();
        if (length != -1) length -= reader.getLengthLength();
        componentLength = reader.getLengthValue();
        instance.setDateOfBirth(reader.readRestrictedCharacterString(componentLength));
        if (length != -1) length -= componentLength;
        if (length == 0) return;
        if (reader.isTagMatched()) {
            reader.readTag();
            if (length != -1) length -= reader.getTagLength();
        }
        //reader.mustMatchTag(new byte[] {22}); /* PRIMITIVE_UNIVERSAL_22 */
        reader.mustMatchTag(new byte[]{51}); /* PRIMITIVE_UNIVERSAL_22 */
        reader.readLength();
        if (length != -1) length -= reader.getLengthLength();
        componentLength = reader.getLengthValue();
        instance.setDateIssued(reader.readRestrictedCharacterString(componentLength));
        if (length != -1) length -= componentLength;
        if (length == 0) return;
        if (reader.isTagMatched()) {
            reader.readTag();
            if (length != -1) length -= reader.getTagLength();
        }
        // reader.mustMatchTag(new byte[] {2}); /* PRIMITIVE_UNIVERSAL_2 */
        reader.mustMatchTag(new byte[]{51}); /* PRIMITIVE_UNIVERSAL_2 */
        reader.readLength();
        if (length != -1) length -= reader.getLengthLength();
        componentLength = reader.getLengthValue();
        instance.setNationality(reader.readRestrictedCharacterString(componentLength));
        if (length != -1) length -= componentLength;
        if (length == 0) return;
        if (reader.isTagMatched()) {
            reader.readTag();
            if (length != -1) length -= reader.getTagLength();
        }
        //reader.mustMatchTag(new byte[] {22}); /* PRIMITIVE_UNIVERSAL_22 */
        reader.mustMatchTag(new byte[]{66}); /* PRIMITIVE_UNIVERSAL_22 */
        reader.readLength();
        if (length != -1) length -= reader.getLengthLength();
        componentLength = reader.getLengthValue();
        instance.setIssuedBy(reader.readRestrictedCharacterString(componentLength));
        if (length != -1) length -= componentLength;
        if (length == 0) return;
        if (reader.isTagMatched()) {
            reader.readTag();
            if (length != -1) length -= reader.getTagLength();
        }
        //reader.mustMatchTag(new byte[] {22}); /* PRIMITIVE_UNIVERSAL_22 */
        reader.mustMatchTag(new byte[]{51}); /* PRIMITIVE_UNIVERSAL_22 */
        reader.readLength();
        if (length != -1) length -= reader.getLengthLength();
        componentLength = reader.getLengthValue();
        instance.setFirmName(reader.readRestrictedCharacterString(componentLength));
        if (length != -1) length -= componentLength;
        if (length == 0) return;
        if (reader.isTagMatched()) {
            reader.readTag();
            if (length != -1) length -= reader.getTagLength();
        }
        //reader.mustMatchTag(new byte[] {2}); /* PRIMITIVE_UNIVERSAL_2 */
        reader.mustMatchTag(new byte[]{48}); /* PRIMITIVE_UNIVERSAL_2 */
        reader.readLength();
        if (length != -1) length -= reader.getLengthLength();
        componentLength = reader.getLengthValue();
        instance.setGender(reader.readRestrictedCharacterString(componentLength));
        if (length != -1) length -= componentLength;
        if (length == 0) return;
        if (reader.isTagMatched()) {
            reader.readTag();
            if (length != -1) length -= reader.getTagLength();
        }
        //reader.mustMatchTag(new byte[] {2}); /* PRIMITIVE_UNIVERSAL_2 */
        reader.mustMatchTag(new byte[]{48}); /* PRIMITIVE_UNIVERSAL_2 */
        reader.readLength();
        if (length != -1) length -= reader.getLengthLength();
        componentLength = reader.getLengthValue();
        instance.setUserCategoryId(reader.readRestrictedCharacterString(componentLength));
        if (length != -1) length -= componentLength;
        if (length == 0) return;
        if (reader.isTagMatched()) {
            reader.readTag();
            if (length != -1) length -= reader.getTagLength();
        }
        //reader.mustMatchTag(new byte[] {19}); /* PRIMITIVE_UNIVERSAL_19 */
        reader.mustMatchTag(new byte[]{48}); /* PRIMITIVE_UNIVERSAL_19 */
        reader.readLength();
        if (length != -1) length -= reader.getLengthLength();
        componentLength = reader.getLengthValue();
        instance.setName(reader.readRestrictedCharacterString(componentLength));
        if (length != -1) length -= componentLength;
        if (length == 0) return;
        if (reader.isTagMatched()) {
            reader.readTag();
            if (length != -1) length -= reader.getTagLength();
        }
        reader.mustMatchTag(new byte[]{22}); /* PRIMITIVE_UNIVERSAL_22 */
        reader.readLength();
        if (length != -1) length -= reader.getLengthLength();
        componentLength = reader.getLengthValue();
        instance.setUniqueId(reader.readRestrictedCharacterString(componentLength));
        if (length != -1) length -= componentLength;
        if (length == -1) {
            reader.readTag();
            reader.mustMatchTag(new byte[]{0});
            reader.mustReadZeroLength();
        } else if (length != 0) throw new Exception("length should be 0, not " + length);
        return;
    }


}
