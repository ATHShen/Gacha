package com.oopsjpeg.gacha.object.user;

import com.oopsjpeg.gacha.object.Card;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserInfo {
	private final long id;

	private int crystals = 1000;
	private List<Card> cards = new ArrayList<>();

	private LocalDateTime dailyDate;
	private LocalDateTime weeklyDate;

	private LocalDateTime lastSave;

	public UserInfo(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public int getCrystals() {
		return crystals;
	}

	public void setCrystals(int crystals) {
		this.crystals = crystals;
	}

	public void addCrystals(int crystals) {
		this.crystals += crystals;
	}

	public void removeCrystals(int crystals) {
		this.crystals -= crystals;
	}

	public List<Card> getCards() {
		return cards;
	}

	public void setCards(List<Card> cards) {
		this.cards = cards;
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

	public LocalDateTime getLastSave() {
		return lastSave;
	}

	public void setLastSave(LocalDateTime lastSave) {
		this.lastSave = lastSave;
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
