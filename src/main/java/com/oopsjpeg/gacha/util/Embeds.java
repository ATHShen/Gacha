package com.oopsjpeg.gacha.util;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.object.CachedCard;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.Mail;
import com.oopsjpeg.gacha.object.Quest;
import com.oopsjpeg.gacha.object.user.QuestData;
import com.oopsjpeg.gacha.object.user.UserInfo;
import com.oopsjpeg.gacha.object.user.UserMail;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.io.IOException;
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

		if (!mail.isSeen()) {
			mail.setSeen(true);
			Gacha.getInstance().getMongo().saveUser(Gacha.getInstance().getUser(user));
		}

		return builder.build();
	}

	public static EmbedObject card(IUser user, Card card) throws IOException {
		EmbedBuilder builder = new EmbedBuilder();
		UserInfo info = Gacha.getInstance().getOrCreateUser(user);
		CachedCard cache = Gacha.getInstance().getCachedCard(card.getID());
		long amount = info.getCards().stream().filter(c -> c.equals(card)).count();

		builder.withColor(cache.getEmbedColor());
		builder.withAuthorName(card.getName() + Util.unformat(" ("
				+ Util.star(card.getStar())) + ") [" + amount + "]");
		builder.withAuthorIcon(user.getAvatarURL());
		builder.withImage("attachment://" + card.getID() + ".png");

		builder.appendDesc("`" + card.getID() + "`");

		return builder.build();
	}

	public static EmbedObject quest(IUser user, IChannel channel, Quest quest) {
		UserInfo info = Gacha.getInstance().getOrCreateUser(user);
		QuestData data = info.getQuestData(quest);
		EmbedBuilder builder = new EmbedBuilder();

		builder.withAuthorIcon(user.getAvatarURL());
		builder.withAuthorName(quest.getTitle());
		builder.withColor(Util.getColor(user, channel));

		builder.appendDesc("Reward: C" + quest.getReward() + "\n");
		if (quest.getInterval() != -1)
			builder.appendDesc("Interval: " + quest.getInterval() + "d\n");
		builder.appendDesc("\n");
		for (Quest.Condition c : quest.getConditions()) {
			if (data.isComplete(c))
				builder.appendDesc("~~" + c.format() + "~~\n");
			else
				builder.appendDesc(c.format() + "\n");
		}

		return builder.build();
	}
}
