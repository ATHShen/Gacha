package com.oopsjpeg.gacha;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.oopsjpeg.gacha.command.*;
import com.oopsjpeg.gacha.handler.CIMGHandler;
import com.oopsjpeg.gacha.handler.CommandHandler;
import com.oopsjpeg.gacha.handler.QuestHandler;
import com.oopsjpeg.gacha.handler.StatusHandler;
import com.oopsjpeg.gacha.json.CardSerializer;
import com.oopsjpeg.gacha.json.EventSerializer;
import com.oopsjpeg.gacha.json.LinkedMailSerializer;
import com.oopsjpeg.gacha.json.QuestSerializer;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.Event;
import com.oopsjpeg.gacha.object.Mail;
import com.oopsjpeg.gacha.object.Quest;
import com.oopsjpeg.gacha.object.user.UserInfo;
import com.oopsjpeg.gacha.object.user.UserMail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Gacha {
	public static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(Card.class, new CardSerializer())
			.registerTypeAdapter(Event.class, new EventSerializer())
			.registerTypeAdapter(Mail.class, new LinkedMailSerializer())
			.registerTypeAdapter(Quest.class, new QuestSerializer())
			.create();
	public static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(2);
	public static final Logger LOGGER = LoggerFactory.getLogger(Gacha.class.getName());

	private static Gacha instance;

	private Settings settings;
	private MongoMaster mongo;
	private IDiscordClient client;
	private CommandHandler commands;
	private IChannel connector;

	private List<UserInfo> users = new ArrayList<>();
	private List<Card> cards = new ArrayList<>();
	private List<Event> events = new ArrayList<>();
	private List<Quest> quests = new ArrayList<>();
	private List<List<IChannel>> cimgs = new ArrayList<>();
	private Map<String, Mail> linkedMail = new HashMap<>();

	private Map<String, BufferedImage> cardCache = new HashMap<>();

	public static void main(String[] args) {
		System.setProperty("user.timezone", "UTC");

		instance = new Gacha();
		instance.start();
	}

	public static Gacha getInstance() {
		return instance;
	}

	public static File getDataFolder() {
		return new File(System.getProperty("user.home") + "\\Gacha");
	}

	public void start() {
		// Load settings
		settings = new Settings(new File("config.cfg"));

		if (!settings.getFile().exists() && settings.save())
			// Create config if it doesn't exist
			LOGGER.info("Created config.");
		else if (settings.load()) {
			if (settings.getDatabase().isEmpty() && settings.save())
				// Store default values if database name is empty
				LOGGER.info("Please insert your database name into the config.");
			else if (settings.getToken().isEmpty() && settings.save())
				// Store default values if database name is empty
				LOGGER.info("Please insert your bot's token into the config.");
			else {
				// Close mongo and logout client on shutdown
				Runtime.getRuntime().addShutdownHook(new Thread(() -> {
					if (client != null && client.isLoggedIn())
						client.logout();
					mongo.close();
				}));

				// Open the mongo connection
				mongo = new MongoMaster(this, settings.getDatabase());

				if (!mongo.isConnected())
					// Mongo is not connected
					LOGGER.error("Error opening the mongo connection.");
				else {
					// Create the command center
					commands = new CommandHandler(settings.getPrefix());

					// Log the client in
					client = new ClientBuilder().withToken(settings.getToken())
							.registerListener(new StatusHandler())
							.registerListener(new QuestHandler(this))
							.registerListener(new CIMGHandler(this))
							.registerListener(commands)
							.login();
				}
			}
		}
	}

	public void postBuild() {
		buildCommands();

		if (!getDataFolder().mkdir()) {
			loadCards();
			loadEvents();
			loadQuests();
			loadChannels();
			loadLinkedMail();
		}

		// Set up the VCC timer
		SCHEDULER.scheduleAtFixedRate(() -> {
			// Loop all voice channels
			for (IVoiceChannel channel : client.getVoiceChannels()) {
				IGuild guild = channel.getGuild();
				// Ignore AFK channel
				if (!channel.equals(guild.getAFKChannel())) {
					// Loop users in voice channel
					for (IUser user : channel.getConnectedUsers()) {
						IVoiceState state = user.getVoiceStateForGuild(guild);
						// Cannot be muted or deafened
						if (!state.isMuted() && !state.isDeafened())
							getUser(user).vcc();
					}
				}
			}
		}, 30, 30, TimeUnit.SECONDS);

		// Set up the backup timer
		SCHEDULER.scheduleAtFixedRate(() -> mongo.backup(), 0, 1, TimeUnit.HOURS);
	}

	public void buildCommands() {
		commands.clear();
		commands.add(new CardCommand());
		commands.add(new CardsCommand());
		commands.add(new DailyCommand());
		commands.add(new EventCommand());
		commands.add(new EventsCommand());
		commands.add(new ForceBackupCommand());
		commands.add(new ForgeCommand());
		commands.add(new GachaCommand());
		commands.add(new GiveCardCommand());
		commands.add(new GiveCrystalsCommand());
		commands.add(new HelpCommand());
		commands.add(new MailCommand());
		commands.add(new ProfileCommand());
		commands.add(new QuestCommand());
		commands.add(new QuestsCommand());
		commands.add(new ReloadCardsCommand());
		commands.add(new ReportCommand());
		commands.add(new SendMailCommand());
		commands.add(new TestCardCommand());
		commands.add(new WeeklyCommand());
	}

	public void loadCards() {
		try (FileReader fr = new FileReader(getDataFolder() + "\\cards.json")) {
			cards = Arrays.stream(GSON.fromJson(fr, Card[].class))
					.filter(Objects::nonNull).collect(Collectors.toList());
			LOGGER.info("Loaded " + cards.size() + " card(s).");
		} catch (IOException err) {
			LOGGER.error("Error loading cards.");
			err.printStackTrace();
		}
	}

	public void loadEvents() {
		try (FileReader fr = new FileReader(getDataFolder() + "\\events.json")) {
			events = Arrays.stream(GSON.fromJson(fr, Event[].class))
					.filter(Objects::nonNull).collect(Collectors.toList());
			LOGGER.info("Loaded " + events.size() + " event(s).");
		} catch (IOException err) {
			LOGGER.error("Error loading events.");
			err.printStackTrace();
		}
	}

	public void loadQuests() {
		try (FileReader fr = new FileReader(getDataFolder() + "\\quests.json")) {
			quests = Arrays.stream(GSON.fromJson(fr, Quest[].class))
					.filter(Objects::nonNull).collect(Collectors.toList());
			LOGGER.info("Loaded " + quests.size() + " quest(s).");
		} catch (IOException err) {
			LOGGER.error("Error loading quests.");
			err.printStackTrace();
		}
	}

	public void loadChannels() {
		try (FileReader fr = new FileReader(getDataFolder() + "\\channels.json")) {
			JsonObject json = new JsonParser().parse(fr).getAsJsonObject();
			if (json.has("connector"))
				connector = client.getChannelByID(json.get("connector").getAsLong());
			if (json.has("cimgs"))
				cimgs = Arrays.stream(GSON.fromJson(json.getAsJsonArray("cimgs"), Long[][].class))
						.map(group -> Arrays.stream(group)
								.map(id -> client.getChannelByID(id))
								.collect(Collectors.toList()))
						.collect(Collectors.toList());
			LOGGER.info("Loaded channel(s).");
		} catch (IOException err) {
			LOGGER.error("Error loading channels.");
			err.printStackTrace();
		}
	}

	public void loadLinkedMail() {
		try (FileReader fr = new FileReader(getDataFolder() + "\\linkedmail.json")) {
			JsonObject json = new JsonParser().parse(fr).getAsJsonObject();
			for (String key : json.keySet())
				linkedMail.put(key, GSON.fromJson(json.get(key), Mail.class));
			LOGGER.info("Loaded " + linkedMail.size() + " linked mail.");
		} catch (IOException e) {
			LOGGER.error("Error loading linked mail.");
			e.printStackTrace();
		}
	}

	public Settings getSettings() {
		return settings;
	}

	public MongoMaster getMongo() {
		return mongo;
	}

	public IDiscordClient getClient() {
		return client;
	}

	public CommandHandler getCommands() {
		return commands;
	}

	public IChannel getConnector() {
		return connector;
	}

	public List<UserInfo> getUsers() {
		return users;
	}

	public UserInfo getUser(long id) {
		// New user
		if (users.stream().noneMatch(u -> id == u.getID()) && !mongo.loadUser(id)) {
			UserInfo info = new UserInfo(id);
			if (linkedMail.containsKey("welcome"))
				info.sendMail(new UserMail("welcome"));
			users.add(info);
			return info;
		}

		return users.stream().filter(u -> id == u.getID()).findAny().orElse(null);
	}

	public UserInfo getUser(IUser user) {
		return user == null ? null : getUser(user.getLongID());
	}

	public List<Card> getCards() {
		return cards;
	}

	public List<Card> getCurrentCards() {
		return cards.stream()
				.filter(c -> !c.isSpecial() || settings.getSpecialEnabled())
				.filter(c -> c.getGen() == settings.getCurrentGen())
				.collect(Collectors.toList());
	}

	public boolean isCurrentCard(Card card) {
		return getCurrentCards().contains(card);
	}

	public Card getCardByID(String id) {
		return id == null ? null : getCards().stream()
				.filter(c -> c.getID().equalsIgnoreCase(id))
				.findAny().orElse(null);
	}

	public List<Card> getCardsByName(String name) {
		return cards.stream()
				.filter(c -> c.getName().toLowerCase().startsWith(name.toLowerCase()))
				.collect(Collectors.toList());
	}

	public List<Card> getCardsByStar(int star) {
		return cards.stream().filter(c -> c.getStar() == star)
				.collect(Collectors.toList());
	}

	public InputStream getCachedCard(String id) {
		id = id.toLowerCase();
		if (!cardCache.containsKey(id))
			cardCache.put(id, Util.genImage(getCardByID(id)));

		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ImageIO.write(cardCache.get(id), "png", os);
			return new ByteArrayInputStream(os.toByteArray());
		} catch (IOException err) {
			LOGGER.error("Error loading cached card ID " + id + ".");
			err.printStackTrace();
			return null;
		}
	}

	public Map<String, BufferedImage> getCardCache() {
		return cardCache;
	}

	public List<Event> getEvents() {
		return events;
	}

	public List<Quest> getQuests() {
		return quests;
	}

	public Quest getQuestByID(String id) {
		return quests.stream().filter(q -> q.getID().equalsIgnoreCase(id)).findAny().orElse(null);
	}

	public List<List<IChannel>> getCIMGs() {
		return cimgs;
	}

	public boolean isCIMG(IChannel channel) {
		return cimgs.stream().anyMatch(g -> g.contains(channel));
	}

	public int getCIMGGroup(IChannel channel) {
		return cimgs.indexOf(cimgs.stream().filter(group -> group.contains(channel)).findAny().orElse(null));
	}

	public Map<String, Mail> getLinkedMail() {
		return linkedMail;
	}
}
