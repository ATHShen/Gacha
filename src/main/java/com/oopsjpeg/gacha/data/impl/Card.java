package com.oopsjpeg.gacha.data.impl;

import java.awt.*;
import java.util.Objects;

public class Card {
	private final String id;
	private String name;
	private int star;
	private int gen = 1;
	private boolean special;
	private Color color;
	private Color textColor;

	public Card(String id) {
		this.id = id;
	}

	public String getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getStar() {
		return star;
	}

	public void setStar(int star) {
		this.star = star;
	}

	public int getGen() {
		return gen;
	}

	public void setGen(int gen) {
		this.gen = gen;
	}

	public boolean isSpecial() {
		return special;
	}

	public void setSpecial(boolean special) {
		this.special = special;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getTextColor() {
		return textColor;
	}

	public void setTextColor(Color textColor) {
		this.textColor = textColor;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Card && ((Card) obj).id.equals(id);
	}

	@Override
	public String toString() {
		return name;
	}
}
