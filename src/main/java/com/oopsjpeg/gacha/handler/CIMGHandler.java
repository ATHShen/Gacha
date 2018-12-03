package com.oopsjpeg.gacha.handler;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.object.user.CIMGData;
import com.oopsjpeg.gacha.object.user.UserInfo;
import com.oopsjpeg.gacha.util.EventUtils;
import com.oopsjpeg.roboops.framework.Bufferer;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageDeleteEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.time.LocalDateTime;

public class CIMGHandler {
	private final Gacha instance = Gacha.getInstance();

	@EventSubscriber
	public void onMessage(MessageReceivedEvent evt) {
		IMessage message = evt.getMessage();
		IChannel channel = evt.getChannel();
		IUser author = evt.getAuthor();

		if (!author.isBot() && instance.isCIMG(channel) && instance.hasUser(author)
				&& (message.getAttachments().stream().anyMatch(a -> Util.isImage(a.getFilename()))
				|| message.getEmbeds().stream().anyMatch(e -> (e.getThumbnail() != null
				&& Util.isImage(e.getThumbnail().getUrl()))))) {
			UserInfo info = instance.getUser(author);
			CIMGData data = info.getCIMGData(instance.getCIMGGroup(channel));
			if (data.canEarn()) {
				data.setMessageID(message.getLongID());
				data.setReward(EventUtils.cimg());
				data.setSentDate(LocalDateTime.now());
				info.addCrystals(data.getReward());
				instance.getMongo().saveUser(info);
			}
		}
	}

	@EventSubscriber
	public void onDelete(MessageDeleteEvent evt) {
		IMessage message = evt.getMessage();
		IChannel channel = evt.getChannel();
		IUser author = evt.getAuthor();

		if (instance.isCIMG(channel) && instance.hasUser(author)) {
			int group = instance.getCIMGGroup(channel);
			UserInfo info = instance.getUser(author);
			CIMGData data = info.getCIMGData(group);

			if (data.getMessageID() == message.getLongID()) {
				Bufferer.sendMessage(author.getOrCreatePMChannel(), "Your image in " + channel
						+ " has been deleted, and you have lost **C" + data.getReward() + "**.");
				info.subCrystals(data.getReward());
				instance.getMongo().saveUser(info);
			}
		}
	}
}
