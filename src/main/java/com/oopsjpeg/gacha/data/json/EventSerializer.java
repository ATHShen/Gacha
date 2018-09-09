package com.oopsjpeg.gacha.data.json;

import com.google.gson.*;
import com.oopsjpeg.gacha.data.impl.Event;

import java.lang.reflect.Type;
import java.time.LocalDateTime;

public class EventSerializer implements JsonDeserializer<Event> {
	@Override
	public Event deserialize(JsonElement src, Type typeOfSrc, JsonDeserializationContext context) throws JsonParseException {
		JsonObject json = src.getAsJsonObject();

		Event event = new Event(Event.Type.valueOf(json.get("type").getAsString()));
		if (json.has("message"))
			event.setMessage(json.get("message").getAsString());
		event.setStartDate(LocalDateTime.parse(json.get("start_date").getAsString()));
		if (json.has("end_date"))
			event.setEndDate(LocalDateTime.parse(json.get("end_date").getAsString()));

		return event;
	}
}
