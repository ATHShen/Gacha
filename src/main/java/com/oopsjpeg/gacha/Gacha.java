package com.oopsjpeg.gacha;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oopsjpeg.gacha.command.*;
import com.oopsjpeg.gacha.data.impl.Card;
import com.oopsjpeg.gacha.data.impl.Event;
import com.oopsjpeg.gacha.data.impl.Quest;
import com.oopsjpeg.gacha.data.json.CardSerializer;
import com.oopsjpeg.gacha.data.json.EventSerializer;
import com.oopsjpeg.gacha.data.json.QuestSerializer;
import com.oopsjpeg.gacha.handler.EventHandler;
import com.oopsjpeg.gacha.wrapper.UserWrapper;
import com.oopsjpeg.roboops.framework.commands.CommandCenter;
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
			.registerTypeAdapter(Quest.class, new QuestSerializer())
			.create();
	public static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(2);
	public static final Logger LOGGER = LoggerFactory.getLogger(Gacha.class.getName());

	private static Gacha instance;

	private Settings settings;
	private MongoMaster mongo;
	private IDiscordClient client;
	private CommandCenter commands;
	private IChannel connector;

	private List<UserWrapper> users = new ArrayList<>();
	private List<Card> cards = new ArrayList<>();
	private List<Event> events = new ArrayList<>();
	private List<Quest> quests = new ArrayList<>();

	private Map<String, BufferedImage> cardCache = new HashMap<>();

	public static void main(String[] args) {
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
			LOGGER.error("Created config.");
		else if (settings.load()) {
			if (settings.getDatabase().isEmpty() && settings.save())
				// Store default values if database name is empty
				LOGGER.error("Please insert your database name into the config.");
			else if (settings.getToken().isEmpty() && settings.save())
				// Store default values if database name is empty
				LOGGER.error("Please insert your bot's token into the config.");
			else {
				// Close mongo and logout client on shutdown
				Runtime.getRuntime().addShutdownHook(new Thread(() -> {
					if (client != null && client.isLoggedIn())
						client.logout();
					mongo.close();
				}));

				// Open the mongo connection
				mongo = new MongoMaster(settings.getDatabase());

				// Create the command center
				commands = new CommandCenter(settings.getPrefix());

				// Log the client in
				client = new ClientBuilder().withToken(settings.getToken())
						.registerListener(new EventHandler(this))
						.registerListener(commands)
						.login();
			}
		}
	}

	public void postBuild() {
		buildCommands();

		if (!getDataFolder().mkdir()) {
			loadCards();
			loadEvents();
			loadQuests();
		}

		// Link the bot connector
		if (settings.getConnectorID() != -1)
			connector = client.getChannelByID(settings.getConnectorID());

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
	}

	public void buildCommands() {
		commands.clear();
		commands.add(new AccountCommand());
		commands.add(new CardCommand());
		commands.add(new CardsCommand());
		commands.add(new DailyCommand());
		commands.add(new EventCommand());
		commands.add(new EventsCommand());
		commands.add(new ForgeCommand());
		commands.add(new GachaCommand());
		commands.add(new GiveCardCommand());
		commands.add(new GiveCrystalsCommand());
		commands.add(new QuestCommand());
		commands.add(new QuestsCommand());
		commands.add(new TestCardCommand());
	}

	public void loadCards() {
		try (FileReader fr = new FileReader(getDataFolder() + "\\cards.json")) {
			cards = Arrays.asList(Gacha.GSON.fromJson(fr, Card[].class));
		} catch (IOException err) {
			err.printStackTrace();
		}
	}

	public void loadEvents() {
		try (FileReader fr = new FileReader(getDataFolder() + "\\events.json")) {
			events = Arrays.asList(GSON.fromJson(fr, Event[].class));
		} catch (IOException err) {
			err.printStackTrace();
		}
	}

	public void loadQuests() {
		try (FileReader fr = new FileReader(getDataFolder() + "\\quests.json")) {
			quests = Arrays.asList(GSON.fromJson(fr, Quest[].class));
		} catch (IOException err) {
			err.printStackTrace();
		}
	}

	public MongoMaster getMongo() {
		return mongo;
	}

	public IDiscordClient getClient() {
		return client;
	}

	public CommandCenter getCommands() {
		return commands;
	}

	public IChannel getConnector() {
		return connector;
	}

	public List<UserWrapper> getUsers() {
		return users;
	}

	@SuppressWarnings("SuspiciousMethodCalls")
	public UserWrapper getUser(IUser user) {
		if (!users.contains(user) && !mongo.loadUser(user.getLongID()))
			users.add(new UserWrapper(user.getLongID()));
		return users.get(users.indexOf(user));
	}

	public List<Card> getCards() {
		return cards;
	}

	public Card getCardByID(String id) {
		return id == null ? null : getCards().stream()
				.filter(c -> c.getID().equalsIgnoreCase(id))
				.findAny().orElse(null);
	}

	public List<Card> getCardsForName(String name) {
		return cards.stream()
				.filter(c -> c.getName().toLowerCase().startsWith(name.toLowerCase()))
				.collect(Collectors.toList());
	}

	public List<Card> getCardsForStar(int star) {
		return getCardsForStar(star, star);
	}

	public List<Card> getCardsForStar(int min, int max) {
		return cards.stream().filter(c -> c.getStar() <= max && c.getStar() >= min)
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
			err.printStackTrace();
			return null;
		}
	}

	public List<Event> getEvents() {
		return events;
	}

	public List<Event> getCurrentEvents() {
		return getEvents().stream().filter(Event::isActive).collect(Collectors.toList());
	}

	public int getGachaCost() {
		if (getCurrentEvents().stream().anyMatch(e ->
				e.getType() == Event.Type.GACHA_DISCOUNT))
			return 250;
		return 500;
	}

	public List<Quest> getQuests() {
		return quests;
	}

	public Quest getQuestByID(String id) {
		return quests.stream().filter(q -> q.getID().equalsIgnoreCase(id)).findAny().orElse(null);
	}
}
