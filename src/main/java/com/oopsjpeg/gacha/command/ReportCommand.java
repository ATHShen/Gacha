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

public class ReportCommand implements Command {
	private final Gacha instance = Gacha.getInstance();

	@Override
	public void execute(IMessage message, String alias, String[] args) {
		IChannel channel = message.getChannel();
		IUser author = message.getAuthor();
		UserInfo info = instance.getOrCreateUser(author);

		if (!info.hasReport())
			Util.sendError(channel, author, "you recently submitted a report, and you must wait 10 minutes in between reports.");
		else if (args.length <= 0)
			Util.sendError(channel, author, "you must enter a message for your report.");
		else {
			String report = String.join(" ", args);
			if (report.length() < 10)
				Util.sendError(channel, author, "your report must be detailed, containing at least 10 characters.");
			else {
				IUser owner = message.getClient().getApplicationOwner();
				Bufferer.sendMessage(owner.getOrCreatePMChannel(), "Report from " + Util.nameThenID(author) + ": " + report);
				info.setReportDate(LocalDateTime.now());
				Gacha.getInstance().getMongo().saveUser(info);
				Bufferer.sendMessage(channel, Util.nameThenID(author) + ", your report has been sent to "
						+ Util.nameThenID(owner) + ". You might receive a friend request for further information soon. Thank you.");
			}
		}
	}

	@Override
	public String getName() {
		return "report";
	}
}
