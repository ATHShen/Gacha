package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.data.impl.Card;
import com.oopsjpeg.gacha.wrapper.UserWrapper;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;

public class GachaCommand implements Command {
	@Override
	public void execute(IMessage message, String alias, String[] args) {
		IChannel channel = message.getChannel();
		IUser author = message.getAuthor();
		UserWrapper info = Gacha.getInstance().getUser(author);

		if (info.getCrystals() < Gacha.getInstance().getGachaCost())
			Util.sendError(channel, author, "you need **C" + Gacha.getInstance().getGachaCost() + "** to Gacha.");
		else if (Gacha.getInstance().getCards().isEmpty())
			Util.sendError(channel, author, "there are no cards available right now.");
		else {
			info.giveCrystals(Gacha.getInstance().getGachaCost() * -1);

			List<Card> pool;
			float f = Util.RANDOM.nextFloat();

			if (f <= 0.01) pool = Gacha.getInstance().getCardsForStar(4);
			else if (f <= 0.03) pool = Gacha.getInstance().getCardsForStar(3);
			else if (f <= 0.10) pool = Gacha.getInstance().getCardsForStar(2);
			else if (f <= 0.21) pool = Gacha.getInstance().getCardsForStar(1);
			else pool = Gacha.getInstance().getCardsForStar(0);

			Card c = pool.get(Util.RANDOM.nextInt(pool.size() - 1));
			info.getCards().add(c);
			Bufferer.sendFile(channel, Util.nameThenID(author) + " got a(n) **" + c.getName() + "** (" + Util.star(c.getStar()) + ").",
					Gacha.getInstance().getCachedCard(c.getID()), c.getID() + ".png");

			Gacha.getInstance().getMongo().saveUser(info);
		}
	}

	@Override
	public String getName() {
		return "gacha";
	}
}
