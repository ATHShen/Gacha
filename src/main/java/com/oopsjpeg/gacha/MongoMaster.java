package com.oopsjpeg.gacha;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.oopsjpeg.gacha.data.impl.Card;
import com.oopsjpeg.gacha.data.impl.Flag;
import com.oopsjpeg.gacha.wrapper.UserWrapper;
import org.bson.Document;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class MongoMaster extends MongoClient {
	private final MongoCollection<Document> main;
	private final MongoCollection<Document> users;

	public MongoMaster(String database) {
		super();
		this.main = getDatabase(database).getCollection("main");
		this.users = getDatabase(database).getCollection("users");
	}

	@SuppressWarnings("unchecked")
	public void loadAnalytics() {
		Document doc = main.find(Filters.eq("analytics")).first();
		if (doc == null)
			Gacha.getInstance().setAnalytics(new Analytics());
		else {
			Analytics analytics = new Analytics();

			if (doc.containsKey("actions"))
				analytics.setActions(((List<Document>) doc.get("actions"))
						.stream().map(d -> new Analytics.Action(
								LocalDateTime.parse(d.getString("time")),
								((List<Object>) d.get("data")).toArray(new Object[0])))
						.collect(Collectors.toList()));

			Gacha.getInstance().setAnalytics(analytics);
		}

	}

	public void saveAnalytics() {
		Document doc = new Document("_id", "analytics");
		Analytics analytics = Gacha.getInstance().getAnalytics();

		doc.put("actions", analytics.getActions().stream().map(a -> {
			Document d = new Document();
			d.put("time", a.getTime().toString());
			d.put("data", Arrays.asList(a.getData()));
			return d;
		}).collect(Collectors.toList()));

		main.replaceOne(Filters.eq("analytics"), doc, new ReplaceOptions().upsert(true));
	}

	@SuppressWarnings("unchecked")
	public boolean loadUser(long id) {
		Document doc = users.find(Filters.eq(id)).first();
		if (doc == null) return false;

		UserWrapper u = new UserWrapper(doc.getLong("_id"));

		u.setCrystals(doc.getInteger("crystals"));
		if (doc.containsKey("cards"))
			u.setCards(((List<String>) doc.getOrDefault("cards", new ArrayList<>()))
					.stream().map(s -> Gacha.getInstance().getCardByID(s))
					.collect(Collectors.toList()));

		if (doc.containsKey("quest_data")) {
			Document qdObj = (Document) doc.get("quest_data");
			if (!qdObj.isEmpty()) {
				UserWrapper.QuestData questData = u.new QuestData(
						Gacha.getInstance().getQuestByID(qdObj.getString("quest")));
				questData.setProgress((Map<String, Map<String, Object>>) qdObj.get("progress"));
				u.setQuestData(questData);
			}
		}
		if (doc.containsKey("quest_cds"))
			u.setQuestCDs(((Map<String, String>) doc.get("quest_cds")).entrySet().stream()
					.collect(Collectors.toMap(Map.Entry::getKey, e -> LocalDateTime.parse(e.getValue()))));

		if (doc.containsKey("daily"))
			u.setDaily(LocalDateTime.parse(doc.getString("daily")));
		if (doc.containsKey("vc_date"))
			u.setVcDate(LocalDateTime.parse(doc.getString("vc_date")));
		if (doc.containsKey("vc_crystals"))
			u.setVcCrystals(doc.getInteger("vc_crystals"));

		if (doc.containsKey("cimg_datas")) {
			Map<Integer, UserWrapper.CimgData> cds = new HashMap<>();
			Map<String, Document> cdDocs = (Map<String, Document>) doc.get("cimg_datas");

			for (Map.Entry<String, Document> cdEnt : cdDocs.entrySet()) {
				Document cdDoc = cdEnt.getValue();
				UserWrapper.CimgData cd = u.new CimgData();
				if (cdDoc.containsKey("message_id"))
					cd.setMessageID(cdDoc.getLong("message_id"));
				if (cdDoc.containsKey("time"))
					cd.setTime(LocalDateTime.parse(cdDoc.getString("time")));
				if (cdDoc.containsKey("reward"))
					cd.setReward(cdDoc.getInteger("reward"));
				cds.put(Integer.parseInt(cdEnt.getKey()), cd);
			}

			u.setCimgDatas(cds);
		}

		if (doc.containsKey("last_save"))
			u.setLastSave(LocalDateTime.parse("last_save"));
		if (doc.containsKey("flags"))
			u.setFlags(((List<Document>) doc.getOrDefault("flags", new ArrayList<>()))
					.stream().map(flag -> new Flag(Flag.Type.valueOf(flag.getString("type")), flag.getString("desc")))
					.collect(Collectors.toList()));

		Gacha.getInstance().getUsers().remove(u);
		Gacha.getInstance().getUsers().add(u);
		return true;
	}

	public void saveUser(UserWrapper u) {
		Document doc = new Document("_id", u.getID());

		doc.put("crystals", u.getCrystals());
		doc.put("cards", u.getCards().stream().map(Card::getID)
				.collect(Collectors.toList()));

		if (u.getQuestData() != null) {
			Document qdDoc = new Document();
			qdDoc.put("quest", u.getQuestData().getQuest().getID());
			qdDoc.put("progress", u.getQuestData().getProgress());
			doc.put("quest_data", qdDoc);
		}
		doc.put("quest_cds", u.getQuestCDs().entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString())));

		if (u.getDaily() != null)
			doc.put("daily", u.getDaily().toString());
		if (u.getVcDate() != null)
			doc.put("vc_date", u.getVcDate().toString());
		doc.put("vc_crystals", u.getVcCrystals());

		doc.put("cimg_datas", u.getCimgDatas().entrySet().stream().collect(
				Collectors.toMap(e -> String.valueOf(e.getKey()), e -> {
					Document d = new Document();
					if (e.getValue().getMessageID() != -1)
						d.put("message_id", e.getValue().getMessageID());
					if (e.getValue().getTime() != null)
						d.put("time", e.getValue().getTime().toString());
					if (e.getValue().getReward() != -1)
						d.put("reward", e.getValue().getReward());
					return d;
				})));

		if (u.getLastSave() != null)
			doc.put("last_save", u.getLastSave().toString());
		doc.put("flags", u.getFlags().stream().map(f -> {
			Document d = new Document();
			d.put("type", f.getType());
			d.put("desc", f.getDesc());
			return d;
		}).collect(Collectors.toList()));

		users.replaceOne(Filters.eq(u.getID()), doc, new ReplaceOptions().upsert(true));
	}

}
