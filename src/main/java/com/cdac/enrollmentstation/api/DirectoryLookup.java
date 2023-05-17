package com.cdac.enrollmentstation.api;

/**
 * @author athisii, CDAC
 * Created on 29/03/23
 */

import com.cdac.enrollmentstation.constant.ApplicationConstant;
import com.cdac.enrollmentstation.constant.PropertyName;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.util.PropertyFile;

import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DirectoryLookup {

    private static final Logger LOGGER = ApplicationLog.getLogger(DirectoryLookup.class);

    public static boolean doLookup(String username, String password) {
        String domain = PropertyFile.getProperty(PropertyName.LDAP_DOMAIN);
        String ldapUrl = PropertyFile.getProperty(PropertyName.LDAP_URL);
        // domain = "@hq.inidannavy.mil"
        // ldapUrl = "ldap://10.184.36.14:389"
        // securityPrincipal = "uid=username,dc=cdac,dc=in" // or usernmae@domain
        String securityPrincipal = username + domain;
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        properties.put(Context.PROVIDER_URL, ldapUrl);
        properties.put(Context.SECURITY_AUTHENTICATION, "simple");
        properties.put(Context.SECURITY_PRINCIPAL, securityPrincipal);
        properties.put(Context.SECURITY_CREDENTIALS, password);
        properties.put("com.sun.jndi.ldap.connect.timeout", "10000");

        try {
            new InitialDirContext(properties);
            return true;
        } catch (AuthenticationException ex) {
            LOGGER.log(Level.SEVERE, () -> "Failed to authenticate user.");
            throw new GenericException(ApplicationConstant.INVALID_CREDENTIALS);
        } catch (CommunicationException ex) {
            LOGGER.log(Level.SEVERE, "Failed to connect with server.");
            throw new GenericException("Failed to connect with server.");
        } catch (NamingException ex) {
            LOGGER.log(Level.SEVERE, ex::getMessage);
            throw new GenericException("Connection timeout or ldap is configured incorrectly.");
        }
    }

    //Suppress default constructor for noninstantiability
    private DirectoryLookup() {
        throw new AssertionError("The DirectoryLookup methods must be accessed statically.");
    }


}