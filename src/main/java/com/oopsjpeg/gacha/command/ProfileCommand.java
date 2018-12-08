package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.user.UserInfo;
import com.oopsjpeg.gacha.util.EventUtils;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.stream.Collectors;

public class ProfileCommand implements Command {
	private final Gacha instance = Gacha.getInstance();

	@Override
	public void execute(IMessage message, String alias, String[] args) {
		IChannel channel = message.getChannel();
		IUser author = message.getAuthor();
		UserInfo info = instance.getOrCreateUser(author);

		EmbedBuilder builder = new EmbedBuilder();

		int star = Collections.max(info.getCards().stream()
				.filter(instance::isCurrentCard)
				.map(Card::getStar)
				.collect(Collectors.toList()));
		builder.withAuthorName(author.getName() + " (" + Util.unformat(Util.star(star)) + ")");
		builder.withAuthorIcon(author.getAvatarURL());
		builder.withThumbnail(author.getAvatarURL());
		builder.withColor(Util.getColor(author, channel));

		// Description
		// Crystals
		builder.appendDesc("**Crystals**: C" + Util.comma(info.getCrystals()) + "\n");
		// Daily
		if (info.hasDaily())
			builder.appendDesc("**Daily** is available.\n");
		else
			builder.appendDesc("**Daily** is available in " + Util.timeDiff(
					LocalDateTime.now(), info.getDailyDate().plusDays(1)) + ".\n");
		// Weekly
		if (info.hasWeekly())
			builder.appendDesc("**Weekly** is available.\n");
		else
			builder.appendDesc("**Weekly** is available in " + Util.timeDiff(
					LocalDateTime.now(), info.getWeeklyDate().plusWeeks(1)) + ".\n");

		// Fields
		// Cards
		builder.appendField("Cards", Util.comma(info.getCards().size()), true);
		// Inbox
		if (!info.getUnseenMail().isEmpty())
			builder.appendField("Inbox", info.getUnseenMail().size() + "", true);
		// CIMGs
		long cimg = instance.getCIMGs().size() - info.getCIMGDatas().stream().filter(c -> !c.canEarn()).count();
		if (cimg > 0) builder.appendField("CIMGs", cimg + "", true);
		// Quests
		long quests = instance.getQuests().size() - info.getQuestDatas().stream().filter(q -> !q.isActive()).count();
		if (quests > 0) builder.appendField("Quests", quests + "", true);
		// VCC
		if (info.getVCC() < EventUtils.vccMax()) {
			float vccLeft = (float) info.getVCC() / EventUtils.vccMax();
			builder.appendField("VCC", Math.round(vccLeft * 100) + "%", true);
		} else if (!info.canStartVCC())
			builder.appendField("VCC Reset", Util.timeDiff(LocalDateTime.now(), info.getVCCDate().plusDays(1)), true);

		Bufferer.sendMessage(channel, "Viewing " + Util.nameThenID(author) + "'s profile.", builder.build());
	}

	@Override
	public String getName() {
		return "profile";
	}

	@Override
	public String getDesc() {
		return "View your profile.";
	}

	@Override
	public String[] getAliases() {
		return new String[]{"account", "me"};
	}
}
