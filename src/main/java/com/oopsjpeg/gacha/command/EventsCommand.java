package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.data.EventUtils;
import com.oopsjpeg.gacha.data.impl.Event;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import com.oopsjpeg.roboops.framework.commands.exception.NotOwnerException;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
import java.util.stream.Collectors;

public class EventsCommand implements Command {
	@Override
	public void execute(IMessage message, String alias, String[] args) throws NotOwnerException {
		if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
			IUser author = message.getAuthor();
			if (!Gacha.getInstance().getClient().getApplicationOwner().equals(author))
				throw new NotOwnerException();
			else
				Gacha.getInstance().loadEvents();
		} else {
			EmbedBuilder builder = new EmbedBuilder();
			builder.withTitle("Gacha Event Schedule");
			builder.withColor(Color.PINK);
			builder.withDesc(EventUtils.listEventsByDate(Gacha.getInstance().getEvents().stream()
					.filter(e -> e.getState() != Event.FINISHED)
					.collect(Collectors.toList())));

			Bufferer.sendMessage(message.getChannel(), "Viewing the event schedule.", builder.build());
		}
	}

	@Override
	public String getName() {
		return "events";
	}

	@Override
	public String getDesc() {
		return "View the event schedule.";
	}

	@Override
	public String[] getAliases() {
		return new String[]{"schedule"};
	}
}
