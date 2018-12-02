package com.oopsjpeg.gacha.handler;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.object.Quest;
import com.oopsjpeg.gacha.object.user.QuestData;
import com.oopsjpeg.gacha.object.user.UserInfo;
import com.oopsjpeg.gacha.util.DataUtils;
import com.oopsjpeg.gacha.util.QuestUtils;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class QuestHandler {
	private final Gacha instance = Gacha.getInstance();

	@EventSubscriber
	public void onMessage(MessageReceivedEvent evt) {
		IMessage message = evt.getMessage();
		IUser author = evt.getAuthor();
		IChannel channel = evt.getChannel();
		String content = message.getContent();

		if (author.isBot()) return;

		if (instance.hasUser(author))
			QuestUtils.check(channel, author);

		if (channel.equals(instance.getConnector())) {
			String[] split = content.split(";");
			if (split[0].equals("blackjack.win")) {
				IChannel bjChannel = instance.getClient().getChannelByID(Long.parseLong(split[1]));
				IUser bjUser = instance.getClient().getUserByID(Long.parseLong(split[2]));

				if (instance.hasUser(bjUser)) {
					UserInfo info = instance.getUser(bjUser);

					for (QuestData data : info.getActiveQuestDatas())
						for (Quest.Condition cond : data.getConditionsByType(Quest.ConditionType.CELESTE_BLACKJACK))
							data.setProgress(cond, 0, DataUtils.getInt(data.getProgress(cond, 0)) + 1);

					instance.getMongo().saveUser(info);
					QuestUtils.check(bjChannel, bjUser);
				}
			}
		}
	}
}
