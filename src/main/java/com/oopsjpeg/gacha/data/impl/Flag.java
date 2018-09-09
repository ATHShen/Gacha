package com.oopsjpeg.gacha.data.impl;

public class Flag {
	private final Type type;
	private String desc;

	public Flag(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	@Override
	public String toString() {
		return desc;
	}

	public enum Type {
		EXPLOIT
	}
}
