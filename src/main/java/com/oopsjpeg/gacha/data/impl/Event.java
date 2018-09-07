package com.oopsjpeg.gacha.data.impl;

import com.oopsjpeg.gacha.Util;

import java.time.LocalDateTime;
import java.util.Objects;

public class Event {
	private Type type;
	private String message;
	private LocalDateTime startTime;
	private LocalDateTime endTime;

	public String format() {
		return (message != null ? message : type.getText()) + (endTime == null ? ""
				: " [Duration: " + Util.timeDiff(startTime, endTime) + "]");
	}

	public boolean isActive() {
		return !isFinished() && LocalDateTime.now().isAfter(startTime);
	}

	public boolean isFinished() {
		return LocalDateTime.now().isAfter(endTime != null ? endTime : LocalDateTime.now().plusDays(1));
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	@Override
	public String toString() {
		return format();
	}

	public enum Type {
		ANNOUNCE,
		GACHA_DISCOUNT("50% off Gacha (C250)"),
		DOUBLE_GRIND("Double VCC/IMGC Earnings");

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
