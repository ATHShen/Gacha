package com.oopsjpeg.gacha.manager;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.object.CachedCard;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.user.UserInfo;
import net.dv8tion.jda.core.events.ReadyEvent;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by oopsjpeg on 3/2/2019.
 */
public class DataManager extends Manager {
    private List<UserInfo> users = new ArrayList<>();

    private List<Card> cards = new ArrayList<>();
    private List<CachedCard> cachedCards = new ArrayList<>();

    public DataManager(Gacha parent) {
        super(parent);
    }

    @Override
    public void onReady(ReadyEvent event) {
        loadCards();
        getParent().getMongo().loadUsers();
    }

    public List<UserInfo> getUsers() {
        return users;
    }

    public UserInfo getUser(long id) {
        return users.stream().filter(u -> u.getId() == id).findAny().orElse(null);
    }

    public void addUser(UserInfo info) {
        users.removeIf(u -> u.getId() == info.getId());
        users.add(info);
    }

    public boolean hasUser(long id) {
        return users.stream().anyMatch(u -> u.getId() == id);
    }

    public UserInfo registerUser(long id) {
        UserInfo info = new UserInfo(id);
        addUser(info);
        return info;
    }

    public List<Card> getCards() {
        return cards;
    }

    public Card getCard(int id) {
        return cards.stream().filter(c -> c.getId() == id).findAny().orElse(null);
    }

    public List<Card> getCardsByStar(int star) {
        return cards.stream().filter(c -> c.getStar() >= star).collect(Collectors.toList());
    }

    public List<CachedCard> getCachedCards() {
        return cachedCards;
    }

    public CachedCard getCachedCard(int id) {
        if (cachedCards.stream().noneMatch(c -> c.getId() == id)) {
            try {
                cachedCards.add(Util.generateImage(getCard(id)));
            } catch (IOException error) {
                Gacha.LOGGER.error("Failed to create cached card for ID " + id + ".");
                error.printStackTrace();
            }
        }
        return cachedCards.stream().filter(c -> c.getId() == id).findAny().orElse(null);
    }

    public void loadCards() {
        File file = new File(Gacha.getDataFolder() + "\\cards.json");
        if (file.exists()) {
            try (FileReader fr = new FileReader(file)) {
                cards = Arrays.stream(Gacha.GSON.fromJson(fr, Card[].class))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                Gacha.LOGGER.info("Loaded " + cards.size() + " card(s).");
            } catch (IOException error) {
                error.printStackTrace();
                Gacha.LOGGER.error("Error loading cards.");
            }
        }
    }
}
