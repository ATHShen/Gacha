package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.object.Event;
import com.oopsjpeg.gacha.util.EventUtils;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import com.oopsjpeg.roboops.framework.commands.exception.NotOwnerException;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

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
			IChannel channel = message.getChannel();
			EmbedBuilder builder = new EmbedBuilder();
			builder.withTitle("Gacha Event Schedule");
			builder.withColor(Util.getColor(message.getClient().getOurUser(), channel));
			builder.withDesc(EventUtils.listEventsByDate(Gacha.getInstance().getEvents().stream()
					.filter(e -> e.getState() != Event.FINISHED)
					.collect(Collectors.toList())));

			Bufferer.sendMessage(channel, "Viewing the event schedule.", builder.build());
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
