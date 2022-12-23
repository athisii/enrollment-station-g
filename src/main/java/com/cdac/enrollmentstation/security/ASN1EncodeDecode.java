/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdac.enrollmentstation.security;

/**
 * @author root
 */


import com.yafred.asn1.runtime.BERDumper;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ASN1EncodeDecode {

    //private final AsnDecoder<byte[]> decoder = new BerDecoder();


    public static void main(String[] args) {


        String values = "3081A60462434337373333384130313030303330303033303030303030304330323035413541343030313930303230303034423633363336363336333346463337373137373031373132323737303531363037303030303733323630354537353532313032313116023831160830313031313939311608303131303230323102016F160B496E6469616E204E6176791605546563684D02010102012B1305536168696C16046D61726B";


        // dump TLV form
        ByteArrayInputStream bufferIn = new ByteArrayInputStream(BERDumper.bytesFromString(values));
        //System.out.println("aaaa"+bufferIn);
        try {
            new BERDumper(new PrintWriter(System.out)).dump(bufferIn);
        } catch (IOException ex) {
            Logger.getLogger(ASN1EncodeDecode.class.getName()).log(Level.SEVERE, null, ex);

        }


        String hexString = "3081A60462434337373333384130313030303330303033303030303030304330323035413541343030313930303230303034423633363336363336333346463337373137373031373132323737303531363037303030303733323630354537353532313032313116023831160830313031313939311608303131303230323102016F160B496E6469616E204E6176791605546563684D02010102012B1305536168696C16046D61726B";
        byte[] bytes = null;
        try {
            bytes = Hex.decodeHex(hexString.toCharArray());
        } catch (DecoderException ex) {
            System.out.println("Hex decode exception" + ex);
            // Logger.getLogger(HextoAsn.class.getName()).log(Level.SEVERE, null, ex);
        }


        try {
            System.out.println("dfgdfgdf" + new String(bytes, "UTF8"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ASN1EncodeDecode.class.getName()).log(Level.SEVERE, null, ex);
        }

        String serverString = "3031313032303231";
        new String(serverString.getBytes());

    }

}
