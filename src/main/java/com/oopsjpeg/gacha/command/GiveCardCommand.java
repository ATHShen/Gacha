package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.Arrays;

public class GiveCardCommand implements Command {
	@Override
	public void execute(IMessage message, String alias, String[] args) {
		IUser user = Util.findUser(message.getGuild().getUsers(), Arrays.copyOfRange(args, 1, args.length), 0);
		Card card = Gacha.getInstance().getCardByID(args[0]);
		if (card != null) {
			Gacha.getInstance().getUser(user).getCards().add(card);
			Bufferer.sendMessage(message.getChannel(), "**" + user.getName() + "** has been given **" + card.getName() + "**.");
			Gacha.getInstance().getMongo().saveUser(Gacha.getInstance().getUser(user));
		}
	}

	@Override
	public String getName() {
		return "givecard";
	}

	@Override
	public boolean isOwnerOnly() {
		return true;
	}
}
