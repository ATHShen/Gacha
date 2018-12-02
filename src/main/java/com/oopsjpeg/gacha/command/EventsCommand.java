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
	private final Gacha instance = Gacha.getInstance();
	
	@Override
	public void execute(IMessage message, String alias, String[] args) throws NotOwnerException {
		IChannel channel = message.getChannel();
		IUser author = message.getAuthor();

		if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
			if (!author.equals(instance.getClient().getApplicationOwner()))
				throw new NotOwnerException();
			else instance.loadEvents();
		} else {
			if (instance.getEvents().isEmpty())
				Util.sendError(channel, author, "there are no events on the schedule.");
			else {
				EmbedBuilder builder = new EmbedBuilder();
				builder.withTitle("Event Schedule (in UTC)");
				builder.withColor(Util.getColor(message.getClient().getOurUser(), channel));
				builder.withDesc(EventUtils.listEventsByDate(instance.getEvents().stream()
						.filter(e -> e.getState() != Event.FINISHED)
						.collect(Collectors.toList())));

				Bufferer.sendMessage(channel, "Viewing the event schedule.", builder.build());
			}
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
