package com.oopsjpeg.gacha.data.json;

import com.google.gson.*;
import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.data.impl.Quest;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class QuestSerializer implements JsonDeserializer<Quest> {
	@Override
	public Quest deserialize(JsonElement src, Type typeOfSrc, JsonDeserializationContext context) throws JsonParseException {
		JsonObject json = src.getAsJsonObject();

		Quest quest = new Quest(json.get("id").getAsString());
		quest.setTitle(json.get("title").getAsString());
		quest.setInterval(json.get("interval").getAsInt());
		quest.setReward(json.get("reward").getAsInt());

		List<Quest.Condition> conds = new ArrayList<>();
		for (JsonElement condElm : json.getAsJsonArray("conditions")) {
			JsonObject condObj = condElm.getAsJsonObject();

			Quest.Condition cond = quest.new Condition(condObj.get("id").getAsString());
			cond.setType(Quest.ConditionType.valueOf(condObj.get("type").getAsString()));
			cond.setData(Gacha.GSON.fromJson(condObj.getAsJsonArray("data"), Object[].class));

			conds.add(cond);
		}
		quest.setConditions(conds);

		return quest;
	}
}
