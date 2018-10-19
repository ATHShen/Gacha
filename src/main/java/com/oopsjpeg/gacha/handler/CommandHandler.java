package com.oopsjpeg.gacha.handler;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.object.user.UserInfo;
import com.oopsjpeg.roboops.framework.commands.CommandCenter;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class CommandHandler extends CommandCenter {
	public CommandHandler(String prefix) {
		super(prefix);
	}

	@Override
	@EventSubscriber
	public void onMessage(MessageReceivedEvent evt) {
		UserInfo info = Gacha.getInstance().getUser(evt.getAuthor());
		if (info != null && info.getDialog() == null) super.onMessage(evt);
	}
}
