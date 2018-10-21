package com.oopsjpeg.gacha;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.Mail;
import com.oopsjpeg.gacha.object.user.CIMGData;
import com.oopsjpeg.gacha.object.user.QuestData;
import com.oopsjpeg.gacha.object.user.UserInfo;
import com.oopsjpeg.gacha.object.user.UserMail;
import org.bson.Document;
import sx.blah.discord.handle.obj.IUser;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MongoMaster extends MongoClient {
	private final Gacha instance;
	//private final MongoCollection<Document> main;
	private final MongoCollection<Document> users;

	public MongoMaster(Gacha instance, String database) {
		super();
		this.instance = instance;
		//this.main = getDatabase(database).getCollection("main");
		this.users = getDatabase(database).getCollection("users");
	}

	public boolean isConnected() {
		try {
			getAddress();
			return true;
		} catch (Exception err) {
			return false;
		}
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
				Process pr = rt.exec("mongoexport --db " + Gacha.getInstance().getSettings().getDatabase()
						+ " --collection users --out " + file);
				pr.waitFor();
			}
		} catch (IOException | InterruptedException err) {
			err.printStackTrace();
		}
	}

	public void loadUsers() {
		for (Document d : users.find())
			try {
				loadUser(d.getLong("_id"));
			} catch (Exception err) {
				err.printStackTrace();
			}
	}

	@SuppressWarnings("unchecked")
	public boolean loadUser(long id) {
		Document doc = users.find(Filters.eq(id)).first();
		if (doc == null) return false;

		IUser user = Gacha.getInstance().getClient().getUserByID(id);
		if (user == null || user.isBot()) return false;

		UserInfo info = new UserInfo(doc.getLong("_id"));

		if (doc.containsKey("crystals"))
			info.setCrystals(doc.getInteger("crystals"));

		if (doc.containsKey("cards") && Util.listType(doc.get("cards"), String.class))
			info.setCards(((List<String>) doc.get("cards")).stream()
					.map(instance::getCardByID)
					.filter(Objects::nonNull)
					.collect(Collectors.toList()));

		if (doc.containsKey("mail") && Util.listType(doc.get("mail"), Document.class))
			info.setMail(((List<Document>) doc.get("mail")).stream()
					.map(mailDoc -> {
						UserMail mail = new UserMail();
						mail.setGiftCollected(mailDoc.getBoolean("gift_collected"));
						if (mailDoc.containsKey("link_id"))
							mail.setLinkID(mailDoc.getString("link_id"));
						else {
							if (mailDoc.containsKey("content")) {
								Document contentDoc = (Document) mailDoc.get("content");
								Mail.Content content = new Mail.Content();
								content.setAuthorID(contentDoc.getLong("author_id"));
								content.setSubject(contentDoc.getString("subject"));
								content.setBody(contentDoc.getString("body"));
								mail.setContent(content);
							}
							if (mailDoc.containsKey("gift")) {
								Document giftDoc = (Document) mailDoc.get("gift");
								Mail.Gift gift = new Mail.Gift();
								if (giftDoc.containsKey("crystals"))
									gift.setCrystals(giftDoc.getInteger("crystals"));
								if (giftDoc.containsKey("cards"))
									gift.setCards(((List<String>) giftDoc.get("cards")).stream()
											.map(c -> Gacha.getInstance().getCardByID(c))
											.collect(Collectors.toList()));
								mail.setGift(gift);
							}
						}

						return mail;
					}).collect(Collectors.toList()));

		if (doc.containsKey("mail_notifs"))
			info.setMailNotifs(doc.getBoolean("mail_notifs"));

		if (doc.containsKey("quest_datas") && Util.listType(doc.get("quest_datas"), Document.class))
			info.setQuestDatas(((List<Document>) doc.get("quest_datas")).stream()
					.filter(d -> instance.getQuestByID(d.getString("quest_id")) != null)
					.map(d -> {
						QuestData qd = new QuestData(info, instance.getQuestByID(d.getString("quest_id")));
						if (d.containsKey("progress"))
							qd.setProgress((Map<String, Map<String, Object>>) d.get("progress"));
						if (d.containsKey("active"))
							qd.setActive(d.getBoolean("active"));
						if (d.containsKey("complete_date"))
							qd.setCompleteDate(LocalDateTime.parse(d.getString("complete_date")));
						return qd;
					}).collect(Collectors.toList()));

		if (doc.containsKey("cimg_datas") && Util.listType(doc.get("cimg_datas"), Document.class))
			info.setCIMGDatas(((List<Document>) doc.get("cimg_datas")).stream()
					.map(d -> {
						CIMGData cd = new CIMGData(d.getInteger("group"));
						cd.setMessageID(d.getLong("message_id"));
						cd.setReward(d.getInteger("reward"));
						if (d.containsKey("sent_date"))
							cd.setSentDate(LocalDateTime.parse(d.getString("sent_date")));
						return cd;
					}).collect(Collectors.toList()));

		if (doc.containsKey("daily_date"))
			info.setDailyDate(LocalDateTime.parse(doc.getString("daily_date")));
		if (doc.containsKey("weekly_date"))
			info.setWeeklyDate(LocalDateTime.parse(doc.getString("weekly_date")));
		if (doc.containsKey("report_date"))
			info.setReportDate(LocalDateTime.parse(doc.getString("report_date")));
		if (doc.containsKey("vcc_date"))
			info.setVCCDate(LocalDateTime.parse(doc.getString("vcc_date")));
		if (doc.containsKey("vcc"))
			info.setVCC(doc.getInteger("vcc"));

		if (doc.containsKey("last_save"))
			info.setLastSave(LocalDateTime.parse("last_save"));

		instance.getUsers().remove(info);
		instance.getUsers().add(info);
		return true;
	}

	public void saveUser(UserInfo user) {
		Document doc = new Document("_id", user.getID());

		doc.put("crystals", user.getCrystals());
		doc.put("cards", user.getCards().stream().filter(Objects::nonNull)
				.map(Card::getID).collect(Collectors.toList()));

		doc.put("mail", user.getMail().stream().filter(Objects::nonNull)
				.map(mail -> {
					Document mailDoc = new Document();
					mailDoc.put("gift_collected", mail.isGiftCollected());
					if (mail.getLinkID() != null)
						mailDoc.put("link_id", mail.getLinkID());
					if (mail.getContent() != null) {
						Mail.Content content = mail.getContent();
						Document contentDoc = new Document();
						contentDoc.put("author_id", content.getAuthorID());
						contentDoc.put("subject", content.getSubject());
						contentDoc.put("body", content.getBody());
						mailDoc.put("content", contentDoc);
					}
					if (mail.getGift() != null) {
						Mail.Gift gift = mail.getGift();
						Document giftDoc = new Document();
						giftDoc.put("crystals", gift.getCrystals());
						giftDoc.put("cards", gift.getCards().stream().map(Card::getID).collect(Collectors.toList()));
						mailDoc.put("gift", giftDoc);
					}
					return mailDoc;
				}).collect(Collectors.toList()));

		doc.put("mail_notifs", user.getMailNotifs());

		doc.put("quest_datas", user.getQuestDatas().stream()
				.map(qd -> {
					Document d = new Document();
					d.put("quest_id", qd.getQuest().getID());
					d.put("progress", qd.getProgress());
					d.put("active", qd.isActive());
					if (qd.getCompleteDate() != null)
						d.put("complete_date", qd.getCompleteDate().toString());
					return d;
				}).collect(Collectors.toList()));

		doc.put("cimg_datas", user.getCIMGDatas().stream().filter(Objects::nonNull)
				.map(cd -> {
					Document d = new Document();
					d.put("group", cd.getGroup());
					d.put("message_id", cd.getMessageID());
					d.put("reward", cd.getReward());
					if (cd.getSentDate() != null)
						d.put("sent_date", cd.getSentDate().toString());
					return d;
				}).collect(Collectors.toList()));

		if (user.getDailyDate() != null)
			doc.put("daily_date", user.getDailyDate().toString());
		if (user.getWeeklyDate() != null)
			doc.put("weekly_date", user.getWeeklyDate().toString());
		if (user.getReportDate() != null)
			doc.put("report_date", user.getReportDate().toString());
		if (user.getVCCDate() != null)
			doc.put("vcc_date", user.getVCCDate().toString());
		doc.put("vcc", user.getVCC());

		if (user.getLastSave() != null)
			doc.put("last_save", user.getLastSave().toString());

		users.replaceOne(Filters.eq(user.getID()), doc, new ReplaceOptions().upsert(true));
	}

}
