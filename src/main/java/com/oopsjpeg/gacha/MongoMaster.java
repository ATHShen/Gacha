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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MongoMaster extends MongoClient {
	private final MongoCollection<Document> users;

	public MongoMaster(String database) {
		super();
		this.users = getDatabase(database).getCollection("users");
	}

	public boolean loadUser(long id) {
		Document d = users.find(Filters.eq(id)).first();
		if (d == null) return false;

		UserWrapper uw = inUser(d);
		if (uw == null) return false;

		Gacha.getInstance().getUsers().remove(uw);
		Gacha.getInstance().getUsers().add(uw);
		return true;
	}

	public void saveUser(UserWrapper u) {
		users.replaceOne(Filters.eq(u.getID()), outUser(u), new ReplaceOptions().upsert(true));
	}

	@SuppressWarnings("unchecked")
	public UserWrapper inUser(Document d) {
		if (d == null) return null;

		UserWrapper u = new UserWrapper(d.getLong("_id"));

		u.setCrystals(d.getInteger("crystals"));
		if (d.containsKey("cards"))
			u.setCards(((List<String>) d.getOrDefault("cards", new ArrayList<>()))
					.stream().map(s -> Gacha.getInstance().getCardByID(s))
					.collect(Collectors.toList()));

		if (d.containsKey("quest_data")) {
			Document qdObj = (Document) d.get("quest_data");
			if (!qdObj.isEmpty()) {
				UserWrapper.QuestData questData = u.new QuestData(
						Gacha.getInstance().getQuestByID(qdObj.getString("quest")));
				questData.setProgress((Map<String, Map<String, Object>>) qdObj.get("progress"));
				u.setQuestData(questData);
			}
		}
		if (d.containsKey("quest_cds"))
			u.setQuestCDs(((Map<String, String>) d.get("quest_cds")).entrySet().stream()
					.collect(Collectors.toMap(Map.Entry::getKey, e -> LocalDateTime.parse(e.getValue()))));

		if (d.containsKey("daily"))
			u.setDaily(LocalDateTime.parse(d.getString("daily")));
		if (d.containsKey("vc_date"))
			u.setVcDate(LocalDateTime.parse(d.getString("vc_date")));
		if (d.containsKey("vc_crystals"))
			u.setVcCrystals(d.getInteger("vc_crystals"));

		if (d.containsKey("last_save"))
			u.setLastSave(LocalDateTime.parse("last_save"));
		if (d.containsKey("flags"))
			u.setFlags(((List<Document>) d.getOrDefault("flags", new ArrayList<>()))
					.stream().map(flag -> new Flag(Flag.Type.valueOf(flag.getString("type")), flag.getString("desc")))
					.collect(Collectors.toList()));

		return u;
	}

	public Document outUser(UserWrapper u) {
		Document doc = new Document("_id", u.getID());

		doc.put("crystals", u.getCrystals());
		doc.put("cards", u.getCards().stream().map(Card::getID)
				.collect(Collectors.toList()));

		Document qdDoc = new Document();
		if (u.getQuestData() != null) {
			qdDoc.put("quest", u.getQuestData().getQuest().getID());
			qdDoc.put("progress", u.getQuestData().getProgress());
		}
		doc.put("quest_data", qdDoc);

		doc.put("quest_cds", u.getQuestCDs().entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString())));

		if (u.getDaily() != null)
			doc.put("daily", u.getDaily().toString());
		if (u.getVcDate() != null)
			doc.put("vc_date", u.getVcDate().toString());
		doc.put("vc_crystals", u.getVcCrystals());

		if (u.getLastSave() != null)
			doc.put("last_save", u.getLastSave().toString());
		doc.put("flags", u.getFlags().stream().map(f -> {
			Document d = new Document();
			d.put("type", f.getType());
			d.put("desc", f.getDesc());
			return d;
		}).collect(Collectors.toList()));

		return doc;
	}

}
