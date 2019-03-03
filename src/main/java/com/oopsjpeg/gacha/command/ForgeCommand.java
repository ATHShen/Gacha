package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.command.util.Command;
import com.oopsjpeg.gacha.command.util.CommandManager;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.user.UserInfo;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.io.IOException;
import java.util.*;

public class ForgeCommand extends Command {
	public ForgeCommand(CommandManager manager) {
		super(manager, "forge");
		aliases = new String[]{"combine", "craft"};
		usage = "[card_id_1 card_id_2 card_id_3]";
		description = "Combine 3 cards of equal tier for a new card.";
		registeredOnly = true;
	}

	@Override
	public void execute(Message message, String alias, String[] args) throws IOException {
		User author = message.getAuthor();
		MessageChannel channel = message.getChannel();

		if (args.length == 0)
			author.openPrivateChannel().complete().sendMessage(new EmbedBuilder()
					.setTitle("Forging")
					.appendDescription("Combine 3 cards of equal tier to forge a new card of equal or above tier.\n")
					.appendDescription("Identical cards increase the chance of getting the above tier.\n")
					.appendDescription("Use `/forge <card ids...>` to combine the cards.").build()).queue();
		else {
			UserInfo info = getParent().getData().getUser(author.getIdLong());
			int[] ids = Arrays.stream(args).mapToInt(Integer::parseInt).toArray();

			List<Card> available = new ArrayList<>(info.getCards());
			List<Card> combine = new ArrayList<>();

			// Store already specified ids
			for (int i = 0; i < Math.min(3, ids.length); i++) {
				Card c = getParent().getData().getCard(ids[i]);
				if (!available.contains(c)) {
					Util.sendError(channel, author, "one or more of the specified IDs is invalid.");
					return;
				}
				combine.add(c);
				available.remove(c);
			}

			// Attempt to reuse already specified ids if needed
			if (combine.size() < 3) for (int i = 0; i <= 3 - combine.size(); i++) {
				for (int id : ids) {
					Card c = getParent().getData().getCard(id);
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
			List<Card> pool = getParent().getData().getCardsByStar(above ? star + 1 : star);
			pool.removeIf(Card::isExclusive);
			Card card = pool.get(Util.RANDOM.nextInt(pool.size()));
			info.getCards().add(card);

			Util.sendCard(channel, author, card, Util.nameThenId(author) + " got **"
					+ card.getName() + "** from **Standard Forge**.");

			Gacha.getInstance().getMongo().saveUser(info);
		}
	}
}
