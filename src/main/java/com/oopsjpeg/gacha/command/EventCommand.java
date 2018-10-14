package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.object.Event;
import com.oopsjpeg.gacha.util.EventUtils;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

import java.util.List;

public class EventCommand implements Command {
	@Override
	public void execute(IMessage message, String alias, String[] args) {
		IChannel channel = message.getChannel();
		List<Event> events = EventUtils.activeEvents();
		if (events.isEmpty())
			Bufferer.sendMessage(channel, "There are no events today.");
		else {
			EmbedBuilder builder = new EmbedBuilder();
			builder.withColor(Util.getColor(message.getClient().getOurUser(), channel));
			builder.withDesc(EventUtils.listEventsByDate(events));
			Bufferer.sendMessage(channel, "Viewing current event(s).", builder.build());
		}
	}

	@Override
	public String getName() {
		return "event";
	}

	@Override
	public String getDesc() {
		return "View current events.";
	}
}
