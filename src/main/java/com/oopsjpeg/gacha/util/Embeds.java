package com.oopsjpeg.gacha.util;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.object.CachedCard;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.user.UserInfo;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;

public class Embeds {
	public static MessageEmbed card(User user, Card card) {
		EmbedBuilder builder = new EmbedBuilder();
		UserInfo info = Gacha.getInstance().getData().getUser(user.getIdLong());
		CachedCard cache = Gacha.getInstance().getData().getCachedCard(card.getId());
		long amount = info.getCards().stream().filter(c -> c.equals(card)).count();

		builder.setColor(cache.getEmbedColor());
		builder.setAuthor(card.getName() + " (" + Util.star(card.getStar()) + ") [" + card.getId() + "]", card.getSource(), user.getAvatarUrl());
		builder.setImage("attachment://" + card.getId() + ".png");

		return builder.build();
	}
}
