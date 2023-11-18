package com.cdac.enrollmentstation.util;

import com.cdac.enrollmentstation.constant.ApplicationConstant;
import com.cdac.enrollmentstation.constant.PropertyName;
import com.cdac.enrollmentstation.exception.GenericException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class DisplayUtil {
    //Suppress default constructor for noninstantiability
    private DisplayUtil() {
        throw new AssertionError("The DisplayUtil methods must be accessed statically.");
    }

    public static final int[] SCREEN_SD = {1024, 768}; //default
    public static final int[] SCREEN_HD = {1440, 1080};
    public static final int SCREEN_WIDTH;
    public static final int SCREEN_HEIGHT;

    static {
        String errorMessage = "**** Could not find a valid resolution format in " + PropertyFile.getProperty(PropertyName.SYSTEM_DISPLAY_RESOLUTION_FILE);
        try {
            String[] resolution = Arrays.stream(Files.readAllLines(Path.of(PropertyFile.getProperty(PropertyName.SYSTEM_DISPLAY_RESOLUTION_FILE)))
                            .stream()
                            .filter(line -> line.contains("--mode"))
                            .reduce((first, second) -> second) // get the last line
                            .orElseThrow(() -> new GenericException(errorMessage))
                            .split("\\s")) // split by white space
                    .filter(word -> word.matches("\\d{3,}[xX]\\d{3,}")) // split by 1920x1080
                    .findFirst()
                    .orElseThrow(() -> new GenericException(errorMessage))
                    .split("[xX]");
            SCREEN_WIDTH = Integer.parseInt(resolution[0]);
            SCREEN_HEIGHT = Integer.parseInt(resolution[1]);
        } catch (IOException ex) {
            throw new GenericException("**** No entry for " + PropertyName.SYSTEM_DISPLAY_RESOLUTION_FILE + " in " + ApplicationConstant.DEFAULT_PROPERTY_FILE + " or path pointed by it doesn't exist.");
        } catch (Exception ex) {
            throw new GenericException(ex.getMessage());
        }
    }
}
