package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.data.impl.Card;
import com.oopsjpeg.gacha.util.CardQuery;
import com.oopsjpeg.gacha.wrapper.UserWrapper;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import org.apache.commons.lang3.math.NumberUtils;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CardsCommand implements Command {
	@Override
	public void execute(IMessage message, String alias, String[] args) throws IOException {
		IChannel channel = message.getChannel();
		IUser author = message.getAuthor();
		UserWrapper info = Gacha.getInstance().getUser(author);

		if (info.getCards().isEmpty())
			Util.sendError(channel, author, "you do not have any cards.");
		else if (args.length >= 1 && args[0].equalsIgnoreCase("all")) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			baos.write((author.getName() + "'s Cards (" + info.getCards().size() + ")\n"
					+ CardQuery.of(info.getCards()).raw())
					.getBytes(StandardCharsets.UTF_8));

			Bufferer.sendFile(channel, "Viewing all of " + Util.nameThenID(author) + "'s cards.",
					new ByteArrayInputStream(baos.toByteArray()),
					"Cards_" + Util.fileName(LocalDateTime.now().toString()) + ".txt");

			baos.close();
		} else {
			CardQuery query = CardQuery.of(info.getCards())
					.sort(Comparator.comparingInt(Card::getStar).reversed());

			List<String> filters = new ArrayList<>();
			for (String arg : args) {
				arg = arg.toLowerCase();

				if (arg.contains("-ident")) {
					query.filter(c -> query.get().stream().filter(c::equals).count() >= 2);
					filters.add("Identical");
				}

				if (arg.contains("-g")) {
					int gen = Integer.parseInt(arg.substring(2));
					query.filter(c -> c.getGen() == gen);
					filters.add("Generation " + gen);
				}
			}

			int page = 1;
			if (args.length >= 1 && NumberUtils.isDigits(args[args.length - 1]))
				page = Integer.parseInt(args[args.length - 1]);

			if (page <= 0 || page > query.pages())
				Util.sendError(channel, author, "invalid page.");
			else {
				EmbedBuilder b = new EmbedBuilder();
				b.withAuthorName(author.getName() + "'s Cards (" + info.getCards().size() + ")");
				b.withAuthorIcon(author.getAvatarURL());
				b.withColor(Util.getColor(author, channel));

				b.withDesc(query.page(page).format());
				b.withFooterText("Page " + page + " / " + query.pages() + (filters.isEmpty() ? ""
						: " [Filter: " + String.join(", ", filters) + "]"));

				Bufferer.sendMessage(channel, "Viewing " + Util.nameThenID(author) + "'s cards.", b.build());
			}
		}
	}

	@Override
	public String getName() {
		return "cards";
	}

	@Override
	public String getUsage() {
		return "[page/\"all\"]";
	}

	@Override
	public String getDesc() {
		return "View your cards.";
	}
}
