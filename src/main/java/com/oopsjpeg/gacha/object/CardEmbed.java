package com.oopsjpeg.gacha.object;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
@Getter
public class CardEmbed {
    private final int cardId;
    private final BufferedImage image;
    private final Color embedColor;

    public InputStream get() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(getImage(), "png", output);
        return new ByteArrayInputStream(output.toByteArray());
    }
}
