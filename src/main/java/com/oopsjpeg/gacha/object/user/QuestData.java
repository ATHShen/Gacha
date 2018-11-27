package com.oopsjpeg.gacha.object.user;

import com.oopsjpeg.gacha.object.Quest;
import com.oopsjpeg.gacha.util.DataUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QuestData {
	private final UserInfo userInfo;
	private final Quest quest;
	private Map<String, Map<String, Object>> progress = new HashMap<>();
	private LocalDateTime completeDate;

	public QuestData(UserInfo userInfo, Quest quest) {
		this.userInfo = userInfo;
		this.quest = quest;
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public Quest getQuest() {
		return quest;
	}

	public List<Quest.Condition> getConditions() {
		return quest.getConditions();
	}

	public List<Quest.Condition> getConditionsByType(Quest.ConditionType type) {
		return getConditions().stream().filter(c -> c.getType() == type).collect(Collectors.toList());
	}

	public boolean isComplete() {
		return completeDate != null || getConditions().stream().allMatch(this::isComplete);
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
				return userInfo.getCards().stream().anyMatch(c -> c.getID().equals(cond.getData()[0]));
			case CARD_AMOUNT:
				return userInfo.getCards().size() >= DataUtils.getInt(cond.getData(), 0);
			default:
				return false;
		}
	}

	public Map<String, Map<String, Object>> getProgress() {
		return progress;
	}

	public Map<String, Object> getProgress(Quest.Condition cond) {
		if (!progress.containsKey(cond.getID()))
			progress.put(cond.getID(), new HashMap<>());
		return progress.get(cond.getID());
	}

	public Object getProgress(Quest.Condition cond, int index) {
		return getProgress(cond).getOrDefault(String.valueOf(index), null);
	}

	public void setProgress(Map<String, Map<String, Object>> progress) {
		this.progress = progress;
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

	public boolean isActive() {
		return (completeDate == null || (getQuest().getInterval() != -1
				&& LocalDateTime.now().isAfter(completeDate.plusDays(getQuest().getInterval()))));
	}
}