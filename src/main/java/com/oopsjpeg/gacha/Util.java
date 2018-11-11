package com.oopsjpeg.gacha;

import com.oopsjpeg.gacha.object.CachedCard;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.util.Embeds;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.RoEmote;
import com.oopsjpeg.roboops.framework.RoUtil;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Util extends RoUtil {
	public static CachedCard genImage(Card card) {
		try {
			BufferedImage canvas = new BufferedImage(250, 340, BufferedImage.TYPE_3BYTE_BGR);
			BufferedImage base = ImageIO.read(new File(Gacha.getDataFolder()
					+ "\\cards\\base\\" + card.getGen() + (card.isSpecial() ? "s" : "") + ".png"));
			BufferedImage art = ImageIO.read(new File(Gacha.getDataFolder()
					+ "\\cards\\" + card.getID() + ".png"));
			Font font;

			Color color = card.getColor() != null ? card.getColor() : getMostCommonColor(art);
			Color textColor = card.getTextColor();

			try {
				if (card.getStar() >= 3) {
					font = Font.createFont(Font.TRUETYPE_FONT,
							Util.class.getClassLoader().getResourceAsStream("ITCEDSCR.TTF"))
							.deriveFont(Font.PLAIN, 30);
				} else {
					font = Font.createFont(Font.TRUETYPE_FONT,
							Util.class.getClassLoader().getResourceAsStream("MISTRAL.TTF"))
							.deriveFont(Font.PLAIN, 28);
				}
			} catch (FontFormatException err) {
				font = new Font("Default", Font.PLAIN, 28);
			}

			drawImage(canvas, base, 0, 0, 0, base.getWidth(), base.getHeight());

			Graphics2D g2d = canvas.createGraphics();
			g2d.setComposite(AlphaComposite.SrcAtop);
			g2d.setColor(color);
			g2d.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

			drawImage(canvas, art, 1, base.getWidth() / 2, 45, 232, 232);
			drawString(canvas, card.getName(), font, textColor, 1, canvas.getWidth() / 2, 33);
			drawString(canvas, unformat(star(card.getStar())), new Font("Default", Font.BOLD, 28),
					textColor, 1, canvas.getWidth() / 2, canvas.getHeight() - 27);

			canvas.getGraphics().dispose();

			return new CachedCard(card.getID(), canvas, color);
		} catch (IOException err) {
			Gacha.LOGGER.error("Error generating image for card ID " + card.getID() + ".");
			err.printStackTrace();
			return null;
		}
	}

	public static String star(int stars) {
		if (stars == 6) return "\\✧";

		return String.join("", Collections.nCopies(stars, "\\☆"));
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

	public static void drawString(BufferedImage src, String s, Font font, Color color, int align, int x, int y) {
		Graphics2D g2d = src.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setFont(font);
		g2d.setColor(color);
		int length = (int) g2d.getFontMetrics().getStringBounds(s, g2d).getWidth();
		if (align == 1) g2d.drawString(s, x - (length / 2), y);
	}

	@SuppressWarnings("unchecked")
	private static Color getMostCommonColor(BufferedImage image) {
		Map<Integer, Integer> colorMap = new HashMap<>();
		int height = image.getHeight();
		int width = image.getWidth();

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int rgb = image.getRGB(i, j);
				if (!isGray(getRGBArr(rgb))) {
					Integer counter = colorMap.get(rgb);
					if (counter == null) {
						counter = 0;
					}

					colorMap.put(rgb, ++counter);
				}
			}
		}

		List<Map.Entry<Integer, Integer>> list = new LinkedList<>(colorMap.entrySet());

		list.sort((Map.Entry<Integer, Integer> obj1, Map.Entry<Integer, Integer> obj2)
				-> ((Comparable) obj1.getValue()).compareTo(obj2.getValue()));

		Map.Entry<Integer, Integer> entry = list.get(list.size() - 1);
		int[] rgb = getRGBArr(entry.getKey());

		return new Color((float) rgb[0] / 255, (float) rgb[1] / 255, (float) rgb[2] / 255, 0.35f);
	}

	private static int[] getRGBArr(int pixel) {
		int alpha = (pixel >> 24) & 0xff;
		int red = (pixel >> 16) & 0xff;
		int green = (pixel >> 8) & 0xff;
		int blue = (pixel) & 0xff;

		return new int[]{red, green, blue};
	}

	private static boolean isGray(int[] rgbArr) {
		int rgDiff = rgbArr[0] - rgbArr[1];
		int rbDiff = rgbArr[0] - rgbArr[2];
		// Filter out black, white and grays...... (tolerance within 10 pixels)
		int tolerance = 10;
		if (rgDiff > tolerance || rgDiff < -tolerance)
			return rbDiff <= tolerance && rbDiff >= -tolerance;
		return true;
	}

	public static String unformat(String s) {
		return s.replaceAll("\\*", "").replaceAll("`", "").replaceAll("\\\\", "");
	}

	public static String fileName(String s) {
		return s.replaceAll("[^a-zA-Z0-9.\\-]", "_");
	}

	public static String nameThenID(IUser user) {
		return "**" + user.getName() + "**#" + user.getDiscriminator();
	}

	public static void sendCard(IChannel channel, IUser author, Card card, String content) {
		Bufferer.sendFile(channel, content, Gacha.getInstance().getCachedCard(card.getID()).get(),
				card.getID() + ".png", Embeds.card(author, card));
	}

	public static void sendError(IChannel channel, IUser author, String content) {
		Bufferer.deleteMessage(Bufferer.sendMessage(channel,
				RoEmote.ERROR + nameThenID(author) + ", " + content), 30);
	}

	public static boolean isImage(String file) {
		String name = file.toLowerCase();
		return name.endsWith("png") || name.endsWith("jpg")
				|| name.endsWith("gif") || name.endsWith("bmp");
	}

	public static <T> boolean listType(Object object, Class clazz) {
		if (!(object instanceof List)) return false;
		List list = (List) object;
		return !list.isEmpty() && list.get(0).getClass().equals(clazz);
	}
}
