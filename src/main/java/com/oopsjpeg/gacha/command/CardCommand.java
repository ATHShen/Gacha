package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.user.UserInfo;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class CardCommand implements Command {
	@Override
	public void execute(IMessage message, String alias, String[] args) {
		IChannel channel = message.getChannel();
		IUser author = message.getAuthor();
		UserInfo info = Gacha.getInstance().getUser(author);

		if (info.getCards().isEmpty())
			Util.sendError(channel, author, "you do not have any cards.");
		else {
			Card card = args.length >= 1 ? info.getCards().stream()
					.filter(c -> c.getName().toLowerCase().startsWith(String.join(" ", args).toLowerCase()))
					.findAny().orElse(null) : info.getCards().get(Util.RANDOM.nextInt(info.getCards().size()));
			if (card == null)
				Util.sendError(channel, author, "that card does not exist.");
			else if (!info.getCards().contains(card))
				Util.sendError(channel, author, "you do not have that card.");
			else {
				Bufferer.sendFile(channel, Util.nameThenID(author) + " is showing **" + card.getName()
								+ "** (" + Util.star(card.getStar()) + ")!",
						Gacha.getInstance().getCachedCard(card.getID()), card.getID() + ".png");
			}
		}
	}

	@Override
	public String getName() {
		return "card";
	}

	@Override
	public String getUsage() {
		return "[id]";
	}

	@Override
	public String getDesc() {
		return "Show a random card or a specified card.";
	}

	@Override
	public String[] getAliases() {
		return new String[]{"show"};
	}
}
