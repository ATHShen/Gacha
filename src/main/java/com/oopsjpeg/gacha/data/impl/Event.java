package com.oopsjpeg.gacha.data.impl;

import com.oopsjpeg.gacha.Util;

import java.time.LocalDateTime;

public class Event {
	public static final int SCHEDULED = 0;
	public static final int STARTING = 1;
	public static final int ACTIVE = 2;
	public static final int FINISHED = 3;

	private final Type type;
	private String message;
	private LocalDateTime startDate;
	private LocalDateTime endDate;

	public Event(Type type) {
		this.type = type;
	}

	public String format() {
		return (message != null ? message : type.getText())
				+ (endDate == null ? "" : " [Duration: " + Util.timeDiff(startDate, endDate) + "]")
				+ (getState() != STARTING ? "" : " (Starting in " + Util.timeDiff(LocalDateTime.now(), startDate) + ")");
	}

	public Type getType() {
		return type;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getState() {
		if (LocalDateTime.now().isAfter(endDate)) return FINISHED;
		else if (LocalDateTime.now().isAfter(startDate)) return ACTIVE;
		else if (LocalDateTime.now().isAfter(startDate.minusDays(1))) return STARTING;
		else return SCHEDULED;
	}

	public LocalDateTime getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDateTime startDate) {
		this.startDate = startDate;
	}

	public LocalDateTime getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
	}

	@Override
	public String toString() {
		return format();
	}

	public enum Type {
		NONE,
		GACHA_DISCOUNT("25% off Gacha"),
		DOUBLE_GRIND("Double VCC/CIMG");

		private final String text;

		Type() {
			this("");
		}

		Type(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}
	}
}
