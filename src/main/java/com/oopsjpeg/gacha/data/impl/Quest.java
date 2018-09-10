package com.oopsjpeg.gacha.data.impl;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.data.DataUtils;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Quest {
	public static final long ONCE = -1;
	public static final long DAILY = 1;
	public static final long WEEKLY = 7;

	private final String id;
	private String title;
	private long interval;
	private int reward;
	private List<Condition> conditions = new ArrayList<>();

	public Quest(String id) {
		this.id = id;
	}

	public String format() {
		String output = "**" + title + "**\n";

		if (interval != -1)
			output += "Interval: " + interval + "d\n";

		output += "Reward: C" + reward + "\n\n";

		for (Condition c : conditions)
			output += "- " + c.format() + "\n";

		return output;
	}

	public EmbedObject embed() {
		return new EmbedBuilder().withColor(Color.PINK).withDesc(format()).build();
	}

	public String getID() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public int getReward() {
		return reward;
	}

	public void setReward(int reward) {
		this.reward = reward;
	}

	public List<Condition> getConditions() {
		return conditions;
	}

	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Quest && ((Quest) obj).id.equalsIgnoreCase(id);
	}

	@Override
	public String toString() {
		return title;
	}

	public enum ConditionType {
		GACHA_ANY {
			@Override
			public String format(Object[] data) {
				return "Pull **" + DataUtils.getInt(data, 0) + "** card(s) from Gacha.";
			}
		},

		FORGE_ANY {
			@Override
			public String format(Object[] data) {
				return "Forge **" + DataUtils.getInt(data, 0) + "** card(s).";
			}
		},
		FORGE_SUCCESS {
			@Override
			public String format(Object[] data) {
				return "Forge **" + DataUtils.getInt(data, 0) + "** card(s) of the above tier.";
			}
		},

		QUEST_ANY {
			@Override
			public String format(Object[] data) {
				return "Complete **" + DataUtils.getInt(data, 0) + "** quest(s).";
			}
		},

		CARD_SINGLE {
			@Override
			public String format(Object[] data) {
				return "Own **" + Gacha.getInstance().getCardByID(
						DataUtils.getString(data, 0)).getName() + "**.";
			}
		},
		CARD_AMOUNT {
			@Override
			public String format(Object[] data) {
				return "Own **" + DataUtils.getInt(data, 0) + "** card(s).";
			}
		},

		CELESTE_BLACKJACK {
			@Override
			public String format(Object[] data) {
				return "Win **" + DataUtils.getInt(data, 0) + "** game(s) of Blackjack.";
			}
		};

		public abstract String format(Object[] data);
	}

	public class Condition {
		private final String id;
		private ConditionType type;
		private Object[] data;

		public Condition(String id) {
			this.id = id;
		}

		public String format() {
			return type.format(data);
		}

		public String getID() {
			return id;
		}

		public ConditionType getType() {
			return type;
		}

		public void setType(ConditionType type) {
			this.type = type;
		}

		public Object[] getData() {
			return data;
		}

		public void setData(Object[] data) {
			this.data = data;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(id);
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof Condition && ((Condition) obj).id.equalsIgnoreCase(id);
		}

		@Override
		public String toString() {
			return id;
		}
	}
}
