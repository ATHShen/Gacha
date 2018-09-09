package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.data.impl.Quest;
import com.oopsjpeg.gacha.wrapper.UserWrapper;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import com.oopsjpeg.roboops.framework.commands.exception.InvalidUsageException;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.time.LocalDateTime;
import java.util.List;

public class QuestCommand implements Command {
	@Override
	public void execute(IMessage message, String alias, String[] args) throws InvalidUsageException {
		IUser author = message.getAuthor();
		IChannel channel = message.getChannel();
		UserWrapper info = Gacha.getInstance().getUser(message.getAuthor());
		List<Quest> quests = Gacha.getInstance().getQuests();

		if (args.length <= 0) throw new InvalidUsageException();

		if (info.getQuestDatas().isEmpty() && !info.getFlags().isEmpty())
			// Flagged users cannot complete quests
			Util.sendError(channel, author, "you cannot accept quests while flagged.");
		else if (args[0].equalsIgnoreCase("view")) {
			// View a quest
			if (quests.isEmpty())
				// There are no quests
				Util.sendError(channel, author, "there are no quests available to view.");
			else if (args.length < 2)
				// Quest ID is not specified
				Util.sendError(channel, author, "you must specify a quest ID.");
			else {
				Quest quest = Gacha.getInstance().getQuestByID(args[1]);
				if (quest == null)
					// Quest ID is invalid
					Util.sendError(channel, author, "invalid quest ID.");
				else
					Bufferer.sendMessage(channel, "Viewing quest.", quest.embed());
			}
		} else if (args[0].equalsIgnoreCase("accept")) {
			// Accept a quest
			if (quests.isEmpty())
				// There are no quests
				Util.sendError(channel, author, "there are no quests available to accept.");
			else if (args.length < 2)
				// Quest ID is not specified
				Util.sendError(channel, author, "you must specify a quest ID.");
			else {
				Quest quest = Gacha.getInstance().getQuestByID(args[1]);
				if (quest == null)
					// Quest ID is invalid
					Util.sendError(channel, author, "invalid quest ID.");
				else {
					UserWrapper.QuestData data = info.getQuestData(quest);
					if (data.isActive())
						// Quest is already active
						Util.sendError(channel, author, "this quest has already been accepted.");
					else if (quest.getInterval() == -1 && data.hasCompleteDate())
						// Quest can only be completed once
						Util.sendError(channel, author, "this quest can only be completed once.");
					else if (data.getCompleteDate() != null
							&& LocalDateTime.now().isBefore(data.getCompleteDate().plusDays(quest.getInterval())))
						// Quest has not yet reached its reset interval
						Util.sendError(channel, author, "this quest will be available in "
								+ Util.timeDiff(LocalDateTime.now(), data.getCompleteDate()
								.plusDays(quest.getInterval())) + ".");
					else {
						// Accept the specified quest
						data.setActive(true);
						Bufferer.sendMessage(channel, Util.nameThenID(author) + " accepted **"
								+ quest.getTitle() + "**.", quest.embed());
						Gacha.getInstance().getMongo().saveUser(info);
					}
				}
			}
		}
	}

	@Override
	public String getName() {
		return "quest";
	}

	@Override
	public String getUsage() {
		return "<view/accept> <id>";
	}
}
