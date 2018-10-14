package com.oopsjpeg.gacha.json;

import com.google.gson.*;
import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.object.Quest;

import java.lang.reflect.Type;

public class QuestSerializer implements JsonDeserializer<Quest> {
	@Override
	public Quest deserialize(JsonElement src, Type typeOfSrc, JsonDeserializationContext context) throws JsonParseException {
		JsonObject json = src.getAsJsonObject();

		Quest quest = new Quest(json.get("id").getAsString());
		quest.setTitle(json.get("title").getAsString());
		quest.setInterval(json.get("interval").getAsInt());
		quest.setReward(json.get("reward").getAsInt());

		for (JsonElement condElm : json.getAsJsonArray("conditions")) {
			JsonObject condObj = condElm.getAsJsonObject();

			Quest.Condition cond = quest.new Condition(condObj.get("id").getAsString());
			cond.setType(Quest.ConditionType.valueOf(condObj.get("type").getAsString()));
			cond.setData(Gacha.GSON.fromJson(condObj.getAsJsonArray("data"), Object[].class));

			quest.getConditions().add(cond);
		}

		return quest;
	}
}
