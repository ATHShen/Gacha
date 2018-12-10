package com.oopsjpeg.gacha.command;

import com.mongodb.client.model.Filters;
import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import org.bson.Document;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DebugCommand implements Command {
	public void execute(IMessage message, String alias, String[] args) throws IOException {
		IChannel channel = message.getChannel();
		IUser author = message.getAuthor();

		if (args[0].equalsIgnoreCase("view")) {
			long id = Long.parseLong(args[1]);
			Document d = Gacha.getInstance().getMongo().getUsers().find(Filters.eq(id)).first();

			ByteArrayOutputStream output = new ByteArrayOutputStream();
			output.write(d.toString().getBytes(StandardCharsets.UTF_8));
			ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
			Bufferer.sendFile(channel, input, "debug.txt");

			output.close();
			input.close();
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
