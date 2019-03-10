package com.oopsjpeg.gacha.manager;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.util.JSON;
import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.object.user.UserInfo;
import org.bson.Document;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Manages all MongoDB interactions.
 * Created by oopsjpeg on 2/3/2019.
 */
public class MongoManager {
    private final String database;
    private MongoClient client;

    public MongoManager(String host, String database) {
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
        return new File(Gacha.DATA_FOLDER + "\\backups\\gacha_users_" + time.getYear() + "-"
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
                loadUser(d);
            } catch (Exception error) {
                error.printStackTrace();
            }
    }

    public void loadUser(Document d) {
        Gacha.getInstance().getUsers().put(d.getLong("_id"), Gacha.GSON.fromJson(JSON.serialize(d), UserInfo.class));
    }

    public void saveUser(UserInfo u) {
        getUsers().replaceOne(Filters.eq(u.getId()), Document.parse(Gacha.GSON.toJson(u)), new ReplaceOptions().upsert(true));
    }
}
