package com.oopsjpeg.gacha.json;

import com.google.gson.*;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.object.Card;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicInteger;

public class CardSerializer implements JsonDeserializer<Card> {
	private static final AtomicInteger id = new AtomicInteger();

	@Override
	public Card deserialize(JsonElement src, Type typeOfSrc, JsonDeserializationContext context) throws JsonParseException {
		JsonObject json = src.getAsJsonObject();

		Card card = new Card(id.getAndIncrement());
		card.setName(json.get("name").getAsString());
		card.setImage(json.get("image").getAsString());
		if (json.has("source"))
			card.setSource(json.get("source").getAsString());

		card.setStar(json.get("star").getAsInt());
		if (json.has("special"))
			card.setSpecial(json.get("special").getAsBoolean());
		if (json.has("exclusive"))
			card.setExclusive(json.get("exclusive").getAsBoolean());

		card.setBase(json.get("base").getAsInt());
		if (json.has("font"))
			card.setFont(json.get("font").getAsString());
		if (json.has("base_color"))
			card.setBaseColor(Util.stringToColor(json.get("base_color").getAsString()));
		if (json.has("text_color"))
			card.setTextColor(Util.stringToColor(json.get("text_color").getAsString()));

		return card;
	}
}
