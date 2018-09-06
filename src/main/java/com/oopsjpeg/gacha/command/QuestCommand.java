package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.data.impl.Quest;
import com.oopsjpeg.gacha.wrapper.UserWrapper;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.time.LocalDateTime;
import java.util.List;

public class QuestCommand implements Command {
	@Override
	public void execute(IMessage message, String alias, String[] args) {
		IUser author = message.getAuthor();
		IChannel channel = message.getChannel();
		UserWrapper info = Gacha.getInstance().getUser(message.getAuthor());
		List<Quest> quests = Gacha.getInstance().getQuests();

		if (info.getQuestData() == null && !info.getFlags().isEmpty())
			// Flagged users cannot complete quests
			Util.sendError(channel, author, "you cannot accept quests while flagged.");
		else if (args.length > 0 && args[0].equalsIgnoreCase("view")) {
			// View a quest

			if (quests.isEmpty())
				Util.sendError(channel, author, "there are no quests available to view.");
			else if (args.length < 2)
				Util.sendError(channel, author, "you must specify a quest ID.");
			else {
				Quest quest = Gacha.getInstance().getQuestByID(args[1]);
				Bufferer.sendMessage(channel, "Viewing quest.", quest.embed());
			}
		} else if (args.length > 0 && args[0].equalsIgnoreCase("accept")) {
			// Accept a quest

			if (info.getQuestData() != null)
				// User already has quest
				Util.sendError(channel, author, "you already have a quest.");
			else if (quests.isEmpty())
				// There are no quests
				Util.sendError(channel, author, "there are no quests available to accept.");
			else if (args.length < 2)
				// Quest ID is not specified
				Util.sendError(channel, author, "you must specify a quest ID.");
			else {
				Quest quest = Gacha.getInstance().getQuestByID(args[1]);
				// Quest can only be completed once
				if (quest.getInterval() == -1 && info.getQuestCDs().containsKey(quest.getID()))
					Util.sendError(channel, author, "this quest can only be completed once.");
					// Quest has not yet reached its reset interval
				else if (info.getQuestCD(quest) != null
						&& LocalDateTime.now().isBefore(info.getQuestCD(quest)
						.plusDays(quest.getInterval())))
					Util.sendError(channel, author, "this quest will be available in "
							+ Util.timeDiff(LocalDateTime.now(), info.getQuestCD(quest)
							.plusDays(quest.getInterval())) + ".");
				else {
					// Accept the specified quest
					info.setQuestData(info.new QuestData(quest));
					Bufferer.sendMessage(channel, Util.nameThenID(author) + " accepted a quest.", quest.embed());
					Gacha.getInstance().getMongo().saveUser(info);
				}
			}
		} else if (info.getQuestData() == null)
			// User does not have a quest
			Util.sendError(channel, author, "you do not have an active quest.");
		else if (args.length > 0 && args[0].equalsIgnoreCase("abandon")) {
			info.setQuestData(null);
			Bufferer.sendMessage(channel, Util.nameThenID(author) + " abandoned their quest.");
			Gacha.getInstance().getMongo().saveUser(info);
		} else {
			// View user's current quest
			Bufferer.sendMessage(channel, "Showing " + Util.nameThenID(author) + "'s current quest.",
					info.getQuestData().getQuest().embed());
		}
	}

	@Override
	public String getName() {
		return "quest";
	}
}
