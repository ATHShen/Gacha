package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.command.util.Command;
import com.oopsjpeg.gacha.command.util.CommandManager;
import com.oopsjpeg.gacha.object.Card;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

import java.io.IOException;

public class TestCardCommand extends Command {
	public TestCardCommand(CommandManager manager) {
		super(manager, "testcard");
		developerOnly = true;
		registeredOnly = true;
	}

	@Override
	public void execute(Message message, String alias, String[] args) throws IOException {
		MessageChannel channel = message.getChannel();
		Card card = getParent().getData().getCard(Integer.parseInt(args[0]));
		Util.sendCard(channel, message.getAuthor(), card, "");
	}
}
