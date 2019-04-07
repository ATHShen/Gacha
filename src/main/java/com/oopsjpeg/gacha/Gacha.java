package com.oopsjpeg.gacha;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oopsjpeg.gacha.command.CommandListener;
import com.oopsjpeg.gacha.json.CardSerializer;
import com.oopsjpeg.gacha.manager.MongoManager;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.CardEmbed;
import com.oopsjpeg.gacha.object.user.Bank;
import com.oopsjpeg.gacha.object.user.UserInfo;
import lombok.Getter;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.AnnotatedEventManager;
import net.dv8tion.jda.core.hooks.SubscribeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Gacha {
    public static final Logger LOGGER = LoggerFactory.getLogger(Gacha.class.getName());
    public static final File DATA_FOLDER = new File(System.getProperty("user.home") + "\\Gacha Data");
    public static final ScheduledExecutorService SCHEDULER = Executors
            .newScheduledThreadPool(Runtime.getRuntime().availableProcessors() + 1);
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Card.class, new CardSerializer())
            .setPrettyPrinting().create();

    @Getter private static Gacha instance;

    @Getter private JDA client;
    @Getter private Settings settings;
    @Getter private MongoManager mongo;
    @Getter
    private String prefix;

    @Getter private Map<Long, UserInfo> users = new HashMap<>();
    @Getter private Map<Integer, Card> cards = new HashMap<>();
    @Getter private Map<Integer, CardEmbed> cardEmbeds = new HashMap<>();

    public static void main(String[] args) throws LoginException {
        System.setProperty("user.timezone", "UTC");

        instance = new Gacha();
        instance.start();
    }

    public void start() throws LoginException {
        // Load settings
        settings = new Settings("gacha.properties");

        if (!settings.getFile().exists()) {
            if (settings.save())
                LOGGER.info("Created new settings.");
            else
                LOGGER.info("Error creating new settings.");
        } else if (!settings.load()) {
            LOGGER.error("Error loading settings.");
        } else if (!settings.has(Settings.MONGO_HOST)) {
            LOGGER.error("Please insert your mongo host into the settings.");
        } else if (!settings.has(Settings.MONGO_DATABASE)) {
            LOGGER.error("Please insert your mongo database into the settings.");
        } else if (!settings.has(Settings.TOKEN)) {
            LOGGER.error("Please insert your bot's token into the settings.");
        } else if (!DATA_FOLDER.exists() && !DATA_FOLDER.mkdirs()) {
            LOGGER.error("Error creating data folder.");
        } else {
            // Close mongo on shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> mongo.getClient().close()));

            // Create mongo manager
            mongo = new MongoManager(settings.get(Settings.MONGO_HOST), settings.get(Settings.MONGO_DATABASE));

            prefix = settings.get(Settings.PREFIX);

            if (!mongo.isConnected())
                LOGGER.error("Error starting mongo manager.");
            else {
                // Log the client in
                client = new JDABuilder(settings.get(Settings.TOKEN))
                        .setEventManager(new AnnotatedEventManager())
                        .addEventListener(new CommandListener(), this)
                        .build();
            }
        }
    }

    public UserInfo registerUser(long id) {
        UserInfo info = new UserInfo(id);
        info.addCrystals(1000);
        users.put(id, info);
        return info;
    }

    public UserInfo getUser(long id) {
        return users.getOrDefault(id, null);
    }

    public Card getCard(int id) {
        return cards.getOrDefault(id, null);
    }

    public List<Card> getCardsByStar(int star) {
        return cards.values().stream().filter(c -> c.getStar() == star).collect(Collectors.toList());
    }

    public CardEmbed getCardEmbed(int cardId) {
        return cardEmbeds.computeIfAbsent(cardId, id -> {
            try {
                return Util.generateImage(getCard(cardId));
            } catch (IOException error) {
                error.printStackTrace();
                LOGGER.error("Error creating card embed for ID " + id + ".");
                return null;
            }
        });
    }

    public void loadCards() {
        try (FileReader fr = new FileReader(DATA_FOLDER + "\\cards.json")) {
            cards = Arrays.stream(GSON.fromJson(fr, Card[].class))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(Card::getId, c -> c));
            LOGGER.info("Loaded " + cards.size() + " card(s).");
        } catch (IOException error) {
            error.printStackTrace();
            LOGGER.error("Error loading cards.");
        }
    }

    @SubscribeEvent
    public void onReady(ReadyEvent event) {
        loadCards();
        getMongo().loadUsers();

        // Interest checker
        SCHEDULER.scheduleAtFixedRate(() -> getUsers().values().stream()
                .map(UserInfo::getBank)
                .filter(Bank::hasInterest)
                .forEach(Bank::interest), 5, 5, TimeUnit.MINUTES);
    }
}
