package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.data.EventUtils;
import com.oopsjpeg.gacha.data.impl.Event;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
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
			builder.withColor(Color.PINK);
			builder.withDesc(EventUtils.listEventsByDate(events));
			Bufferer.sendMessage(channel, "Viewing current event(s).", builder.build());
		}
	}

	@Override
	public String getName() {
		return "event";
	}
}
