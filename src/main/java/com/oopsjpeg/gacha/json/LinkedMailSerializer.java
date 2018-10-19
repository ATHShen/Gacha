package com.oopsjpeg.gacha.json;

import com.google.gson.*;
import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.Mail;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LinkedMailSerializer implements JsonDeserializer<Mail> {
	@Override
	public Mail deserialize(JsonElement src, Type typeOfSrc, JsonDeserializationContext context) throws JsonParseException {
		JsonObject json = src.getAsJsonObject();
		Mail mail = new Mail();

		if (json.has("content")) {
			JsonObject contentObj = json.getAsJsonObject("content");
			Mail.Content content = new Mail.Content();
			content.setAuthorID(contentObj.get("author_id").getAsLong());
			content.setSubject(contentObj.get("subject").getAsString());
			content.setBody(contentObj.get("body").getAsString());
			mail.setContent(content);
		}

		if (json.has("gift")) {
			JsonObject giftObj = json.getAsJsonObject("gift");
			Mail.Gift gift = new Mail.Gift();
			if (giftObj.has("crystals"))
				gift.setCrystals(giftObj.get("crystals").getAsInt());
			if (giftObj.has("cards")) {
				List<Card> cards = new ArrayList<>();
				for (JsonElement e : giftObj.getAsJsonArray("cards"))
					cards.add(Gacha.getInstance().getCardByID(e.getAsString()));
				gift.setCards(cards);
			}
			mail.setGift(gift);
		}

		return mail;
	}
}
