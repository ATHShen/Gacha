package com.oopsjpeg.gacha;

import com.oopsjpeg.gacha.data.impl.Card;
import sx.blah.discord.handle.obj.IUser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Analytics {
	private List<Action> actions = new ArrayList<>();

	public List<Action> getActions() {
		return actions;
	}

	public List<Action> getActions(String type) {
		return actions.stream()
				.filter(a -> a.data[0] instanceof String && a.data[0].equals(type))
				.collect(Collectors.toList());
	}

	public void setActions(List<Action> actions) {
		this.actions = actions;
	}

	public void addAction(Action action) {
		actions.add(action);
		Gacha.getInstance().getMongo().saveAnalytics();
	}

	public void addDailyAction(IUser user, int amount) {
		addAction(new Action(LocalDateTime.now(), "daily", user.getLongID(), amount));
	}

	public void addForgeAction(IUser user, Card[] input, Card output) {
		addAction(new Action(LocalDateTime.now(), "forge", user.getLongID(),
				Arrays.stream(input).map(Card::getID), output.getID()));
	}

	public void addGachaAction(IUser user, int cost, Card output) {
		addAction(new Action(LocalDateTime.now(), "gacha", user.getLongID(), cost, output.getID()));
	}

	public static class Action {
		private final LocalDateTime time;
		private final Object[] data;

		public Action(LocalDateTime time, Object... data) {
			this.time = time;
			this.data = data;
		}

		public LocalDateTime getTime() {
			return time;
		}

		public Object[] getData() {
			return data;
		}
	}
}
