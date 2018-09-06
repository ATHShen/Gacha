package com.oopsjpeg.gacha.data.json;

import com.google.gson.*;
import com.oopsjpeg.gacha.data.impl.Card;

import java.awt.*;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CardSerializer implements JsonDeserializer<Card> {
	@Override
	public Card deserialize(JsonElement src, Type typeOfSrc, JsonDeserializationContext context) throws JsonParseException {
		JsonObject json = src.getAsJsonObject();
		Card card = new Card(json.get("id").getAsString());
		card.setName(json.get("name").getAsString());
		card.setStar(json.get("star").getAsInt());
		if (json.has("gen"))
			card.setGen(json.get("gen").getAsInt());
		if (json.has("special"))
			card.setSpecial(json.get("special").getAsBoolean());
		if (json.has("color"))
			card.setColor(color(json.get("color").getAsString()));
		if (json.has("text_color"))
			card.setTextColor(color(json.get("text_color").getAsString()));
		return card;
	}

	public Color color(String s) {
		List<Float> rgba = Arrays.stream(s.split(","))
				.map(Float::parseFloat).collect(Collectors.toList());
		return new Color(rgba.get(0), rgba.get(1), rgba.get(2), rgba.get(3));
	}
}
