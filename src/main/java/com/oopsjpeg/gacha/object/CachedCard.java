package com.oopsjpeg.gacha.object;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CachedCard {
	private final int id;
	private final BufferedImage image;
	private final Color embedColor;

	public CachedCard(int id, BufferedImage image, Color embedColor) {
		this.id = id;
		this.image = image;
		this.embedColor = embedColor;
	}

	public InputStream get() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageIO.write(getImage(), "png", output);
		return new ByteArrayInputStream(output.toByteArray());
	}

	public int getId() {
		return id;
	}

	public BufferedImage getImage() {
		return image;
	}

	public Color getEmbedColor() {
		return embedColor;
	}
}
