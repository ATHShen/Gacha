package com.oopsjpeg.gacha;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oopsjpeg.gacha.command.util.CommandManager;
import com.oopsjpeg.gacha.json.CardSerializer;
import com.oopsjpeg.gacha.manager.DataManager;
import com.oopsjpeg.gacha.manager.MongoManager;
import com.oopsjpeg.gacha.manager.StatusManager;
import com.oopsjpeg.gacha.object.Card;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Gacha {
	public static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(Card.class, new CardSerializer())
			.create();
	public static final ScheduledExecutorService SCHEDULER = Executors
			.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() + 1);
	public static final Logger LOGGER = LoggerFactory.getLogger(Gacha.class.getName());

	private static Gacha instance;

	private JDA client;
	private Settings settings;

	private MongoManager mongo;
	private CommandManager commands;
	private DataManager data;

	public static void main(String[] args) throws LoginException {
		System.setProperty("user.timezone", "UTC");

		instance = new Gacha();
		instance.start();
	}

	public static Gacha getInstance() {
		return instance;
	}

	public static File getDataFolder() {
		return new File(System.getProperty("user.home") + "\\Gacha Data");
	}

	public void start() throws LoginException {
		// Load settings
		settings = new Settings("gacha.properties");

		if (!settings.getFile().exists()) {
			if (settings.save())
				LOGGER.info("Created new settings.");
			else
				LOGGER.info("Failed to create new settings.");
		} else if (!settings.load()) {
			LOGGER.error("Failed to load settings.");
		} else if (!settings.has(Settings.MONGO_HOST)) {
			LOGGER.error("Please insert your mongo host into the settings.");
		} else if (!settings.has(Settings.MONGO_DATABASE)) {
			LOGGER.error("Please insert your mongo database into the settings.");
		} else if (!settings.has(Settings.TOKEN)) {
			LOGGER.error("Please insert your bot's token into the settings.");
		} else if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
			LOGGER.error("Failed to create data folder.");
		} else {
			// Close mongo on shutdown
			Runtime.getRuntime().addShutdownHook(new Thread(() -> mongo.getClient().close()));

			// Create mongo manager
			mongo = new MongoManager(this, settings.get(Settings.MONGO_HOST), settings.get(Settings.MONGO_DATABASE));

			if (!mongo.isConnected())
				LOGGER.error("Failed to start mongo manager.");
			else {
				// Create other managers
				commands = new CommandManager(this, settings.get(Settings.PREFIX));
				data = new DataManager(this);

				// Log the client in
				client = new JDABuilder(settings.get(Settings.TOKEN))
						.addEventListener(mongo)
						.addEventListener(commands)
						.addEventListener(data)
						.addEventListener(new StatusManager(this)).build();
			}
		}
	}

	public JDA getClient() {
		return client;
	}

	public Settings getSettings() {
		return settings;
	}

	public MongoManager getMongo() {
		return mongo;
	}

	public CommandManager getCommands() {
		return commands;
	}

	public DataManager getData() {
		return data;
	}
}
