package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Analytics;
import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;

public class AnalyticsCommand implements Command {
	@Override
	public void execute(IMessage message, String alias, String[] args) {
		IChannel channel = message.getChannel();
		Analytics analytics = Gacha.getInstance().getAnalytics();

		EmbedBuilder builder = new EmbedBuilder();
		builder.withTitle("Gacha Analytics");
		builder.withColor(Color.PINK);
		builder.appendDesc("Dailies collected: **" + analytics.getActions("daily").size() + "**\n");
		builder.appendDesc("Cards forged: **" + analytics.getActions("forge").size() + "**\n");
		builder.appendDesc("Cards pulled from gacha: **" + analytics.getActions("gacha").size() + "**\n");
		Bufferer.sendMessage(channel, "Showing analytics.", builder.build());
	}

	@Override
	public String getName() {
		return "analytics";
	}

	@Override
	public boolean isOwnerOnly() {
		return true;
	}
}
