package com.oopsjpeg.gacha;

import lombok.Getter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * Properties wrapper with custom defaults.
 * Created by oopsjpeg on 1/30/2019.
 */
public class Settings {
    public static final String MONGO_HOST = "mongo_host";
    public static final String MONGO_DATABASE = "mongo_database";

    public static final String TOKEN = "token";
    public static final String PREFIX = "prefix";

    private static final Properties DEFAULTS = new Properties();

    static {
        DEFAULTS.put(MONGO_HOST, "127.0.0.1");
        DEFAULTS.put(MONGO_DATABASE, "gacha");

        DEFAULTS.put(TOKEN, "");
        DEFAULTS.put(PREFIX, "/");
    }

    @Getter private final File file;
    @Getter private final Properties properties = new Properties();

    public Settings(String file) {
        this.file = new File(file);
        properties.putAll(DEFAULTS);
    }

    public boolean load() {
        try (FileReader fr = new FileReader(getFile())) {
            properties.load(fr);
            return true;
        } catch (IOException error) {
            error.printStackTrace();
            return false;
        }
    }

    public boolean save() {
        try (FileWriter fw = new FileWriter(getFile())) {
            properties.store(fw, "Gacha settings");
            return true;
        } catch (IOException error) {
            error.printStackTrace();
            return false;
        }
    }

    public String get(String key) {
        return properties.getProperty(key, "");
    }

    public int getInt(String key) {
        return Integer.valueOf(get(key));
    }

    public long getLong(String key) {
        return Long.valueOf(get(key));
    }

    public float getFloat(String key) {
        return Float.valueOf(get(key));
    }

    public double getDouble(String key) {
        return Double.valueOf(get(key));
    }

    public boolean has(String key) {
        return properties.containsKey(key) && !get(key).isEmpty();
    }
}