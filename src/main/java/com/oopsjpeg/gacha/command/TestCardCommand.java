package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

import java.io.IOException;

public class TestCardCommand implements Command {
	@Override
	public void execute(IMessage message, String alias, String[] args) throws IOException {
		IChannel channel = message.getChannel();
		Card card = Gacha.getInstance().getCardByID(args[0]);
		Util.sendCard(channel, message.getAuthor(), card, "");
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
