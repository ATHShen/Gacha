package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.handle.obj.IMessage;

public class ReloadCardsCommand implements Command {
	@Override
	public void execute(IMessage message, String alias, String[] args) {
		Gacha.getInstance().loadCards();
		Gacha.getInstance().getCachedCards().clear();
		Bufferer.sendMessage(message.getChannel(), "Reloaded cards.");
	}

	@Override
	public String getName() {
		return "reloadcards";
	}

	@Override
	public boolean isOwnerOnly() {
		return true;
	}
}
