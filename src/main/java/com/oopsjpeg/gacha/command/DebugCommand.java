package com.oopsjpeg.gacha.command;

import com.mongodb.client.model.Filters;
import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import org.bson.Document;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class DebugCommand implements Command {
	public void execute(IMessage message, String alias, String[] args) {
		IChannel channel = message.getChannel();
		IUser author = message.getAuthor();

		if (args[0].equalsIgnoreCase("view")) {
			long id = Long.parseLong(args[1]);
			Document d = Gacha.getInstance().getMongo().getUsers().find(Filters.eq(id)).first();
			if (args.length >= 3) {
				String[] querySplit = args[2].split(".");
				for (String s : querySplit) d = (Document) d.get(s);
			}
			Bufferer.sendMessage(channel, "```json\n" + d.toString() + "\n```");
		}
	}

	public String getName() {
		return "debug";
	}

	@Override
	public boolean isOwnerOnly() {
		return true;
	}
}
