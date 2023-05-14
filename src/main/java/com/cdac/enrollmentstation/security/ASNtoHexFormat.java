/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdac.enrollmentstation.security;


import com.cdac.enrollmentstation.model.*;
import com.yafred.asn1.runtime.BERWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
//import org.bouncycastle.asn1.*;

/**
 * @author boss
 */
public class ASNtoHexFormat {

    public static void main(String args[]) {

//        byte[] encodedDyanamicFile = getEncodedDyanamicFile();
//        System.out.println("ENcoded dynamic File::"+encodedDyanamicFile);
//        
//
//        
//        String encodedbase64Dynamic = Base64.getEncoder().encodeToString(encodedDyanamicFile);
//        
//        System.out.println("Encoded dynamic FILLE :::"+encodedbase64Dynamic);


//        byte[] encodedDefaultAccessValidity = getEncodedDefaultAccessValidity();
//        System.out.println("ENcoded dynamic File::"+encodedDefaultAccessValidity);
//        
//
//        
//        String encodedbase64DefaultAccessValidity = Base64.getEncoder().encodeToString(encodedDefaultAccessValidity);
//        
//        System.out.println("Encoded dynamic FILLE :::"+encodedbase64DefaultAccessValidity);


    }

    //Get Labour Photo
    //public static byte[] getEncodedLabourPhoto(){
    public static Object[] getEncodedLabourPhoto() {
        ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
        Labour labourDetails = holder.getLabourDetails();
        ContractorDetailsFile contractorDynamicDetails = holder.getContractorDynamicDetails();

        TokenPhoto tokenPhoto = new TokenPhoto();
        tokenPhoto.setPhoto(contractorDynamicDetails.getLabourPhoto());

        int length = 0;


        ByteArrayOutputStream bufferOut = new ByteArrayOutputStream();
        BERWriter berWriter = new BERWriter(bufferOut);
        try {
            length = writeTokenLabourPhoto(tokenPhoto, berWriter);
        } catch (Exception ex) {
            Logger.getLogger(ASNtoHexFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
        byte[] berEncodedDefaultAccess = bufferOut.toByteArray();

        return new Object[]{berEncodedDefaultAccess, length};

    }

    public static int writeTokenLabourPhoto(TokenPhoto tokenLabourPhoto, BERWriter writer) {
        int componentLength = 0;
        try {
            componentLength = writeTokenPhoto(tokenLabourPhoto, writer);
        } catch (Exception ex) {
            Logger.getLogger(ASNtoHexFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
        componentLength += writer.writeLength(componentLength);
        componentLength += writer.writeOctetString(new byte[]{48}); /* CONSTRUCTED_UNIVERSAL_16 */
        try {
            writer.flush();
        } catch (IOException ex) {
            Logger.getLogger(ASNtoHexFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
        return componentLength;
    }

    public static int writeTokenPhoto(TokenPhoto tokenLabourPhoto, BERWriter writer) {
        int length = 0;

        if (tokenLabourPhoto.getPhoto() != null) {
            int componentLength = 0;
            componentLength = writer.writeRestrictedCharacterString(tokenLabourPhoto.getPhoto());
            componentLength += writer.writeLength(componentLength);
            componentLength += writer.writeOctetString(new byte[]{22}); /* PRIMITIVE_UNIVERSAL_22 */
            length += componentLength;
        }
        return length;
    }


    //Get Default Access Validity
    // public static byte[] getEncodedDefaultAccessValidity(){
    public static Object[] getEncodedDefaultAccessValidity() {

        // byte[] decodedcsn = Base64.getDecoder().decode(csnValue);
        //  String decodedCsnValue = DatatypeConverter.printHexBinary(decodedcsn);
        //return decodedCsnValue;
        ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
        Labour labourDetails = holder.getLabourDetails();
        ContractorDetailsFile contractorDynamicDetails = holder.getContractorDynamicDetails();

        TokenDefaultAccessValidity tokenDefaultAccess = new TokenDefaultAccessValidity();
        tokenDefaultAccess.setFromDate(contractorDynamicDetails.getAccessFromDate());
        tokenDefaultAccess.setToDate(contractorDynamicDetails.getAccessToDate());
        //    tokenDefaultAccess.setFromdate("20-10-2021");
        //    tokenDefaultAccess.setTodate("21-10-2021");

        int length = 0;
        ByteArrayOutputStream bufferOut = new ByteArrayOutputStream();
        BERWriter berWriter = new BERWriter(bufferOut);
        try {
            length = writeTokenDefaultAccess(tokenDefaultAccess, berWriter);
            System.out.println("Length :::" + length);
        } catch (Exception ex) {
            Logger.getLogger(ASNtoHexFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
        byte[] berEncodedDefaultAccess = bufferOut.toByteArray();
        System.out.println("BER ENCODED DEfault A:" + berEncodedDefaultAccess);
        return new Object[]{berEncodedDefaultAccess, length};
        // return berEncodedDefaultAccess;
    }

    public static int writeTokenDefaultAccess(TokenDefaultAccessValidity tokenDefaultAccess, BERWriter writer) {
        int componentLength = 0;
        try {
            componentLength = writeTokenDefault(tokenDefaultAccess, writer);
        } catch (Exception ex) {
            Logger.getLogger(ASNtoHexFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
        componentLength += writer.writeLength(componentLength);
        componentLength += writer.writeOctetString(new byte[]{48}); /* CONSTRUCTED_UNIVERSAL_16 */
        try {
            writer.flush();
        } catch (IOException ex) {
            Logger.getLogger(ASNtoHexFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
        return componentLength;
    }

    public static int writeTokenDefault(TokenDefaultAccessValidity tokenDefaultAccess, BERWriter writer) {
        int length = 0;

        if (tokenDefaultAccess.getFromDate() != null) {
            int componentLength = 0;
            componentLength = writer.writeRestrictedCharacterString(tokenDefaultAccess.getFromDate());
            componentLength += writer.writeLength(componentLength);
            componentLength += writer.writeOctetString(new byte[]{22}); /* PRIMITIVE_UNIVERSAL_22 */
            length += componentLength;
        }

        if (tokenDefaultAccess.getToDate() != null) {
            int componentLength = 0;
            componentLength = writer.writeRestrictedCharacterString(tokenDefaultAccess.getToDate());
            componentLength += writer.writeLength(componentLength);
            componentLength += writer.writeOctetString(new byte[]{22}); /* PRIMITIVE_UNIVERSAL_19 */
            length += componentLength;
        }

        return length;
    }


    //Get Special Access Permission
    //  public static byte[] getEncodedSpecialAccessPermission(){
    public static Object[] getEncodedSpecialAccessPermission() {
        // byte[] decodedcsn = Base64.getDecoder().decode(csnValue);
        //  String decodedCsnValue = DatatypeConverter.printHexBinary(decodedcsn);
        //return decodedCsnValue;
        ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
        Labour labourDetails = holder.getLabourDetails();
        ContractorDetailsFile contractorDynamicDetails = holder.getContractorDynamicDetails();

        TokenSpecialAccessPermission tokenSpecialAccess = new TokenSpecialAccessPermission();
        tokenSpecialAccess.setUnitCode(contractorDynamicDetails.getAccessUnitCode());
        tokenSpecialAccess.setWorkingHourCode(contractorDynamicDetails.getAccessWorkingHourCode());
        tokenSpecialAccess.setZoneId(contractorDynamicDetails.getAccessZoneId());

        int length = 0;
        ByteArrayOutputStream bufferOut = new ByteArrayOutputStream();
        BERWriter berWriter = new BERWriter(bufferOut);
        try {
            length = writeTokenSpecialAccess(tokenSpecialAccess, berWriter);
        } catch (Exception ex) {
            Logger.getLogger(ASNtoHexFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
        byte[] berEncodedSpecialAccess = bufferOut.toByteArray();

        return new Object[]{berEncodedSpecialAccess, length};
        //return berEncodedSpecialAccess;
    }

    public static int writeTokenSpecialAccess(TokenSpecialAccessPermission tokenSpecialAccess, BERWriter writer) {
        int componentLength = 0;
        try {
            componentLength = writeSpecialDefault(tokenSpecialAccess, writer);
        } catch (Exception ex) {
            Logger.getLogger(ASNtoHexFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
        componentLength += writer.writeLength(componentLength);
        componentLength += writer.writeOctetString(new byte[]{48}); /* CONSTRUCTED_UNIVERSAL_16 */
        try {
            writer.flush();
        } catch (IOException ex) {
            Logger.getLogger(ASNtoHexFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
        return componentLength;
    }

    public static int writeSpecialDefault(TokenSpecialAccessPermission tokenSpecialAccess, BERWriter writer) {
        int length = 0;

        if (tokenSpecialAccess.getUnitCode() != null) {
            int componentLength = 0;
            componentLength = writer.writeRestrictedCharacterString(tokenSpecialAccess.getUnitCode());
            componentLength += writer.writeLength(componentLength);
            componentLength += writer.writeOctetString(new byte[]{22}); /* PRIMITIVE_UNIVERSAL_22 */
            length += componentLength;
        }

        if (tokenSpecialAccess.getWorkingHourCode() != null) {
            int componentLength = 0;
            componentLength = writer.writeRestrictedCharacterString(tokenSpecialAccess.getWorkingHourCode());
            componentLength += writer.writeLength(componentLength);
            componentLength += writer.writeOctetString(new byte[]{22}); /* PRIMITIVE_UNIVERSAL_19 */
            length += componentLength;
        }

        if (tokenSpecialAccess.getZoneId() != null) {
            int componentLength = 0;
            componentLength = writer.writeRestrictedCharacterString(tokenSpecialAccess.getZoneId());
            componentLength += writer.writeLength(componentLength);
            componentLength += writer.writeOctetString(new byte[]{22}); /* PRIMITIVE_UNIVERSAL_19 */
            length += componentLength;
        }

        return length;
    }


//   public void getDynamicFileDetails(){
//        ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
//        Labour labourDetails = holder.getLabourDetails();
//        ContractorDynamicFile contractorDynamicDetails = holder.getContractorDynamicDetails();
//        DynamicFile dynamicFile = new DynamicFile();
//        dynamicFile.setContractorId(contractorDynamicDetails.getDynamicContractorId());
//        dynamicFile.setUserCategoryId(contractorDynamicDetails.getDynamicUserCategoryId());
//        dynamicFile.setIssuanceUnit(contractorDynamicDetails.getDynamicIssuanceUnit());
//        dynamicFile.setLabourName(labourDetails.getLabourName());
//        dynamicFile.setLabourGenderId(labourDetails.getGenderId()); 
//        dynamicFile.setLabourDateOfBirth(labourDetails.getDateOfBirth());
//        dynamicFile.setLabourBloodGroupId(labourDetails.getBloodGroupId());
//        dynamicFile.setLabourNationalityId(labourDetails.getNationalityId());
//        dynamicFile.setLabourId(labourDetails.getLabourId());
//        
//        System.out.println("Dynamic Select file:::"+dynamicFile.toString());
//        
//   }

    public static Object[] getEncodedDyanamicFile() {
        //public static byte[] getEncodedDyanamicFile(){
        // byte[] decodedcsn = Base64.getDecoder().decode(csnValue);
        //  String decodedCsnValue = DatatypeConverter.printHexBinary(decodedcsn);
        //return decodedCsnValue;
        ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
        Labour labourDetails = holder.getLabourDetails();
        ContractorDetailsFile contractorDynamicDetails = holder.getContractorDynamicDetails();
        DynamicFile dynamicFile = new DynamicFile();
        dynamicFile.setContractorId(contractorDynamicDetails.getDynamicContractorId());
        dynamicFile.setUserCategoryId(contractorDynamicDetails.getDynamicUserCategoryId());
        dynamicFile.setIssuanceUnit(contractorDynamicDetails.getDynamicIssuanceUnit());
        //uncomment after
//        dynamicFile.setLabourName(labourDetails.getLabourName());
//        dynamicFile.setLabourGenderId(labourDetails.getGenderId()); 
//        dynamicFile.setLabourDateOfBirth(labourDetails.getDateOfBirth());
//        dynamicFile.setLabourBloodGroupId(labourDetails.getBloodGroupId());
//        dynamicFile.setLabourNationalityId(labourDetails.getNationalityId());
//        dynamicFile.setLabourId(labourDetails.getLabourId());

        //
        //Labour obj = new Labour();
        //obj.setLabourId("Lab001");
        //obj.setGenderId("M");
        //obj.setLabourName("Labour01");
        int length = 0;
        ByteArrayOutputStream bufferOut = new ByteArrayOutputStream();
        BERWriter berWriter = new BERWriter(bufferOut);
        try {
            length = writeTokenDynamicFile(dynamicFile, berWriter);
            System.out.println("Length Dynamic File :::" + length);
        } catch (Exception ex) {
            Logger.getLogger(ASNtoHexFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
        byte[] berEncodedDefaultAccess = bufferOut.toByteArray();
        System.out.println("BER ENCODED DEfault A:" + berEncodedDefaultAccess);


        byte[] berEncodedDynamic = bufferOut.toByteArray();

        //return berEncodedDynamic;
        return new Object[]{berEncodedDynamic, length};
    }

    public static int writeTokenDynamicFile(DynamicFile pdu, BERWriter writer) {
        int componentLength = 0;
        try {
            componentLength = writeTokenDynamic(pdu, writer);
        } catch (Exception ex) {
            Logger.getLogger(ASNtoHexFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
        componentLength += writer.writeLength(componentLength);
        componentLength += writer.writeOctetString(new byte[]{48}); /* CONSTRUCTED_UNIVERSAL_16 */
        try {
            writer.flush();
        } catch (IOException ex) {
            Logger.getLogger(ASNtoHexFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
        return componentLength;
    }


    public static int writeTokenDynamic(DynamicFile instance, BERWriter writer) {
        int length = 0;

        if (instance.getContractorId() != null) {
            int componentLength = 0;
            componentLength = writer.writeRestrictedCharacterString(instance.getContractorId());
            componentLength += writer.writeLength(componentLength);
            componentLength += writer.writeOctetString(new byte[]{22}); /* PRIMITIVE_UNIVERSAL_22 */
            length += componentLength;
        }

        if (instance.getUserCategoryId() != null) {
            int componentLength = 0;
            componentLength = writer.writeRestrictedCharacterString(instance.getUserCategoryId());
            componentLength += writer.writeLength(componentLength);
            componentLength += writer.writeOctetString(new byte[]{2}); /* PRIMITIVE_UNIVERSAL_22 */
            length += componentLength;
        }

        if (instance.getIssuanceUnit() != null) {
            int componentLength = 0;
            componentLength = writer.writeRestrictedCharacterString(instance.getIssuanceUnit());
            componentLength += writer.writeLength(componentLength);
            componentLength += writer.writeOctetString(new byte[]{22}); /* PRIMITIVE_UNIVERSAL_22 */
            length += componentLength;
        }

        if (instance.getLabourId() != null) {
            int componentLength = 0;
            componentLength = writer.writeRestrictedCharacterString(instance.getLabourId());
            componentLength += writer.writeLength(componentLength);
            componentLength += writer.writeOctetString(new byte[]{22}); /* PRIMITIVE_UNIVERSAL_22 */
            length += componentLength;
        }

        if (instance.getLabourName() != null) {
            int componentLength = 0;
            componentLength = writer.writeRestrictedCharacterString(instance.getLabourName());
            componentLength += writer.writeLength(componentLength);
            componentLength += writer.writeOctetString(new byte[]{19}); /* PRIMITIVE_UNIVERSAL_19 */
            length += componentLength;
        }

        if (instance.getLabourGenderId() != null) {
            int componentLength = 0;
            componentLength = writer.writeRestrictedCharacterString(instance.getLabourGenderId());
            componentLength += writer.writeLength(componentLength);
            componentLength += writer.writeOctetString(new byte[]{2}); /* PRIMITIVE_UNIVERSAL_2 */
            length += componentLength;
        }


        if (instance.getLabourDateOfBirth() != null) {
            int componentLength = 0;
            componentLength = writer.writeRestrictedCharacterString(instance.getLabourDateOfBirth());
            componentLength += writer.writeLength(componentLength);
            componentLength += writer.writeOctetString(new byte[]{22}); /* PRIMITIVE_UNIVERSAL_22 */
            length += componentLength;
        }

        if (instance.getLabourBloodGroupId() != null) {
            int componentLength = 0;
            componentLength = writer.writeRestrictedCharacterString(instance.getLabourBloodGroupId());
            componentLength += writer.writeLength(componentLength);
            componentLength += writer.writeOctetString(new byte[]{22}); /* PRIMITIVE_UNIVERSAL_22 */
            length += componentLength;
        }

        if (instance.getLabourNationalityId() != null) {
            int componentLength = 0;
            componentLength = writer.writeRestrictedCharacterString(instance.getLabourNationalityId());
            componentLength += writer.writeLength(componentLength);
            componentLength += writer.writeOctetString(new byte[]{2}); /* PRIMITIVE_UNIVERSAL_22 */
            length += componentLength;
        }

        return length;
    }

    //Get Fingerprint Data
    //  public static byte[] getEncodedSpecialAccessPermission(){
    public static Object[] getEncodedFingerprintData() {
        // byte[] decodedcsn = Base64.getDecoder().decode(csnValue);
        //  String decodedCsnValue = DatatypeConverter.printHexBinary(decodedcsn);
        //return decodedCsnValue;
        ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
        Labour labourDetails = holder.getLabourDetails();
        ContractorDetailsFile contractorDynamicDetails = holder.getContractorDynamicDetails();

        TokenFingerPrint tokenFingerPrint = new TokenFingerPrint();
        tokenFingerPrint.setFpData(contractorDynamicDetails.getLabourFpData());
        tokenFingerPrint.setFpPos(contractorDynamicDetails.getLabourFpPos());


        int length = 0;
        ByteArrayOutputStream bufferOut = new ByteArrayOutputStream();
        BERWriter berWriter = new BERWriter(bufferOut);
        try {
            length = writeFingerPrint(tokenFingerPrint, berWriter);
        } catch (Exception ex) {
            Logger.getLogger(ASNtoHexFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
        byte[] berEncodedSpecialAccess = bufferOut.toByteArray();

        return new Object[]{berEncodedSpecialAccess, length};
        //return berEncodedSpecialAccess;
    }

    public static int writeFingerPrint(TokenFingerPrint tokenFingerPrint, BERWriter writer) {
        int componentLength = 0;
        try {
            componentLength = writeFingerPrintDefault(tokenFingerPrint, writer);
        } catch (Exception ex) {
            Logger.getLogger(ASNtoHexFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
        componentLength += writer.writeLength(componentLength);
        componentLength += writer.writeOctetString(new byte[]{48}); /* CONSTRUCTED_UNIVERSAL_16 */
        try {
            writer.flush();
        } catch (IOException ex) {
            Logger.getLogger(ASNtoHexFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
        return componentLength;
    }

    public static int writeFingerPrintDefault(TokenFingerPrint tokenFingerPrint, BERWriter writer) {
        int length = 0;

        if (tokenFingerPrint.getFpPos() != null) {
            int componentLength = 0;
            componentLength = writer.writeRestrictedCharacterString(tokenFingerPrint.getFpPos());
            componentLength += writer.writeLength(componentLength);
            componentLength += writer.writeOctetString(new byte[]{22}); /* PRIMITIVE_UNIVERSAL_22 */
            length += componentLength;
        }

        if (tokenFingerPrint.getFpData() != null) {
            int componentLength = 0;
            componentLength = writer.writeRestrictedCharacterString(tokenFingerPrint.getFpData());
            componentLength += writer.writeLength(componentLength);
            componentLength += writer.writeOctetString(new byte[]{127}); /* PRIMITIVE_UNIVERSAL_19 */
            length += componentLength;
        }

        return length;
    }

    //Get Signature1 Data
    //  public static byte[] getEncodedSpecialAccessPermission(){
    public static Object[] getEncodedSignature1() {
        // byte[] decodedcsn = Base64.getDecoder().decode(csnValue);
        //  String decodedCsnValue = DatatypeConverter.printHexBinary(decodedcsn);
        //return decodedCsnValue;
        ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
        Labour labourDetails = holder.getLabourDetails();
        ContractorDetailsFile contractorDynamicDetails = holder.getContractorDynamicDetails();

        TokenSignature1 tokenSign1 = new TokenSignature1();
        tokenSign1.setSignFile1(contractorDynamicDetails.getSignatureFile1());


        int length = 0;
        ByteArrayOutputStream bufferOut = new ByteArrayOutputStream();
        BERWriter berWriter = new BERWriter(bufferOut);
        try {
            length = writeSignature1(tokenSign1, berWriter);
        } catch (Exception ex) {
            Logger.getLogger(ASNtoHexFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
        byte[] berEncodedSpecialAccess = bufferOut.toByteArray();

        return new Object[]{berEncodedSpecialAccess, length};
        //return berEncodedSpecialAccess;
    }

    public static int writeSignature1(TokenSignature1 tokenSign1, BERWriter writer) {
        int componentLength = 0;
        try {
            componentLength = writeSignature1Default(tokenSign1, writer);
        } catch (Exception ex) {
            Logger.getLogger(ASNtoHexFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
        componentLength += writer.writeLength(componentLength);
        componentLength += writer.writeOctetString(new byte[]{48}); /* CONSTRUCTED_UNIVERSAL_16 */
        try {
            writer.flush();
        } catch (IOException ex) {
            Logger.getLogger(ASNtoHexFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
        return componentLength;
    }

    public static int writeSignature1Default(TokenSignature1 tokenSign1, BERWriter writer) {
        int length = 0;

        if (tokenSign1.getSignFile1() != null) {
            int componentLength = 0;
            componentLength = writer.writeRestrictedCharacterString(tokenSign1.getSignFile1());
            componentLength += writer.writeLength(componentLength);
            componentLength += writer.writeOctetString(new byte[]{22}); /* PRIMITIVE_UNIVERSAL_22 */
            length += componentLength;
        }

        return length;
    }


    //Get Signature1 Data
    //  public static byte[] getEncodedSpecialAccessPermission(){
    public static Object[] getEncodedSignature3() {
        // byte[] decodedcsn = Base64.getDecoder().decode(csnValue);
        //  String decodedCsnValue = DatatypeConverter.printHexBinary(decodedcsn);
        //return decodedCsnValue;
        ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
        Labour labourDetails = holder.getLabourDetails();
        ContractorDetailsFile contractorDynamicDetails = holder.getContractorDynamicDetails();

        TokenSignature3 tokenSign3 = new TokenSignature3();
        tokenSign3.setSignFile3(contractorDynamicDetails.getSignatureFile3());


        int length = 0;
        ByteArrayOutputStream bufferOut = new ByteArrayOutputStream();
        BERWriter berWriter = new BERWriter(bufferOut);
        try {
            length = writeSignature3(tokenSign3, berWriter);
        } catch (Exception ex) {
            Logger.getLogger(ASNtoHexFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
        byte[] berEncodedSpecialAccess = bufferOut.toByteArray();

        return new Object[]{berEncodedSpecialAccess, length};
        //return berEncodedSpecialAccess;
    }

    public static int writeSignature3(TokenSignature3 tokenSign3, BERWriter writer) {
        int componentLength = 0;
        try {
            componentLength = writeSignature3Default(tokenSign3, writer);
        } catch (Exception ex) {
            Logger.getLogger(ASNtoHexFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
        componentLength += writer.writeLength(componentLength);
        componentLength += writer.writeOctetString(new byte[]{48}); /* CONSTRUCTED_UNIVERSAL_16 */
        try {
            writer.flush();
        } catch (IOException ex) {
            Logger.getLogger(ASNtoHexFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
        return componentLength;
    }

    public static int writeSignature3Default(TokenSignature3 tokenSign3, BERWriter writer) {
        int length = 0;

        if (tokenSign3.getSignFile3() != null) {
            int componentLength = 0;
            componentLength = writer.writeRestrictedCharacterString(tokenSign3.getSignFile3());
            componentLength += writer.writeLength(componentLength);
            componentLength += writer.writeOctetString(new byte[]{22}); /* PRIMITIVE_UNIVERSAL_22 */
            length += componentLength;
        }

        return length;
    }


}
