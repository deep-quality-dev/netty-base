package com.useful.client.properties;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.util.Properties;

public class PropertyUtils {

    private static Properties properties = null;

    static {
        try {
            Resource resource = new ClassPathResource("application.properties");
            properties = PropertiesLoaderUtils.loadProperties(resource);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String getString(String key) {
        if (properties == null) {
            return null;
        }

        return (String)properties.get(key);
    }

    public static Integer getInteger(String key) {
        String value = getString(key);
        if (value == null) {
            return null;
        }

        return Integer.parseInt(value.trim());
    }

    public static Boolean getBoolean(String key) {
        String value = getString(key);
        if (value == null) {
            return null;
        }

        return Boolean.parseBoolean(value.trim());
    }
}
