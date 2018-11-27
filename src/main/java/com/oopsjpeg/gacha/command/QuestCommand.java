package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.object.Quest;
import com.oopsjpeg.gacha.object.user.UserInfo;
import com.oopsjpeg.gacha.util.Embeds;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class QuestCommand implements Command {
	private final Gacha instance = Gacha.getInstance();
	
	@Override
	public void execute(IMessage message, String alias, String[] args) {
		IUser author = message.getAuthor();
		IChannel channel = message.getChannel();
		UserInfo info = instance.getUser(message.getAuthor());

		if (args.length == 1) {
			Quest quest = instance.getQuestByID(args[0]);
			if (quest == null)
				// Quest ID is invalid
				Util.sendError(channel, author, "that is not a valid quest ID.");
			else
				Bufferer.sendMessage(channel, "Viewing quest.", Embeds.quest(author, channel, quest));
		} else {
			List<Quest> quests = instance.getQuests().stream()
					.filter(q -> info.getQuestData(q).isActive())
					.sorted(Comparator.comparing(Quest::getTitle))
					.collect(Collectors.toList());

			if (quests.isEmpty())
				Util.sendError(channel, author, "you do not have any available quests.");
			else {
				EmbedBuilder builder = new EmbedBuilder();
				builder.withAuthorName(author.getName());
				builder.withAuthorIcon(author.getAvatarURL());
				builder.withColor(Util.getColor(author, channel));

				String available = quests.stream()
						.map(q -> q.getTitle() + " [`" + q.getID() + "`]")
						.collect(Collectors.joining("\n"));
				builder.appendField("Available Quests", available, false);

				Bufferer.sendMessage(channel, "Viewing " + Util.nameThenID(author) + "'s quests.", builder.build());
			}
		}
	}

	@Override
	public String getName() {
		return "quest";
	}

	@Override
	public String getUsage() {
		return "id";
	}

	@Override
	public String getDesc() {
		return "View available quests or view a specified quest.";
	}

	@Override
	public String[] getAliases() {
		return new String[]{"quests"};
	}
}
