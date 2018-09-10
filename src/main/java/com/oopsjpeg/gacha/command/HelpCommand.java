package com.oopsjpeg.gacha.command;

import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.handle.obj.IMessage;

public class HelpCommand implements Command {
	@Override
	public void execute(IMessage message, String alias, String[] args) {
		Bufferer.sendMessage(message.getChannel(), "For command help and more information about Gacha: "
				+ "https://discordbots.org/bot/473350175000363018/");
	}

	@Override
	public String getName() {
		return "help";
	}

	@Override
	public String[] getAliases() {
		return new String[]{"?"};
	}
}
