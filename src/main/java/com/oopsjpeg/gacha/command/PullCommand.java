package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.command.util.Command;
import com.oopsjpeg.gacha.command.util.CommandManager;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.UserInfo;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.io.IOException;
import java.util.List;

public class PullCommand extends Command {
	public PullCommand(CommandManager manager) {
		super(manager, "pull");
		aliases = new String[]{"gacha"};
		description = "Pull a random card.";
		registeredOnly = true;
	}

	@Override
	public void execute(Message message, String alias, String[] args) throws IOException {
		MessageChannel channel = message.getChannel();
		User author = message.getAuthor();
		UserInfo info = getParent().getUser(author.getIdLong());

		int cost = 1000;

		if (info.getCrystals() < cost)
			Util.sendError(channel, author, "You need **C" + cost + "** to pull.");
		else if (getParent().getCards().isEmpty())
			Util.sendError(channel, author, "There are no cards available right now.");
		else {
			info.addCrystals(cost * -1);

			List<Card> pool;
			float f = Util.RANDOM.nextFloat();

			if (f <= 0.0075) pool = getParent().getCardsByStar(5);
			else if (f <= 0.0275) pool = getParent().getCardsByStar(4);
			else if (f <= 0.09) pool = getParent().getCardsByStar(3);
			else if (f <= 0.28) pool = getParent().getCardsByStar(2);
			else pool = getParent().getCardsByStar(1);

			pool.removeIf(c -> c.isSpecial() || c.isExclusive());

			Card c = pool.get(Util.RANDOM.nextInt(pool.size()));
			info.addCard(c);
			Util.sendCard(channel, author, c, Util.nameThenId(author) + " got **" + c.getName() + "** from **Standard Pull**.");

			Gacha.getInstance().getMongo().saveUser(info);
		}
	}
}
