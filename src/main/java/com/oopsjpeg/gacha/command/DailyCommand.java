package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.wrapper.UserWrapper;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.time.LocalDateTime;

public class DailyCommand implements Command {
	@Override
	public void execute(IMessage message, String alias, String[] args) {
		IChannel channel = message.getChannel();
		IUser author = message.getAuthor();
		UserWrapper info = Gacha.getInstance().getUser(author);

		if (!info.hasDaily())
			Util.sendError(channel, author, "your **Daily** is available in "
					+ Util.timeDiff(LocalDateTime.now(), info.getDailyDate().plusDays(1)) + ".");
		else {
			int amount = 500;
			info.giveCrystals(amount);
			info.setDailyDate(LocalDateTime.now());
			Bufferer.sendMessage(channel, Util.nameThenID(author) + " collected **C500** from **Daily**.");
			Gacha.getInstance().getMongo().saveUser(info);
		}
	}

	@Override
	public String getName() {
		return "daily";
	}

	@Override
	public String getDesc() {
		return "Collect your daily bonus.";
	}
}
