package com.oopsjpeg.gacha.wrapper;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.data.DataUtils;
import com.oopsjpeg.gacha.data.EventUtils;
import com.oopsjpeg.gacha.data.impl.Card;
import com.oopsjpeg.gacha.data.impl.Flag;
import com.oopsjpeg.gacha.data.impl.Quest;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class UserWrapper {
	private final long id;

	private int crystals = 1000;
	private List<Card> cards = new ArrayList<>();

	private List<QuestData> questDatas = new ArrayList<>();
	private List<CIMGData> cimgDatas = new ArrayList<>();

	private LocalDateTime dailyDate;
	private LocalDateTime vccDate;
	private int vcc = 0;

	private LocalDateTime lastSave;
	private List<Flag> flags = new ArrayList<>();

	public UserWrapper(long id) {
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

	public void giveCrystals(int crystals) {
		setCrystals(getCrystals() + crystals);
	}

	public void takeCrystals(int crystals) {
		setCrystals(getCrystals() - crystals);
	}

	public List<Card> getCards() {
		return cards;
	}

	public void setCards(List<Card> cards) {
		this.cards = cards;
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
		return questDatas.stream().filter(qd -> quest.getID().equals(qd.questID))
				.findAny().orElseGet(() -> {
					QuestData qd = new QuestData(quest.getID());
					questDatas.add(qd);
					return qd;
				});
	}

	public List<CIMGData> getCIMGDatas() {
		return cimgDatas;
	}

	public void setCIMGDatas(List<CIMGData> cimgDatas) {
		this.cimgDatas = cimgDatas;
	}

	public CIMGData getCIMGData(int group) {
		return cimgDatas.stream().filter(cd -> group == cd.group).findAny().orElseGet(() -> {
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

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof UserWrapper && ((UserWrapper) obj).id == id;
	}

	@Override
	public String toString() {
		return String.valueOf(id);
	}

	public class QuestData {
		private final String questID;
		private boolean active;
		private Map<String, Map<String, Object>> progress = new HashMap<>();
		private LocalDateTime completeDate;

		public QuestData(String questID) {
			this.questID = questID;
		}

		public boolean isComplete() {
			return getQuest() != null && getConditions().stream().allMatch(this::isComplete);
		}

		public boolean isComplete(Quest.Condition cond) {
			switch (cond.getType()) {
				case GACHA_ANY:
				case FORGE_ANY:
				case FORGE_SUCCESS:
				case QUEST_ANY:
				case CELESTE_BLACKJACK:
					return DataUtils.getInt(getProgress(cond, 0)) >= DataUtils.getInt(cond.getData(), 0);
				case CARD_SINGLE:
					return getCards().stream().anyMatch(c -> c.getID().equals(cond.getData()[0]));
				case CARD_AMOUNT:
					return getCards().size() >= DataUtils.getInt(cond.getData(), 0);
				default:
					return false;
			}
		}

		public String getQuestID() {
			return questID;
		}

		public Quest getQuest() {
			return Gacha.getInstance().getQuestByID(questID);
		}

		public List<Quest.Condition> getConditions() {
			return getQuest() != null ? getQuest().getConditions() : new ArrayList<>();
		}

		public List<Quest.Condition> getConditionsByType(Quest.ConditionType type) {
			return getConditions().stream().filter(c -> c.getType() == type).collect(Collectors.toList());
		}

		public boolean isActive() {
			return active;
		}

		public void setActive(boolean active) {
			this.active = active;
		}

		public Map<String, Map<String, Object>> getProgress() {
			return progress;
		}

		public void setProgress(Map<String, Map<String, Object>> progress) {
			this.progress = progress;
		}

		public Map<String, Object> getProgress(Quest.Condition cond) {
			if (!progress.containsKey(cond.getID()))
				progress.put(cond.getID(), new HashMap<>());
			return progress.get(cond.getID());
		}

		public Object getProgress(Quest.Condition cond, int index) {
			return getProgress(cond).getOrDefault(String.valueOf(index), null);
		}

		public void setProgress(Quest.Condition cond, int index, Object value) {
			getProgress(cond).put(String.valueOf(index), value);
		}

		public LocalDateTime getCompleteDate() {
			return completeDate;
		}

		public void setCompleteDate(LocalDateTime completeDate) {
			this.completeDate = completeDate;
		}

		public boolean hasCompleteDate() {
			return completeDate != null;
		}
	}

	public class CIMGData {
		private final int group;
		private long messageID;
		private int reward;
		private LocalDateTime sentDate;

		public CIMGData(int group) {
			this.group = group;
		}

		public boolean canEarn() {
			return sentDate == null || LocalDateTime.now().isAfter(sentDate.plusDays(1));
		}

		public int getGroup() {
			return group;
		}

		public long getMessageID() {
			return messageID;
		}

		public void setMessageID(long messageID) {
			this.messageID = messageID;
		}

		public IMessage getMessage() {
			return Gacha.getInstance().getClient().getMessageByID(messageID);
		}

		public int getReward() {
			return reward;
		}

		public void setReward(int reward) {
			this.reward = reward;
		}

		public LocalDateTime getSentDate() {
			return sentDate;
		}

		public void setSentDate(LocalDateTime sentDate) {
			this.sentDate = sentDate;
		}
	}
}
