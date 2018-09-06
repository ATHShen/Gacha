package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.data.impl.Quest;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
import java.util.Comparator;
import java.util.stream.Collectors;

public class QuestsCommand implements Command {
	@Override
	public void execute(IMessage message, String alias, String[] args) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.withTitle("Available Quests");
		builder.withColor(Color.PINK);
		builder.withDesc(Gacha.getInstance().getQuests().stream()
				.sorted(Comparator.comparing(Quest::getTitle))
				.map(q -> q.getTitle() + " [`" + q.getID() + "`]")
				.collect(Collectors.joining("\n")));
		Bufferer.sendMessage(message.getChannel(), "Showing available quests.", builder.build());
	}

	@Override
	public String getName() {
		return "quests";
	}
}
