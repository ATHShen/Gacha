package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.Arrays;

public class GiveCrystalsCommand implements Command {
	@Override
	public void execute(IMessage message, String alias, String[] args) {
		IUser user = Util.findUser(message.getGuild().getUsers(), Arrays.copyOfRange(args, 1, args.length), 0);
		Gacha.getInstance().getUser(user).giveCrystals(Integer.parseInt(args[0]));
		Bufferer.sendMessage(message.getChannel(), "**" + user.getName() + "** has been given **C" + Integer.parseInt(args[0]) + "**.");
		Gacha.getInstance().getMongo().saveUser(Gacha.getInstance().getUser(user));
	}

	@Override
	public String getName() {
		return "givecrystals";
	}

	@Override
	public boolean isOwnerOnly() {
		return true;
	}
}
