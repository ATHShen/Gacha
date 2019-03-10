package com.oopsjpeg.gacha.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Settings;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.json.CardSerializer;
import com.oopsjpeg.gacha.manager.MongoManager;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.UserInfo;
import org.bson.Document;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by oopsjpeg on 3/9/2019.
 */
public class VersionConverter {
    static MongoManager mongo;
    static JsonArray oldCardsJson;

    static Map<Integer, Card> cards;

    public static void main(String[] args) {
        Gson gson = new GsonBuilder().registerTypeAdapter(Card.class, new CardSerializer()).create();
        Settings settings = new Settings("gacha.properties");
        mongo = new MongoManager(settings.get(Settings.MONGO_HOST), settings.get(Settings.MONGO_DATABASE));

        try (FileReader fr = new FileReader(Gacha.DATA_FOLDER + "\\cards.json")) {
            cards = Arrays.stream(gson.fromJson(fr, Card[].class))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(Card::getId, c -> c));
        } catch (IOException error) {
            error.printStackTrace();
        }

        try (FileReader fr = new FileReader(Gacha.DATA_FOLDER + "\\cards_old.json")) {
            oldCardsJson = gson.fromJson(fr, JsonArray.class);

            for (Document d : mongo.getUsers().find())
                try {
                    convertToNew(d);
                } catch (Exception error) {
                    error.printStackTrace();
                }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void convertToNew(Document d) {
        UserInfo u = new UserInfo(d.getLong("_id"));
        u.setCrystals(d.getInteger("crystals"));

        List<Integer> newCards = new ArrayList<>();
        List<String> oldCards = d.getList("cards", String.class);

        if (oldCards != null) {
            for (String id : oldCards) {
                int add;
                switch (id) {
                    case "g1_fate_jalter_alpha":
                        add = 16;
                        break;
                    case "g1_fate_jalter_beta":
                        add = 15;
                        break;
                    case "g1_fate_jalter_gamma":
                        add = 17;
                        break;
                    case "g1_fate_merlin":
                        add = 26;
                        break;
                    case "g1_fate_nero":
                        add = 27;
                        break;
                    case "g1_fate_scathach_alpha":
                        add = 31;
                        break;
                    case "g1_fate_scathach_beta":
                        add = 32;
                        break;
                    case "g1_th_youmu_alpha":
                        add = 41;
                        break;
                    case "g1_th_youmu_beta":
                        add = 40;
                        break;
                    case "g1_th_youmu_gamma":
                        add = 39;
                        break;
                    case "g2_ditf_02":
                        add = 42;
                        break;
                    case "g2_dr_chiaki":
                        add = 8;
                        break;
                    case "g2_dm_hestia":
                        add = 43;
                        break;
                    case "g2_dr_kaede":
                        add = 28;
                        break;
                    default:
                        List<Card> cas = cards.values().stream().filter(c -> c.getStar() == getOldCardStar(id)).collect(Collectors.toList());
                        add = cas.get(Util.RANDOM.nextInt(cas.size())).getId();
                }
                newCards.add(add);
                System.out.println(id + " : becomes " + cards.get(add).getName());
            }
            u.setCardIds(newCards);
        }

        mongo.getUsers().replaceOne(Filters.eq(u.getId()), Document.parse(Gacha.GSON.toJson(u)), new ReplaceOptions().upsert(true));
    }

    public static int getOldCardStar(String id) {
        for (JsonElement obj : oldCardsJson) {
            if (obj.getAsJsonObject().get("id").getAsString().equals(id))
                return obj.getAsJsonObject().get("star").getAsInt();
        }
        return 1;
    }
}
