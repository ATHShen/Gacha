package com.oopsjpeg.gacha.object.user;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.command.util.CommandDialog;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.Quest;
import com.oopsjpeg.gacha.util.Embeds;
import com.oopsjpeg.gacha.util.EventUtils;
import com.oopsjpeg.roboops.framework.Bufferer;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserInfo {
	private final long id;

	private int crystals = 1000;
	private List<Card> cards = new ArrayList<>();

	private List<UserMail> mail = new ArrayList<>();
	private UUID lastMailID;
	private boolean mailNotifs = false;

	private List<QuestData> questDatas = new ArrayList<>();
	private List<CIMGData> cimgDatas = new ArrayList<>();

	private LocalDateTime dailyDate;
	private LocalDateTime weeklyDate;
	private LocalDateTime reportDate;
	private LocalDateTime vccDate;
	private int vcc = 0;

	private LocalDateTime lastSave;
	private List<Flag> flags = new ArrayList<>();

	private CommandDialog dialog;

	public UserInfo(long id) {
		this.id = id;
	}

	public long getID() {
		return id;
	}

	public IUser getUser() {
		return Gacha.getInstance().getClient().getUserByID(id);
	}

	public int getCrystals() {
		return crystals;
	}

	public void setCrystals(int crystals) {
		this.crystals = crystals;
	}

	public void addCrystals(int crystals) {
		setCrystals(getCrystals() + crystals);
	}

	public void subCrystals(int crystals) {
		setCrystals(getCrystals() - crystals);
	}

	public List<Card> getCards() {
		return cards;
	}

	public void setCards(List<Card> cards) {
		this.cards = cards;
	}

	public List<UserMail> getMail() {
		return mail;
	}

	public UserMail getMail(int index) {
		UserMail mail = this.mail.get(index);
		lastMailID = mail.getUUID();
		return mail;
	}

	public void setMail(List<UserMail> mail) {
		this.mail = mail;
	}

	public void sendMail(UserMail mail) {
		this.mail.add(mail);
		if (mailNotifs) {
			IUser user = getUser();
			IChannel channel = user.getOrCreatePMChannel();
			lastMailID = mail.getUUID();
			Bufferer.sendMessage(channel, Util.nameThenID(user) + ", you have received mail.",
					Embeds.mail(user, channel, mail));
		}
	}

	public UUID getLastMailID() {
		return lastMailID;
	}

	public UserMail getLastMail() {
		return mail.stream().filter(m -> m.getUUID().equals(lastMailID)).findAny().orElse(null);
	}

	public void setLastMailID(UUID lastMailID) {
		this.lastMailID = lastMailID;
	}

	public boolean getMailNotifs() {
		return mailNotifs;
	}

	public void setMailNotifs(boolean mailNotifs) {
		this.mailNotifs = mailNotifs;
	}

	public List<QuestData> getQuestDatas() {
		return questDatas;
	}

	public void setQuestDatas(List<QuestData> questDatas) {
		this.questDatas = questDatas;
	}

	public List<QuestData> getActiveQuestDatas() {
		return questDatas.stream().filter(QuestData::isActive).collect(Collectors.toList());
	}

	public QuestData getQuestData(Quest quest) {
		return questDatas.stream()
				.filter(qd -> qd.getQuest().equals(quest))
				.findAny().orElse(null);
	}

	public boolean hasQuestData(Quest quest) {
		return getQuestData(quest) != null;
	}

	public QuestData addQuestData(Quest quest) {
		removeQuestData(quest);
		QuestData qd = new QuestData(this, quest);
		questDatas.add(qd);
		return qd;
	}

	public void removeQuestData(Quest quest) {
		questDatas.removeIf(qd -> qd.getQuest().equals(quest));
	}

	public List<CIMGData> getCIMGDatas() {
		return cimgDatas;
	}

	public void setCIMGDatas(List<CIMGData> cimgDatas) {
		this.cimgDatas = cimgDatas;
	}

	public CIMGData getCIMGData(int group) {
		return cimgDatas.stream().filter(cd -> group == cd.getGroup()).findAny().orElseGet(() -> {
			CIMGData data = new CIMGData(group);
			cimgDatas.add(data);
			return data;
		});
	}

	public LocalDateTime getDailyDate() {
		return dailyDate;
	}

	public void setDailyDate(LocalDateTime dailyDate) {
		this.dailyDate = dailyDate;
	}

	public boolean hasDaily() {
		return dailyDate == null || LocalDateTime.now().isAfter(dailyDate.plusDays(1));
	}

	public LocalDateTime getWeeklyDate() {
		return weeklyDate;
	}

	public void setWeeklyDate(LocalDateTime weeklyDate) {
		this.weeklyDate = weeklyDate;
	}

	public boolean hasWeekly() {
		return weeklyDate == null || LocalDateTime.now().isAfter(weeklyDate.plusWeeks(1));
	}

	public LocalDateTime getReportDate() {
		return reportDate;
	}

	public void setReportDate(LocalDateTime reportDate) {
		this.reportDate = reportDate;
	}

	public boolean hasReport() {
		return reportDate == null || LocalDateTime.now().isAfter(reportDate.plusMinutes(10));
	}

	public LocalDateTime getVCCDate() {
		return vccDate;
	}

	public void setVCCDate(LocalDateTime vccDate) {
		this.vccDate = vccDate;
	}

	public int getVCC() {
		return vcc;
	}

	public void setVCC(int vcc) {
		this.vcc = vcc;
	}

	public boolean hasVCC() {
		return vccDate == null || LocalDateTime.now().isAfter(vccDate.plusDays(1));
	}

	public void vcc() {
		if (hasVCC()) {
			vccDate = LocalDateTime.now();
			vcc = 0;
		}

		if (vcc < EventUtils.vccMax()) {
			int amount = EventUtils.vcc();
			crystals += amount;
			vcc += amount;
			Gacha.getInstance().getMongo().saveUser(this);
		}
	}

	public LocalDateTime getLastSave() {
		return lastSave;
	}

	public void setLastSave(LocalDateTime lastSave) {
		this.lastSave = lastSave;
	}

	public List<Flag> getFlags() {
		return flags;
	}

	public void setFlags(List<Flag> flags) {
		this.flags = flags;
	}

	public CommandDialog getDialog() {
		return dialog;
	}

	public void setDialog(CommandDialog dialog) {
		this.dialog = dialog;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof UserInfo && ((UserInfo) obj).id == id;
	}

	@Override
	public String toString() {
		return String.valueOf(id);
	}
}
