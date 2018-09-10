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
import sx.blah.discord.util.EmbedBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
					+ Util.unformat(formatCards(info.getCards())))

					.getBytes(StandardCharsets.UTF_8));

			Bufferer.sendFile(channel, "Viewing all of " + Util.nameThenID(author) + "'s cards.",
					new ByteArrayInputStream(baos.toByteArray()),
					"Cards_" + Util.fileName(LocalDateTime.now().toString()) + ".txt");

			baos.close();
		} else {
			int page = args.length == 0 ? 1 : Integer.parseInt(args[0]);
			if (page <= 0 || page > pages(info.getCards().size()))
				Util.sendError(channel, author, "invalid page.");
			else {
				EmbedBuilder b = new EmbedBuilder();
				b.withAuthorName(author.getName() + "'s Cards (" + info.getCards().size() + ")");
				b.withAuthorIcon(author.getAvatarURL());
				b.withColor(Util.getColor(author, channel));

				b.withDesc(formatCards(info.getCards(), page));
				b.withFooterText("Page " + page + " / " + pages(info.getCards().size()));

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

	private List<Card> sortCards(List<Card> cards) {
		return cards.stream()
				.sorted(Comparator.comparingInt(Card::getStar)
						.reversed().thenComparing(Card::getName))
				.collect(Collectors.toList());
	}

	private List<Card> pageCards(List<Card> cards, int page) {
		return sortCards(cards).stream()
				.skip((page - 1) * 10).limit(10)
				.collect(Collectors.toList());
	}

	private String formatCards(List<Card> cards) {
		return sortCards(cards).stream()
				.map(c -> "(" + Util.star(c.getStar()) + ") **" + c.getName() + "** [`" + c.getID() + "`]")
				.collect(Collectors.joining("\n"));
	}

	private String formatCards(List<Card> cards, int page) {
		return formatCards(pageCards(cards, page));
	}

	private int pages(int cards) {
		return (int) Math.ceil((float) cards / 10);
	}
}
