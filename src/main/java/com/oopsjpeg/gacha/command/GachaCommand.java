package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.data.DataUtils;
import com.oopsjpeg.gacha.data.EventUtils;
import com.oopsjpeg.gacha.data.QuestUtils;
import com.oopsjpeg.gacha.data.impl.Card;
import com.oopsjpeg.gacha.data.impl.Quest;
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

		int cost = EventUtils.gacha();

		if (info.getCrystals() < cost)
			Util.sendError(channel, author, "you need **C" + cost + "** to Gacha.");
		else if (Gacha.getInstance().getCards().isEmpty())
			Util.sendError(channel, author, "there are no cards available right now.");
		else {
			info.giveCrystals(cost * -1);

			List<Card> pool;
			float f = Util.RANDOM.nextFloat();

			if (f <= 0.01) pool = Gacha.getInstance().getCardsByStar(4);
			else if (f <= 0.03) pool = Gacha.getInstance().getCardsByStar(3);
			else if (f <= 0.10) pool = Gacha.getInstance().getCardsByStar(2);
			else if (f <= 0.21) pool = Gacha.getInstance().getCardsByStar(1);
			else pool = Gacha.getInstance().getCardsByStar(0);

			pool.removeIf(Card::isSpecial);

			Card c = pool.get(Util.RANDOM.nextInt(pool.size() - 1));
			info.getCards().add(c);
			Bufferer.sendFile(channel, Util.nameThenID(author) + " got a(n) **" + c.getName() + "** (" + Util.star(c.getStar()) + ").",
					Gacha.getInstance().getCachedCard(c.getID()), c.getID() + ".png");

			for (UserWrapper.QuestData data : info.getActiveQuestDatas())
				for (Quest.Condition cond : data.getConditionsByType(Quest.ConditionType.GACHA_ANY))
					data.setProgress(cond, 0, DataUtils.getInt(data.getProgress(cond, 0)) + 1);

			QuestUtils.check(channel, author);

			Gacha.getInstance().getMongo().saveUser(info);
		}
	}

	@Override
	public String getName() {
		return "gacha";
	}

	@Override
	public String getDesc() {
		return "Pull a random card from 1-star to 5-star.";
	}
}
