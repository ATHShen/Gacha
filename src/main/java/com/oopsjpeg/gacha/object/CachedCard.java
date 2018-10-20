package com.oopsjpeg.gacha.object;

import com.oopsjpeg.gacha.Gacha;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CachedCard {
	private String id;
	private BufferedImage image;
	private Color color;

	public CachedCard(String id, BufferedImage image, Color color) {
		this.id = id;
		this.image = image;
		this.color = color;
	}

	public InputStream get() {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ImageIO.write(getImage(), "png", os);
			return new ByteArrayInputStream(os.toByteArray());
		} catch (IOException err) {
			Gacha.LOGGER.error("Error loading cached card ID " + id + ".");
			err.printStackTrace();
			return null;
		}
	}

	public String getID() {
		return id;
	}

	public BufferedImage getImage() {
		return image;
	}

	public Color getColor() {
		return color;
	}
}
