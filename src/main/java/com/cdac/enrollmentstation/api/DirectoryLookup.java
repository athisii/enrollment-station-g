package com.cdac.enrollmentstation.api;

import com.cdac.enrollmentstation.constant.ApplicationConstant;
import com.cdac.enrollmentstation.constant.PropertyName;
import com.cdac.enrollmentstation.exception.AuthException;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.util.PropertyFile;

import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author athisii, CDAC
 * Created on 29/03/23
 */

public class DirectoryLookup {

    private static final Logger LOGGER = ApplicationLog.getLogger(DirectoryLookup.class);

    public static boolean doLookup(String username, String password) {
        String domain = PropertyFile.getProperty(PropertyName.LDAP_DOMAIN);
        String ldapUrl = PropertyFile.getProperty(PropertyName.LDAP_URL);
        String securityPrincipal = username + domain;
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        properties.put(Context.PROVIDER_URL, ldapUrl);
        properties.put(Context.SECURITY_AUTHENTICATION, "simple");
        properties.put(Context.SECURITY_PRINCIPAL, securityPrincipal);
        properties.put(Context.SECURITY_CREDENTIALS, password);
        properties.put("com.sun.jndi.ldap.connect.timeout", "10000");
        properties.put("com.sun.jndi.ldap.read.timeout", "10000");    // 10 seconds read timeout

        LOGGER.info(() -> "Connecting to LDAP server at: " + ldapUrl);

        DirContext ctx = null;
        try {
            ctx = new InitialDirContext(properties);
            return true;
        } catch (AuthenticationException ex) {
            LOGGER.log(Level.SEVERE, () -> "Failed to authenticate user.");
            throw new AuthException(ApplicationConstant.INVALID_CREDENTIALS);
        } catch (CommunicationException ex) {
            LOGGER.log(Level.SEVERE, "Communication Exception Occurred:  ", ex);
            throw new GenericException("Failed to connect with the LDAP server.");
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Exception Occurred:  ", ex);
            throw new GenericException("Connection timeout or ldap is configured incorrectly.");
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException e) {
                    LOGGER.log(Level.SEVERE, "Error closing ldap context: ", e);
                }
            }
        }
    }

    //Suppress default constructor for noninstantiability
    private DirectoryLookup() {
        throw new AssertionError("The DirectoryLookup methods must be accessed statically.");
    }


}