package com.oopsjpeg.gacha;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class Settings {
	private final File file;
	private final Properties properties = new Properties();

	public Settings(File file) {
		this.file = file;
	}

	public boolean load() {
		try (FileReader fr = new FileReader(file)) {
			properties.load(fr);
			return true;
		} catch (IOException err) {
			err.printStackTrace();
		}
		return false;
	}

	public boolean save() {
		try (FileWriter fw = new FileWriter(file)) {
			properties.put("database", getDatabase());
			properties.put("token", getToken());
			properties.put("prefix", getPrefix());
			properties.store(fw, "Celeste settings");
			return true;
		} catch (IOException err) {
			err.printStackTrace();
		}
		return false;
	}

	public File getFile() {
		return file;
	}

	public String getDatabase() {
		return (String) properties.getOrDefault("database", "");
	}

	public void setDatabase(String database) {
		properties.put("database", database);
	}

	public String getToken() {
		return (String) properties.getOrDefault("token", "");
	}

	public void setToken(String token) {
		properties.put("token", token);
	}

	public String getPrefix() {
		return (String) properties.getOrDefault("prefix", "/");
	}

	public void setPrefix() {
		properties.put("prefix", "/");
	}
}
