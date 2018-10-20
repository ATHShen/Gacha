package com.oopsjpeg.gacha.util;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.object.CachedCard;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.Mail;
import com.oopsjpeg.gacha.object.user.UserInfo;
import com.oopsjpeg.gacha.object.user.UserMail;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.util.stream.Collectors;

public class Embeds {
	public static EmbedObject mail(IUser user, IChannel channel, UserMail mail) {
		IUser mailAuthor = mail.getContent().getAuthor();

		EmbedBuilder builder = new EmbedBuilder();
		builder.withAuthorName("Sent by " + mailAuthor.getName() + "#" + mailAuthor.getDiscriminator());
		builder.withAuthorIcon(mailAuthor.getAvatarURL());
		builder.withTitle(mail.getContent().getSubject());
		builder.withDesc(mail.getContent().getBody());
		builder.withColor(Util.getColor(user, channel));

		if (mail.getGift() != null && !mail.isGiftCollected()) {
			Mail.Gift gift = mail.getGift();
			String giftField = "";
			if (gift.getCrystals() > 0)
				giftField += "Crystals: " + gift.getCrystals() + "\n";
			if (!gift.getCards().isEmpty())
				giftField += "Cards: " + gift.getCards().stream().map(Card::getName)
						.collect(Collectors.joining(", ")) + "\n";
			builder.appendField("Gift", giftField, false);
		}

		return builder.build();
	}

	public static EmbedObject card(IUser user, Card card) {
		EmbedBuilder builder = new EmbedBuilder();
		UserInfo info = Gacha.getInstance().getUser(user);
		CachedCard cache = Gacha.getInstance().getCachedCard(card.getID());
		builder.withColor(cache.getColor());
		builder.withAuthorName(card.getName() + Util.unformat(" (" + Util.star(card.getStar())) + ")");
		builder.withAuthorIcon(user.getAvatarURL());

		long identicals = info.getCards().stream().filter(c -> c.equals(card)).count();
		if (identicals > 0) builder.appendDesc("Identicals: " + identicals + "\n");

		builder.withImage("attachment://" + card.getID() + ".png");

		return builder.build();
	}
}
