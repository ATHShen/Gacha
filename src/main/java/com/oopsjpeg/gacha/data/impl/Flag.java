package com.oopsjpeg.gacha.data.impl;

public class Flag {
	private final Type type;
	private final String desc;

	public Flag(Type type, String desc) {
		this.type = type;
		this.desc = desc;
	}

	public Type getType() {
		return type;
	}

	public String getDesc() {
		return desc;
	}

	@Override
	public String toString() {
		return desc;
	}

	public enum Type {
		EXPLOIT
	}
}
