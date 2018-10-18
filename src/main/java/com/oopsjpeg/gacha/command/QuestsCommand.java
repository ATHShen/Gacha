package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.object.Quest;
import com.oopsjpeg.gacha.object.user.UserInfo;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class QuestsCommand implements Command {
	@Override
	public void execute(IMessage message, String alias, String[] args) {
		IChannel channel = message.getChannel();
		IUser author = message.getAuthor();
		UserInfo info = Gacha.getInstance().getUser(author);
		List<Quest> quests = Gacha.getInstance().getQuests().stream()
				.sorted(Comparator.comparing(Quest::getTitle))
				.collect(Collectors.toList());

		EmbedBuilder builder = new EmbedBuilder();
		builder.withAuthorName(author.getName());
		builder.withAuthorIcon(author.getAvatarURL());
		builder.withColor(Util.getColor(author, channel));

		String active = format(q -> info.hasQuestData(q) && info.getQuestData(q).isActive());
		String available = format(q -> !info.hasQuestData(q) || info.getQuestData(q).canAccept());
		if (!active.isEmpty()) builder.appendField("Active Quests", active, false);
		if (!available.isEmpty()) builder.appendField("Available Quests", available, false);

		Bufferer.sendMessage(channel, "Viewing " + Util.nameThenID(author) + "'s quests.", builder.build());
	}

	@Override
	public String getName() {
		return "quests";
	}

	@Override
	public String getDesc() {
		return "View active and available quests.";
	}

	private String format(Predicate<? super Quest> predicate) {
		return Gacha.getInstance().getQuests().stream().filter(predicate)
				.sorted(Comparator.comparing(Quest::getTitle))
				.map(q -> q.getTitle() + " [`" + q.getID() + "`]")
				.collect(Collectors.joining("\n"));
	}
}
