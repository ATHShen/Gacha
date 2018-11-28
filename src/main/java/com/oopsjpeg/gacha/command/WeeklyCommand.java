package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.object.user.UserInfo;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.time.LocalDateTime;

public class WeeklyCommand implements Command {
	private final Gacha instance = Gacha.getInstance();

	@Override
	public void execute(IMessage message, String alias, String[] args) {
		IChannel channel = message.getChannel();
		IUser author = message.getAuthor();
		UserInfo info = instance.getOrCreateUser(author);

		if (!info.hasWeekly())
			Util.sendError(channel, author, "your **Weekly** is available in "
					+ Util.timeDiff(LocalDateTime.now(), info.getWeeklyDate().plusWeeks(1)) + ".");
		else {
			int amount = 1500;
			info.addCrystals(amount);
			info.setWeeklyDate(LocalDateTime.now());
			Bufferer.sendMessage(channel, Util.nameThenID(author) + " collected **"
					+ Util.comma(amount) + "** from **Weekly**.");
			Gacha.getInstance().getMongo().saveUser(info);
		}
	}

	@Override
	public String getName() {
		return "weekly";
	}

	@Override
	public String getDesc() {
		return "Collect your weekly bonus.";
	}
}