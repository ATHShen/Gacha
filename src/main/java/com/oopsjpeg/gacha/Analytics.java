package com.oopsjpeg.gacha;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Analytics {
	private List<Action> actions = new ArrayList<>();

	public List<Action> getActions() {
		return actions;
	}

	public void setActions(List<Action> actions) {
		this.actions = actions;
	}

	public void addAction(Action action) {
		actions.add(action);
		Gacha.getInstance().getMongo().saveAnalytics();
	}

	public static class Action {
		private final LocalDateTime time;
		private final Object[] data;

		public Action(LocalDateTime time, Object[] data) {
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
