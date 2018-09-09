package com.oopsjpeg.gacha.handler;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.data.DataUtils;
import com.oopsjpeg.gacha.data.EventUtils;
import com.oopsjpeg.gacha.data.QuestUtils;
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
			if (split[0].equals("blackjack.win")) {
				IChannel bjChannel = gacha.getClient().getChannelByID(Long.parseLong(split[1]));
				IUser bjUser = gacha.getClient().getUserByID(Long.parseLong(split[2]));
				UserWrapper info = gacha.getUser(bjUser);

				for (UserWrapper.QuestData data : info.getActiveQuestDatas())
					for (Quest.Condition cond : data.getConditionsByType(Quest.ConditionType.CELESTE_BLACKJACK))
						data.setProgress(cond, 0, DataUtils.getInt(data.getProgress(cond, 0)) + 1);

				gacha.getMongo().saveUser(info);
				QuestUtils.check(bjChannel, bjUser);
			}
		}

		if (!author.isBot()) {
			if (gacha.isCIMG(channel) && message.getAttachments().stream()
					.anyMatch(a -> Util.isImage(a.getFilename())) || message.getEmbeds().stream()
					.anyMatch(e -> (e.getThumbnail() != null && Util.isImage(e.getThumbnail().getUrl())))) {
				UserWrapper info = gacha.getUser(author);
				UserWrapper.CIMGData data = info.getCIMGData(gacha.getCIMGGroup(channel));
				if (data.canEarn()) {
					data.setMessageID(message.getLongID());
					data.setReward(EventUtils.cimg());
					data.setSentDate(LocalDateTime.now());
					info.giveCrystals(data.getReward());
					gacha.getMongo().saveUser(info);
				}
			}
			QuestUtils.check(channel, author);
		}
	}

	@EventSubscriber
	public void onDelete(MessageDeleteEvent evt) {
		IChannel channel = evt.getChannel();

		if (gacha.isCIMG(channel)) {
			IMessage message = evt.getMessage();
			IUser author = evt.getAuthor();
			int group = gacha.getCIMGGroup(channel);
			UserWrapper info = gacha.getUser(author);
			UserWrapper.CIMGData data = info.getCIMGData(group);

			if (data.getMessageID() == message.getLongID()) {
				Bufferer.sendMessage(author.getOrCreatePMChannel(), "Your image in " + channel
						+ " has been deleted, and you have lost **C" + data.getReward() + "**.");
				info.giveCrystals(data.getReward() * -1);
				gacha.getMongo().saveUser(info);
			}
		}
	}
}
