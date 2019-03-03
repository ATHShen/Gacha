package com.oopsjpeg.gacha.manager;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.user.UserInfo;
import org.bson.Document;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Manages all MongoDB interactions.
 * Created by oopsjpeg on 2/3/2019.
 */
public class MongoManager extends Manager {
    private MongoClient client;
    private final String database;

    public MongoManager(Gacha parent, String host, String database) {
        super(parent);
        this.database = database;
        client = new MongoClient(host);

        // Set up the backup timer
        Gacha.SCHEDULER.scheduleAtFixedRate(this::backup, 0, 1, TimeUnit.HOURS);
    }

    public boolean isConnected() {
        try {
            client.getAddress();
            return true;
        } catch (Exception error) {
            return false;
        }
    }

    public MongoClient getClient() {
        return client;
    }

    public MongoDatabase getDatabase() {
        return client.getDatabase(database);
    }

    public MongoCollection<Document> getUsers() {
        return getDatabase().getCollection("users");
    }

    public File getBackupFile(LocalDateTime time) {
        return new File(Gacha.getDataFolder() + "\\backups\\gacha_users_" + time.getYear() + "-"
                + time.getMonthValue() + "-" + time.getDayOfMonth() + "-"
                + time.getHour() + ".json");
    }

    public void backup() {
        File file = getBackupFile(LocalDateTime.now());
        if (!file.exists()) forceBackup(file);
    }

    public void forceBackup(File file) {
        try {
            if (isConnected()) {
                Runtime rt = Runtime.getRuntime();
                Process pr = rt.exec("mongoexport --db " + database + " --collection users --out " + file);
                pr.waitFor();
            }
        } catch (IOException | InterruptedException error) {
            error.printStackTrace();
        }
    }

    public void loadUsers() {
        for (Document d : getUsers().find())
            try {
                loadUser(d.getLong("_id"));
            } catch (Exception error) {
                error.printStackTrace();
            }
    }

    @SuppressWarnings("unchecked")
    public boolean loadUser(long id) {
        Document doc = getUsers().find(Filters.eq(id)).first();
        if (doc == null) return false;

        UserInfo info = new UserInfo(doc.getLong("_id"));

        if (doc.containsKey("crystals"))
            info.setCrystals(doc.getInteger("crystals"));
        if (doc.containsKey("card_ids"))
            info.setCards(((List<Integer>) doc.get("card_ids")).stream()
                    .map(cid -> getParent().getData().getCard(cid))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));

        if (doc.containsKey("daily_date"))
            info.setDailyDate(LocalDateTime.parse(doc.getString("daily_date")));
        if (doc.containsKey("weekly_date"))
            info.setWeeklyDate(LocalDateTime.parse(doc.getString("weekly_date")));

        if (doc.containsKey("last_save"))
            info.setLastSave(LocalDateTime.parse("last_save"));

        getParent().getData().addUser(info);
        return true;
    }

    public void saveUser(UserInfo user) {
        Document doc = new Document("_id", user.getId());

        doc.put("crystals", user.getCrystals());
        doc.put("card_ids", user.getCards().stream()
                .map(Card::getId)
                .collect(Collectors.toList()));

        if (user.getDailyDate() != null)
            doc.put("daily_date", user.getDailyDate().toString());
        if (user.getWeeklyDate() != null)
            doc.put("weekly_date", user.getWeeklyDate().toString());
        if (user.getLastSave() != null)
            doc.put("last_save", user.getLastSave().toString());

        getUsers().replaceOne(Filters.eq(user.getId()), doc, new ReplaceOptions().upsert(true));
    }
}
