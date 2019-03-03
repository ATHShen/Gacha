package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.command.util.Command;
import com.oopsjpeg.gacha.command.util.CommandManager;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.user.UserInfo;
import com.oopsjpeg.gacha.util.CardQuery;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.io.IOException;

public class CardCommand extends Command {
	public CardCommand(CommandManager manager) {
		super(manager, "card");
		aliases = new String[]{"show"};
		usage = "[id]";
		description = "Show a random card or a specified card.";
		registeredOnly = true;
	}

	@Override
	public void execute(Message message, String alias, String[] args) throws IOException {
		MessageChannel channel = message.getChannel();
		User author = message.getAuthor();
		UserInfo info = getParent().getData().getUser(author.getIdLong());

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
				Util.sendCard(channel, author, card, Util.nameThenId(author) + " is viewing **" + card.getName() + "**.");
		}
	}
}
