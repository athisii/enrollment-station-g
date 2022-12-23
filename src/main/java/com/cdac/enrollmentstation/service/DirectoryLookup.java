/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdac.enrollmentstation.service;

/**
 * @author root
 */

import com.cdac.enrollmentstation.util.TestProp;

import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Properties;

public class DirectoryLookup {
    TestProp prop = new TestProp();

    public String doLookup(String username, String password) {
        String domain = null;
        String ldapurl = null;
        domain = prop.getProp().getProperty("domain");
        ldapurl = prop.getProp().getProperty("ldapurl");
        boolean result = false;
        //String domain = "CDACAD";
        String securityprincipal = domain + "\\" + username;
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        //properties.put(Context.PROVIDER_URL, "ldap://10.184.49.113");
        //properties.put(Context.PROVIDER_URL, "ldap://10.184.36.14");
        properties.put(Context.PROVIDER_URL, ldapurl);
        properties.put(Context.SECURITY_AUTHENTICATION, "simple");
        //properties.put(Context.SECURITY_PRINCIPAL, "uid="+username+",cn=users,cn=accounts,dc=dssc,dc=mil");
        //properties.put(Context.SECURITY_PRINCIPAL, "uid="+username+",cn=users,cn=accounts,dc=cdacad,dc=in");
        properties.put(Context.SECURITY_PRINCIPAL, securityprincipal);
        //properties.put(Context.SECURITY_PRINCIPAL, "cn="+username+",dc=cdacad,dc=in");
        //properties.put(Context.SECURITY_PRINCIPAL, "uid=sysadmin1,cn=users,cn=accounts,dc=dssc,dc=mil");
        properties.put(Context.SECURITY_CREDENTIALS, password);
        //properties.put(Context.SECURITY_CREDENTIALS, "CDACchennai!12");
        try {
            DirContext context = new InitialDirContext(properties);
            result = context != null;
            if (context != null) {

                context.close();
            }
            System.out.println("Result::::::" + result);
            //Attributes attrs = context.getAttributes("uid=sysadmin1,cn=users,cn=accounts,dc=dssc,dc=mil");
            //System.out.println("Surname: " + attrs.get("sn").get());
            //System.out.println("Common name : " + attrs.get("cn").get());
            //System.out.println("telephone number : " + attrs.get("telephoneNumber").get());
        } catch (CommunicationException e) {
            return "Failed to connect with ldap server";
        } catch (AuthenticationException e) {
            return "Failed to authenticate user";
        } catch (NamingException e) {
            //e.printStackTrace();
            System.out.println("Invalid credentials");
            return "Invalid credentials";
        }

        return "true";
    }

    public static void main(String[] args) {
        DirectoryLookup sample = new DirectoryLookup();
        //String result = sample.doLookup("r101", "boss");
        //String result = sample.doLookup("CDACAD\\sudhakar", "Root1234#$");
        String result = sample.doLookup("sudhakar", "Root1234#$");
        System.out.println("Ldap Result::" + result);
    }

}