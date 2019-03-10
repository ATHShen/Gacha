package com.oopsjpeg.gacha.object;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.awt.*;
import java.util.Objects;

@RequiredArgsConstructor
@Getter
@Setter
public class Card {
    private final int id;
    private String name;
    private String image;
    private String source;

    private int star;
    private boolean special;
    private boolean exclusive;

    private int base;
    private String font = "COMIC";
    private int fontSize = 60;
    private Color baseColor = Color.GRAY;
    private Color textColor = Color.WHITE;

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Card && ((Card) obj).id == id;
    }

    @Override
    public String toString() {
        return name;
    }
}