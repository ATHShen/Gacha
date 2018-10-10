package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.data.DataUtils;
import com.oopsjpeg.gacha.data.QuestUtils;
import com.oopsjpeg.gacha.data.impl.Card;
import com.oopsjpeg.gacha.data.impl.Quest;
import com.oopsjpeg.gacha.wrapper.UserWrapper;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.*;

public class ForgeCommand implements Command {
	@Override
	public void execute(IMessage message, String alias, String[] args) {
		IUser author = message.getAuthor();
		IChannel channel = message.getChannel();

		if (args.length == 0)
			Bufferer.sendMessage(author.getOrCreatePMChannel(), "**Forging**\n"
					+ "Combine 3 cards of equal tier to forge a new card of equal or above tier.\n"
					+ "Identical cards increase the chance of getting the above tier.\n"
					+ "Use `/forge <card ids...>` to combine the cards.");
		else {
			UserWrapper info = Gacha.getInstance().getUser(author);
			String[] ids = Arrays.stream(args).map(String::toLowerCase).toArray(String[]::new);

			List<Card> available = new ArrayList<>(info.getCards());
			List<Card> combine = new ArrayList<>();

			// Store already specified ids
			for (int i = 0; i < Math.min(3, ids.length); i++) {
				Card c = Gacha.getInstance().getCardByID(ids[i]);
				if (!available.contains(c)) {
					Util.sendError(channel, author, "one or more of the specified IDs is invalid.");
					return;
				}
				combine.add(c);
				available.remove(c);
			}

			// Attempt to reuse already specified ids if needed
			if (combine.size() < 3) for (int i = 0; i <= 3 - combine.size(); i++) {
				for (String id : ids) {
					Card c = Gacha.getInstance().getCardByID(id);
					if (available.contains(c)) {
						combine.add(c);
						available.remove(c);
						break;
					}
				}
			}

			// Minimum of 3 cards required
			if (combine.size() < 3) {
				Util.sendError(channel, author, "you require at least 3 cards to forge.");
				return;
			}

			int star = combine.get(0).getStar();
			// Legends cannot be forged
			if (star == 6) {
				Util.sendError(channel, author, "you cannot forge Legend cards.");
				return;
			}
			// Equal tier required
			if (combine.stream().anyMatch(c -> c.getStar() != star)) {
				Util.sendError(channel, author, "the cards must all be of equal tier.");
				return;
			}

			// Increase above tier chance from identical cards
			float chance = 0.4f - (star * 0.05f);
			for (Card c : new HashSet<>(combine))
				chance += (Collections.frequency(combine, c) - 1) * (0.25f + (0.05f * star));

			// Take the cards and gacha a new card
			info.setCards(available);

			boolean above = Util.RANDOM.nextFloat() <= chance;
			List<Card> pool = Gacha.getInstance().getCardsByStar(above ? star + 1 : star);
			pool.removeIf(c -> c.isSpecial() || c.getGen() != Gacha.getInstance().getSettings().getCurrentGen());
			Card card = pool.get(Util.RANDOM.nextInt(pool.size()));
			info.getCards().add(card);

			Bufferer.sendFile(channel, Util.nameThenID(author) + " got a(n) **" + card.getName()
							+ "** (" + Util.star(card.getStar()) + ") from forging.",
					Gacha.getInstance().getCachedCard(card.getID()), card.getID() + ".png");

			for (UserWrapper.QuestData data : info.getActiveQuestDatas()) {
				for (Quest.Condition cond : data.getConditionsByType(Quest.ConditionType.FORGE_ANY))
					data.setProgress(cond, 0, DataUtils.getInt(data.getProgress(cond, 0)) + 1);
				if (above) for (Quest.Condition cond : data.getConditionsByType(Quest.ConditionType.FORGE_SUCCESS))
					data.setProgress(cond, 0, DataUtils.getInt(data.getProgress(cond, 0)) + 1);
			}

			QuestUtils.check(channel, author);

			Gacha.getInstance().getMongo().saveUser(info);
		}
	}

	@Override
	public String getName() {
		return "forge";
	}

	@Override
	public String getUsage() {
		return "[card_id_1 card_id_2 card_id_3]";
	}

	@Override
	public String getDesc() {
		return "Combine 3 cards of equal tier for a new card.";
	}

	@Override
	public String[] getAliases() {
		return new String[]{"combine"};
	}
}
