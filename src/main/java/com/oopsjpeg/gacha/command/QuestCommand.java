package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.object.Quest;
import com.oopsjpeg.gacha.object.user.QuestData;
import com.oopsjpeg.gacha.object.user.UserInfo;
import com.oopsjpeg.gacha.util.Embeds;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import com.oopsjpeg.roboops.framework.commands.exception.InvalidUsageException;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

public class QuestCommand implements Command {
	private final Gacha instance = Gacha.getInstance();
	
	@Override
	public void execute(IMessage message, String alias, String[] args) throws InvalidUsageException {
		IUser author = message.getAuthor();
		IChannel channel = message.getChannel();
		UserInfo info = instance.getUser(message.getAuthor());
		List<Quest> quests = instance.getQuests();

		if (args.length <= 0) throw new InvalidUsageException();

		if (quests.isEmpty())
			Util.sendError(channel, author, "there are no quests available.");
		else if (args[0].equalsIgnoreCase("view")) {
			if (args.length < 2)
				// Quest ID is not specified
				Util.sendError(channel, author, "you must specify a quest ID.");
			else {
				Quest quest = instance.getQuestByID(args[1]);
				if (quest == null)
					// Quest ID is invalid
					Util.sendError(channel, author, "invalid quest ID.");
				else
					Bufferer.sendMessage(channel, "Viewing quest.", Embeds.quest(author, channel, quest));
			}
		} else if (args[0].equalsIgnoreCase("accept")) {
			if (args.length < 2)
				// Quest ID is not specified
				Util.sendError(channel, author, "you must specify a quest ID.");
			else {
				Quest quest = instance.getQuestByID(args[1]);
				if (quest == null)
					// Quest ID is invalid
					Util.sendError(channel, author, "invalid quest ID.");
				else {
					QuestData data = info.getQuestData(quest);
					if (data == null) acceptQuest(message, quest);
					else if (data.isActive())
						// Quest is already active
						Util.sendError(channel, author, "this quest is already active.");
					else if (!data.canAccept()) {
						if (quest.getInterval() == -1)
							// Quest can only be completed once
							Util.sendError(channel, author, "this quest can only be completed once.");
						else
							// Quest has not yet been reset
							Util.sendError(channel, author, "this quest will be available in "
									+ Util.timeDiff(LocalDateTime.now(), data.getCompleteDate()
									.plusDays(quest.getInterval())) + ".");
					} else acceptQuest(message, quest);
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
		return "\"view\"/\"accept\" id";
	}

	@Override
	public String getDesc() {
		return "View/accept a specified quest.";
	}

	private void acceptQuest(IMessage message, Quest quest) {
		IUser author = message.getAuthor();
		IChannel channel = message.getChannel();
		UserInfo info = instance.getUser(author);

		QuestData data = info.addQuestData(quest);
		data.setActive(true);
		data.setProgress(new HashMap<>());
		Bufferer.sendMessage(channel, Util.nameThenID(author) + " accepted **"
				+ quest.getTitle() + "**.", Embeds.quest(author, channel, quest));

		instance.getMongo().saveUser(info);
	}
}
