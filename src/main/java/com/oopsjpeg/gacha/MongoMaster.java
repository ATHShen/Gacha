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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MongoMaster extends MongoClient {
	//private final MongoCollection<Document> main;
	private final MongoCollection<Document> users;

	public MongoMaster(String database) {
		super();
		//this.main = getDatabase(database).getCollection("main");
		this.users = getDatabase(database).getCollection("users");
	}

	@SuppressWarnings("unchecked")
	public boolean loadUser(long id) {
		Document doc = users.find(Filters.eq(id)).first();
		if (doc == null) return false;

		UserWrapper user = new UserWrapper(doc.getLong("_id"));

		if (doc.containsKey("crystals"))
			user.setCrystals(doc.getInteger("crystals"));

		if (doc.containsKey("cards") && Util.listType(doc.get("cards"), String.class))
			user.setCards(((List<String>) doc.get("cards")).stream()
					.map(s -> Gacha.getInstance().getCardByID(s))
					.filter(Objects::nonNull)
					.collect(Collectors.toList()));

		if (doc.containsKey("quest_datas") && Util.listType(doc.get("quest_datas"), Document.class))
			user.setQuestDatas(((List<Document>) doc.get("quest_datas")).stream()
					.map(d -> {
						UserWrapper.QuestData qd = user.new QuestData(d.getString("quest_id"));
						if (d.containsKey("active"))
							qd.setActive(d.getBoolean("active"));
						qd.setProgress((Map<String, Map<String, Object>>) d.get("progress"));
						if (d.containsKey("complete_date"))
							qd.setCompleteDate(LocalDateTime.parse(d.getString("complete_date")));
						return qd;
					}).collect(Collectors.toList()));

		if (doc.containsKey("cimg_datas") && Util.listType(doc.get("cimg_datas"), Document.class))
			user.setCIMGDatas(((List<Document>) doc.get("cimg_datas")).stream()
					.map(d -> {
						UserWrapper.CIMGData cd = user.new CIMGData(d.getInteger("group"));
						cd.setMessageID(d.getLong("message_id"));
						cd.setReward(d.getInteger("reward"));
						if (d.containsKey("sent_date"))
							cd.setSentDate(LocalDateTime.parse(d.getString("sent_date")));
						return cd;
					}).collect(Collectors.toList()));

		if (doc.containsKey("daily_date"))
			user.setDailyDate(LocalDateTime.parse(doc.getString("daily_date")));
		if (doc.containsKey("vcc_date"))
			user.setVCCDate(LocalDateTime.parse(doc.getString("vcc_date")));
		if (doc.containsKey("vcc"))
			user.setVCC(doc.getInteger("vcc"));

		if (doc.containsKey("last_save"))
			user.setLastSave(LocalDateTime.parse("last_save"));
		if (doc.containsKey("flags") && Util.listType(doc.get("flags"), Document.class))
			user.setFlags(((List<Document>) doc.get("flags")).stream()
					.map(d -> {
						Flag flag = new Flag(Flag.Type.valueOf(d.getString("type")));
						flag.setDesc(d.getString("desc"));
						return flag;
					}).collect(Collectors.toList()));

		Gacha.getInstance().getUsers().remove(user);
		Gacha.getInstance().getUsers().add(user);
		return true;
	}

	public void saveUser(UserWrapper user) {
		Document doc = new Document("_id", user.getID());

		doc.put("crystals", user.getCrystals());
		doc.put("cards", user.getCards().stream().map(Card::getID).collect(Collectors.toList()));

		doc.put("quest_datas", user.getQuestDatas().stream()
				.filter(qd -> Gacha.getInstance().getQuestByID(qd.getQuestID()) != null)
				.map(qd -> {
					Document d = new Document("quest_id", qd.getQuestID());
					d.put("active", qd.isActive());
					d.put("progress", qd.getProgress());
					if (qd.getCompleteDate() != null)
						d.put("complete_date", qd.getCompleteDate().toString());
					return d;
				}).collect(Collectors.toList()));

		doc.put("cimg_datas", user.getCIMGDatas().stream()
				.map(cd -> {
					Document d = new Document("group", cd.getGroup());
					d.put("message_id", cd.getMessageID());
					d.put("reward", cd.getReward());
					if (cd.getSentDate() != null)
						d.put("sent_date", cd.getSentDate().toString());
					return d;
				}).collect(Collectors.toList()));

		if (user.getDailyDate() != null)
			doc.put("daily_date", user.getDailyDate().toString());
		if (user.getVCCDate() != null)
			doc.put("vcc_date", user.getVCCDate().toString());
		doc.put("vcc", user.getVCC());

		if (user.getLastSave() != null)
			doc.put("last_save", user.getLastSave().toString());

		doc.put("flags", user.getFlags().stream()
				.map(f -> {
					Document d = new Document("type", f.getType());
					d.put("desc", f.getDesc());
					return d;
				}).collect(Collectors.toList()));

		users.replaceOne(Filters.eq(user.getID()), doc, new ReplaceOptions().upsert(true));
	}

}
