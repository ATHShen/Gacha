package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.Mail;
import com.oopsjpeg.gacha.object.user.CIMGData;
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
import java.util.List;
import java.util.stream.Collectors;

public class ProfileCommand implements Command {
	private final Gacha instance = Gacha.getInstance();

	@Override
	public void execute(IMessage message, String alias, String[] args) {
		IChannel channel = message.getChannel();
		IUser author = message.getAuthor();
		UserInfo info = instance.getOrCreateUser(author);

		EmbedBuilder builder = new EmbedBuilder();
		builder.withAuthorName(author.getName() + " (" + Util.unformat(Util.star(Collections.max(
				info.getCards().stream().map(Card::getStar).collect(Collectors.toList())))) + ")");
		builder.withAuthorIcon(author.getAvatarURL());
		builder.withColor(Util.getColor(author, channel));

		builder.appendDesc("**Crystals**: C" + Util.comma(info.getCrystals()) + "\n");

		if (info.hasDaily())
			builder.appendDesc("**Daily** is available.\n");
		else
			builder.appendDesc("**Daily** is available in " +
					Util.timeDiff(LocalDateTime.now(), info.getDailyDate().plusDays(1)) + ".\n");

		if (info.hasWeekly())
			builder.appendDesc("**Weekly** is available.\n");
		else
			builder.appendDesc("**Weekly** is available in " +
					Util.timeDiff(LocalDateTime.now(), info.getWeeklyDate().plusWeeks(1)) + ".\n");

		builder.appendField("Cards", Util.comma(info.getCards().size()), true);

		List<Mail> unreadMail = info.getMail().stream()
				.filter(m -> !m.isSeen()).collect(Collectors.toList());
		if (!unreadMail.isEmpty()) builder.appendField("Inbox", unreadMail.size() + "", true);

		if (info.getVCC() < EventUtils.vccMax() && info.getVCC() > 0) {
			float vcc = 100 - (((float) info.getVCC() / EventUtils.vccMax()) * 100);
			builder.appendField("VCC Left", Math.round(vcc) + "%", true);
		} else if (!info.hasVCC())
			builder.appendField("VCC Reset", Util.timeDiff(LocalDateTime.now(), info.getVCCDate().plusDays(1)), true);

		int cimg = instance.getCIMGs().size() - (int) info.getCIMGDatas()
				.stream().filter(CIMGData::canEarn).count();
		if (cimg > 0) builder.appendField("CIMG(s) Left", cimg + "", true);

		int quests = instance.getQuests().size() - (int) info.getQuestDatas()
				.stream().filter(q -> !q.isActive()).count();
		if (quests > 0) builder.appendField("Quest(s) Available", quests + "", true);

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
