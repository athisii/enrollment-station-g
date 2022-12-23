package com.cdac.enrollmentstation.logging;

import com.cdac.enrollmentstation.constant.PropertyName;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.util.PropertyFile;

import java.io.IOException;
import java.util.logging.*;

/**
 * @author athisii, CDAC
 * Created on 17/12/22
 */
public class ApplicationLogNew {
    //Suppress default constructor for noninstantiability
    private ApplicationLogNew() {
        throw new AssertionError("The ApplicationLogNew methods should be accessed statically");
    }

    private static Handler handler;
    private static final Logger LOG = Logger.getLogger(ApplicationLogNew.class.getName());

    static {
        try {
            handler = new FileHandler(PropertyFile.getProperty(PropertyName.LOG_FILE), 1024000, 2); //1024000 is 1Mb - It will roll over after a file becomes 1Mb
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.ALL);
            LOG.addHandler(handler);
            LOG.setLevel(Level.INFO);
        } catch (GenericException ex) {
            // throws by ProperFile class if /etc/file.properties is not found.
            LOG.log(Level.SEVERE, "Property File not found. Shutting down.......");
            System.exit(-1);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Exception in Application Log. Failed to add new handler. Shutting down.......");
            System.exit(-1);
        } catch (SecurityException ex) {
            LOG.log(Level.SEVERE, "Exception in Application Log. Security Exception. Shutting down.......");
            System.exit(-1);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Errored occurred in creating Application Log. Shutting down.......");
            System.exit(-1);
        }
    }

    public static Logger getLogger(Class<?> klass) {
        var logger = Logger.getLogger(klass.getName());
        logger.addHandler(handler);
        return logger;
    }
}
