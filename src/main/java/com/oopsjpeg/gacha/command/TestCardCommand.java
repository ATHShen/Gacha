package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

public class TestCardCommand implements Command {
	@Override
	public void execute(IMessage message, String alias, String[] args) {
		IChannel channel = message.getChannel();
		Bufferer.sendFile(channel, "", Gacha.getInstance().getCachedCard(args[0]), "card.png");
	}

	@Override
	public String getName() {
		return "testcard";
	}

	@Override
	public boolean isOwnerOnly() {
		return true;
	}
}
