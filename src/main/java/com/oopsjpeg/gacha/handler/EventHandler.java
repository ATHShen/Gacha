package com.oopsjpeg.gacha.handler;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.data.DataUtils;
import com.oopsjpeg.gacha.data.impl.Quest;
import com.oopsjpeg.gacha.wrapper.UserWrapper;
import com.oopsjpeg.roboops.framework.Bufferer;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageDeleteEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EventHandler {
	private static final List<String> games = new ArrayList<>();

	static {
		games.addAll(Arrays.asList(
				"KanColle",
				"Girls' Frontline",
				"Azur Lane",
				"Danganronpa: Trigger Happy Havoc",
				"Danganronpa 2: Goodbye Despair",
				"Danganronpa 1.2: Reload",
				"Danganronpa V3: Killing Harmony",
				"Danganronpa Another Episode: Ultra Despair Girls",
				"Touhou 6: Embodiment of Scarlet Devil",
				"Touhou 7: Perfect Cherry Blossom",
				"Touhou 8: Imperishable Night",
				"Touhou 9: Phantasmagoria of Flower View",
				"Touhou 10: Mountain of Faith",
				"Touhou 11: Subterranean Animism",
				"Touhou 12: Undefined Fantastic Object",
				"Touhou 13: Ten Desires",
				"Touhou 14: Double Dealing Character",
				"Touhou 15: Legacy of Lunatic Kingdom",
				"Touhou 16: Hidden Star in Four Seasons"
		));
	}

	private final Gacha gacha;

	public EventHandler(Gacha gacha) {
		this.gacha = gacha;
	}

	@EventSubscriber
	public void onReady(ReadyEvent evt) {
		gacha.postBuild();

		Gacha.SCHEDULER.scheduleAtFixedRate(() ->
				evt.getClient().changePresence(StatusType.ONLINE, ActivityType.PLAYING,
						games.get(Util.RANDOM.nextInt(games.size()))), 0, 10, TimeUnit.MINUTES);
	}

	@EventSubscriber
	public void onMessage(MessageReceivedEvent evt) {
		IUser author = evt.getAuthor();
		IChannel channel = evt.getChannel();
		IMessage message = evt.getMessage();

		if (channel.equals(gacha.getConnector())) {
			String[] split = message.getContent().split(";");
			if (split[0].equalsIgnoreCase("blackjack.win")) {
				IChannel bjChannel = gacha.getClient().getChannelByID(Long.parseLong(split[1]));
				IUser bjUser = gacha.getClient().getUserByID(Long.parseLong(split[2]));
				UserWrapper info = gacha.getUser(bjUser);

				if (info.getQuestData() != null) {
					UserWrapper.QuestData data = info.getQuestData();
					for (Quest.Condition cond : data.getQuest().getConditions()) {
						if (cond.getType() == Quest.ConditionType.CELESTE_BLACKJACK)
							data.setProgress(cond, 0, DataUtils.getInt(data.getProgress(cond, 0)) + 1);
					}
					checkQuest(bjChannel, bjUser);
					gacha.getMongo().saveUser(info);
				}
			}
		}

		if (!author.isBot()) {
			// CIMG earnings
			if (gacha.isCimg(channel) && message.getAttachments().stream().anyMatch(a -> Util.isImage(a.getFilename()))
					|| message.getEmbeds().stream().anyMatch(e ->
					(e.getThumbnail() != null && Util.isImage(e.getThumbnail().getUrl())))) {
				UserWrapper info = gacha.getUser(author);
				int group = gacha.getCimgGroup(channel);
				if (info.getCimgData(group).canEarn()) {
					info.getCimgData(group).setMessageID(message.getLongID());
					info.getCimgData(group).setTime(LocalDateTime.now());
					info.giveCrystals(250);
					Gacha.getInstance().getMongo().saveUser(info);
				}
			}
			checkQuest(channel, author);
		}
	}

	@EventSubscriber
	public void onDelete(MessageDeleteEvent evt) {
		IChannel channel = evt.getChannel();

		if (gacha.isCimg(channel)) {
			IMessage message = evt.getMessage();
			IUser author = evt.getAuthor();
			UserWrapper info = gacha.getUser(author);
			int group = gacha.getCimgGroup(channel);

			if (info.getCimgData(gacha.getCimgGroup(channel)).getMessageID() == message.getLongID()) {
				Bufferer.sendMessage(channel, "Your image in " + channel
						+ " has been deleted, and you have lost **C250**.");
				info.giveCrystals(-250);
				gacha.getMongo().saveUser(info);
			}
		}
	}

	private void checkQuest(IChannel channel, IUser user) {
		UserWrapper info = gacha.getUser(user);
		UserWrapper.QuestData quest = info.getQuestData();
		if (quest != null && quest.isComplete()) {
			Bufferer.sendMessage(channel, Util.nameThenID(user) + " completed their quest and earned **C"
					+ quest.getQuest().getReward() + "**.");
			info.giveCrystals(quest.getQuest().getReward());
			info.getQuestCDs().put(quest.getQuest().getID(), LocalDateTime.now());
			info.setQuestData(null);
			gacha.getMongo().saveUser(info);
		}
	}
}
