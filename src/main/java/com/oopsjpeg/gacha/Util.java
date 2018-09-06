package com.oopsjpeg.gacha;

import com.oopsjpeg.gacha.data.impl.Card;
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
	public static BufferedImage genImage(Card c) {
		try {
			BufferedImage image = ImageIO.read(new File(Gacha.getDataFolder()
					+ "\\cards\\base\\g" + c.getGen() + ".png"));
			BufferedImage user = ImageIO.read(new File(Gacha.getDataFolder()
					+ "\\cards\\" + c.getID() + ".png"));
			Font font;

			Color color = c.getColor() != null ? c.getColor() : getMostCommonColor(user);
			Color textColor = c.getTextColor();

			Graphics2D g2d = image.createGraphics();
			g2d.drawImage(image, 0, 0, null);
			g2d.setComposite(AlphaComposite.SrcAtop);
			g2d.setColor(color);
			g2d.fillRect(0, 0, image.getWidth(), image.getHeight());

			try {
				if (c.getStar() >= 2) {
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

			drawImage(image, user, 1, image.getWidth() / 2, 45, 232, 232);
			drawString(image, c.getName(), font, textColor, 1, image.getWidth() / 2, 33);
			drawString(image, unformat(star(c.getStar())), new Font("Default", Font.BOLD, 28),
					textColor, 1, image.getWidth() / 2, image.getHeight() - 27);

			image.getGraphics().dispose();

			return image;
		} catch (IOException err) {
			err.printStackTrace();
			return null;
		}
	}

	public static String star(int stars) {
		if (stars == 5) return "\\✧";

		return String.join("", Collections.nCopies(stars + 1, "\\☆"));
	}

	public static void drawImage(BufferedImage src, BufferedImage image, int align, int x, int y, int w, int h) {
		Graphics2D g2d = src.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		if (align == 1) g2d.drawImage(image, x - (w / 2), y, w, h, null);
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

	public static void sendError(IChannel channel, IUser author, String content) {
		Bufferer.deleteMessage(Bufferer.sendMessage(channel,
				RoEmote.ERROR + nameThenID(author) + ", " + content), 5);
	}

	public static boolean isImage(String file) {
		String name = file.toLowerCase();
		return name.endsWith("png") || name.endsWith("jpg")
				|| name.endsWith("gif") || name.endsWith("bmp");
	}
}
