package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.user.UserInfo;
import com.oopsjpeg.gacha.util.CardQuery;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class CardCommand implements Command {
	private final Gacha instance = Gacha.getInstance();

	@Override
	public void execute(IMessage message, String alias, String[] args) {
		IChannel channel = message.getChannel();
		IUser author = message.getAuthor();
		UserInfo info = instance.getOrCreateUser(author);

		if (info.getCards().isEmpty())
			Util.sendError(channel, author, "you do not have any cards.");
		else {
			Card card = null;

			if (args.length <= 0)
				card = info.getCards().get(Util.RANDOM.nextInt(info.getCards().size()));
			else {
				String search = String.join(" ", args);
				CardQuery query = new CardQuery(info.getCards()).search(search);
				if (!query.isEmpty()) card = query.get().get(Util.RANDOM.nextInt(query.size()));
			}

			if (card == null)
				Util.sendError(channel, author, "you either do not have that card, or it does not exist.");
			else
				Util.sendCard(channel, author, card, Util.nameThenID(author) + " is viewing **" + card.getName() + "**.");
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
