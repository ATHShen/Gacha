package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.command.util.Command;
import com.oopsjpeg.gacha.command.util.CommandManager;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.user.UserInfo;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.io.IOException;
import java.util.List;

public class GachaCommand extends Command {
	public GachaCommand(CommandManager manager) {
		super(manager, "gacha");
		description = "Pull a random card.";
		registeredOnly = true;
	}

	@Override
	public void execute(Message message, String alias, String[] args) throws IOException {
		MessageChannel channel = message.getChannel();
		User author = message.getAuthor();
		UserInfo info = getParent().getData().getUser(author.getIdLong());

		int cost = 500;

		if (info.getCrystals() < cost)
			Util.sendError(channel, author, "you need **C" + cost + "** to Gacha.");
		else if (getParent().getData().getCards().isEmpty())
			Util.sendError(channel, author, "there are no cards available right now.");
		else {
			info.addCrystals(cost * -1);

			List<Card> pool;
			float f = Util.RANDOM.nextFloat();

			if (f <= 0.0075) pool = getParent().getData().getCardsByStar(5);
			else if (f <= 0.0275) pool = getParent().getData().getCardsByStar(4);
			else if (f <= 0.09) pool = getParent().getData().getCardsByStar(3);
			else if (f <= 0.28) pool = getParent().getData().getCardsByStar(2);
			else pool = getParent().getData().getCardsByStar(1);

			pool.removeIf(c -> c.isSpecial() || c.isExclusive());

			Card c = pool.get(Util.RANDOM.nextInt(pool.size()));
			info.getCards().add(c);
			Util.sendCard(channel, author, c, Util.nameThenId(author) + " got **" + c.getName() + "** from **Standard Gacha**.");

			Gacha.getInstance().getMongo().saveUser(info);
		}
	}
}
