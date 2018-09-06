package com.oopsjpeg.gacha.wrapper;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.data.DataUtils;
import com.oopsjpeg.gacha.data.impl.Card;
import com.oopsjpeg.gacha.data.impl.Flag;
import com.oopsjpeg.gacha.data.impl.Quest;
import sx.blah.discord.handle.obj.IUser;

import java.time.LocalDateTime;
import java.util.*;

public class UserWrapper {
	private final long id;

	private int crystals = 1000;
	private List<Card> cards = new ArrayList<>();

	private QuestData questData;
	private Map<String, LocalDateTime> questCDs = new HashMap<>();

	private LocalDateTime daily;
	private LocalDateTime vcDate;
	private int vcCrystals = 0;

	private Map<String, LocalDateTime> cimgCDs = new HashMap<>();

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

	public List<Card> getCards() {
		return cards;
	}

	public void setCards(List<Card> cards) {
		this.cards = cards;
	}

	public QuestData getQuestData() {
		return questData;
	}

	public void setQuestData(QuestData questData) {
		this.questData = questData;
	}

	public Map<String, LocalDateTime> getQuestCDs() {
		return questCDs;
	}

	public void setQuestCDs(Map<String, LocalDateTime> questCDs) {
		this.questCDs = questCDs;
	}

	public LocalDateTime getQuestCD(Quest quest) {
		return questCDs.getOrDefault(quest.getID(), null);
	}

	public LocalDateTime getDaily() {
		return daily;
	}

	public void setDaily(LocalDateTime daily) {
		this.daily = daily;
	}

	public boolean hasDaily() {
		return daily == null || LocalDateTime.now().isAfter(daily.plusDays(1));
	}

	public int getVcCrystals() {
		return vcCrystals;
	}

	public void setVcCrystals(int vcCrystals) {
		this.vcCrystals = vcCrystals;
	}

	public LocalDateTime getVcDate() {
		return vcDate;
	}

	public void setVcDate(LocalDateTime vcDate) {
		this.vcDate = vcDate;
	}

	public void vcc() {
		if (vcDate == null || LocalDateTime.now().isAfter(vcDate.plusDays(1))) {
			vcDate = LocalDateTime.now();
			vcCrystals = 0;
		}

		if (vcCrystals < 1500) {
			int crys = Util.nextInt(6, 8);
			crystals += crys;
			vcCrystals += crys;
			Gacha.getInstance().getMongo().saveUser(this);
		}
	}

	public Map<String, LocalDateTime> getCimgCDs() {
		return cimgCDs;
	}

	public void setCImgCDs(Map<String, LocalDateTime> cimgCDs) {
		this.cimgCDs = cimgCDs;
	}

	public LocalDateTime getCimgCD(int group) {
		return questCDs.getOrDefault(String.valueOf(group), null);
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
		return (obj instanceof UserWrapper && ((UserWrapper) obj).id == id)
				|| (obj instanceof IUser && ((IUser) obj).getLongID() == id);
	}

	@Override
	public String toString() {
		return String.valueOf(id);
	}

	public class QuestData {
		private final Quest quest;
		private Map<String, Map<String, Object>> progress = new HashMap<>();

		public QuestData(Quest quest) {
			this.quest = quest;
		}

		public boolean isComplete() {
			return quest.getConditions().stream().allMatch(this::isComplete);
		}

		public boolean isComplete(Quest.Condition cond) {
			switch (cond.getType()) {
				case CARD_SINGLE:
					return getCards().stream().anyMatch(c -> c.getID().equals(cond.getData()[0]));
				case CARD_AMOUNT:
					return getCards().size() >= DataUtils.getInt(cond.getData(), 0);
				case CELESTE_BLACKJACK:
					return DataUtils.getInt(getProgress(cond, 0)) >= DataUtils.getInt(cond.getData(), 0);
				default:
					return false;
			}
		}

		public Quest getQuest() {
			return quest;
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
	}
}
