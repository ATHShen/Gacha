package com.oopsjpeg.gacha;

import com.oopsjpeg.gacha.object.CachedCard;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.util.Embeds;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Util {
	public static final Random RANDOM = new Random();

	public static CachedCard generateImage(Card card) throws IOException {
		BufferedImage canvas = new BufferedImage(250, 340, BufferedImage.TYPE_3BYTE_BGR);
		BufferedImage base = ImageIO.read(new File(Gacha.getDataFolder()
				+ "\\cards\\base\\" + card.getBase() + ".png"));
		BufferedImage image = ImageIO.read(new File(Gacha.getDataFolder()
				+ "\\cards\\" + card.getImage() + ".png"));
		Font font;

		try (InputStream fontStream = Util.class.getClassLoader().getResourceAsStream(card.getFont() + ".TTF")) {
			font = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(Font.PLAIN, card.getFontSize());
		} catch (FontFormatException error) {
			font = new Font("Default", Font.PLAIN, card.getFontSize());
		}

		drawImage(canvas, base, 0, 0, 0, base.getWidth(), base.getHeight());

		Graphics2D g2d = canvas.createGraphics();
		g2d.setComposite(AlphaComposite.SrcAtop);
		g2d.setColor(card.getBaseColor());
		g2d.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

		drawImage(canvas, image, 1, base.getWidth() / 2, 45, 232, 232);

		drawText(canvas, card.getName(), font, card.getTextColor(), 1, canvas.getWidth() / 2, 33);
		drawText(canvas, star(card.getStar()), new Font("Default", Font.BOLD, 28),
				card.getTextColor(), 1, canvas.getWidth() / 2, canvas.getHeight() - 27);

		canvas.getGraphics().dispose();

		return new CachedCard(card.getId(), canvas, card.getBaseColor());
	}

	public static String star(int stars) {
		if (stars == 6) return "\u2727";

		return String.join("", Collections.nCopies(stars, "\u2606"));
	}

	public static String starFormatted(int stars) {
		StringBuilder output = new StringBuilder();
		star(stars).chars().forEach(i -> output.append("\\").append((char) i));
		return output.toString();
	}

	public static void drawImage(BufferedImage src, BufferedImage image, int align, int x, int y, int w, int h) {
		Graphics2D g2d = src.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		switch (align) {
			case 1: g2d.drawImage(image, x - (w / 2), y, w, h, null); break;
			case 2: g2d.drawImage(image, x - w, y, w, h, null); break;
			default: g2d.drawImage(image, x, y, w, h, null); break;
		}
	}

	public static void drawText(BufferedImage src, String s, Font font, Color color, int align, int x, int y) {
		Graphics2D g2d = src.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setFont(font);
		g2d.setColor(color);
		int length = (int) g2d.getFontMetrics().getStringBounds(s, g2d).getWidth();
		if (align == 1) g2d.drawString(s, x - (length / 2), y);
	}

	public static String fileName(String s) {
		return s.replaceAll("[^a-zA-Z0-9.\\-]", "_");
	}

	public static String nameThenId(User user) {
		return "**" + user.getName() + "**#" + user.getDiscriminator();
	}

	public static void send(MessageChannel channel, User user, String content) {
		send(channel, null, content, Util.getColor(user, channel.getIdLong()));
	}

	public static void send(MessageChannel channel, User user, String title, String content) {
		send(channel, title, content, Util.getColor(user, channel.getIdLong()));
	}

	public static void send(MessageChannel channel, String content, Color color) {
		send(channel, null, content, color);
	}

	public static void send(MessageChannel channel, String title, String content, Color color) {
		channel.sendMessage(new EmbedBuilder()
				.setColor(color)
				.setTitle(title)
				.setDescription(content).build()).queue();
	}

	public static void sendCard(MessageChannel channel, User user, Card card, String content) throws IOException {
		try (InputStream is = Gacha.getInstance().getData().getCachedCard(card.getId()).get()) {
			channel.sendFile(is, card.getId() + ".png", new MessageBuilder(content)
					.setEmbed(Embeds.card(user, card)).build()).queue();
		}
	}

	public static void sendError(MessageChannel channel, User user, String content) {
		send(channel, null, ":x: " + nameThenId(user) + ": " + content, Color.RED);
	}

	public static boolean checkListType(Object object, Class clazz) {
		if (!(object instanceof List)) return false;
		List list = (List) object;
		return !list.isEmpty() && list.get(0).getClass().equals(clazz);
	}

	public static Color getColor(User user, long channelId) {
		Color color = Color.LIGHT_GRAY;
		Channel channel = user.getJDA().getTextChannelById(channelId);

		if (channel.getGuild() != null) {
			List<Role> roles = channel.getGuild().getMember(user).getRoles().stream()
					.sorted(Comparator.comparingInt(Role::getPosition))
					.collect(Collectors.toList());

			for (Role role : roles)
				if (!role.getColor().equals(new Color(0, 0, 0, 0)))
					color = role.getColor();
		}

		return color;
	}

	public static Color stringToColor(String s) {
		List<Float> rgba = Arrays.stream(s.split(","))
				.map(Float::parseFloat).collect(Collectors.toList());
		return new Color(rgba.get(0), rgba.get(1), rgba.get(2), rgba.get(3));
	}

	public static boolean isDigits(CharSequence str) {
		return str.codePoints().allMatch(Character::isDigit);
	}

	public static void sendEmbed(MessageChannel channel, String content, MessageEmbed embed) {
		channel.sendMessage(new MessageBuilder(content).setEmbed(embed).build()).queue();
	}

	public static String timeDiff(LocalDateTime date1, LocalDateTime date2) {
		Duration duration = Duration.between(date1, date2);
		Stack<String> stack = new Stack<>();

		if (duration.toDays() > 0) stack.push(duration.toDays() + "d");
		duration = duration.minusDays(duration.toDays());

		if (duration.toHours() > 0) stack.push(duration.toHours() + "h");
		duration = duration.minusHours(duration.toHours());

		if (duration.toMinutes() > 0) stack.push(duration.toMinutes() + "m");
		duration = duration.minusMinutes(duration.toMinutes());

		if (duration.getSeconds() > 0) stack.push(duration.getSeconds() + "s");

		return stack.stream().limit(3).collect(Collectors.joining(" "));
	}

	public static String comma(int value) {
		return new DecimalFormat("#,###").format(value);
	}
}
