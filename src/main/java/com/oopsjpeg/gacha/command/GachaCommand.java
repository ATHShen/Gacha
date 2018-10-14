package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.Quest;
import com.oopsjpeg.gacha.object.user.QuestData;
import com.oopsjpeg.gacha.object.user.UserInfo;
import com.oopsjpeg.gacha.util.DataUtils;
import com.oopsjpeg.gacha.util.EventUtils;
import com.oopsjpeg.gacha.util.QuestUtils;
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
		UserInfo info = Gacha.getInstance().getUser(author);

		int cost = EventUtils.gacha();

		if (info.getCrystals() < cost)
			Util.sendError(channel, author, "you need **C" + cost + "** to Gacha.");
		else if (Gacha.getInstance().getCurrentCards().isEmpty())
			Util.sendError(channel, author, "there are no cards available right now.");
		else {
			info.addCrystals(cost * -1);

			List<Card> pool;
			float f = Util.RANDOM.nextFloat();

			if (f <= 0.0075) pool = Gacha.getInstance().getCardsByStar(5);
			else if (f <= 0.0275) pool = Gacha.getInstance().getCardsByStar(4);
			else if (f <= 0.09) pool = Gacha.getInstance().getCardsByStar(3);
			else if (f <= 0.28) pool = Gacha.getInstance().getCardsByStar(2);
			else pool = Gacha.getInstance().getCardsByStar(1);

			pool.removeIf(c -> !Gacha.getInstance().isCurrentCard(c));

			Card c = pool.get(Util.RANDOM.nextInt(pool.size()));
			info.getCards().add(c);
			Bufferer.sendFile(channel, Util.nameThenID(author) + " got a(n) **"
							+ c.getName() + "** (" + Util.star(c.getStar()) + ").",
					Gacha.getInstance().getCachedCard(c.getID()), c.getID() + ".png");

			for (QuestData data : info.getActiveQuestDatas())
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
