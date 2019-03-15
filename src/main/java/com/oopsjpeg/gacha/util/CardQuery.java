package com.oopsjpeg.gacha.util;

import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.object.Card;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CardQuery {
    private List<Card> cards;

    public CardQuery(Collection<Card> cards) {
        this.cards = new ArrayList<>(cards);
        sort(Comparator.comparingInt(Card::getStar).reversed());
    }

    public CardQuery search(String search) {
        final String s = search.toLowerCase();
        return filter(c -> String.valueOf(c.getId()).equals(search) || c.getName().toLowerCase().contains(s));
    }

    public CardQuery sort(Comparator<Card> comparator) {
        cards = cards.stream()
                .sorted(comparator.thenComparing(Card::getName))
                .collect(Collectors.toList());

        return this;
    }

    public CardQuery filter(Predicate<? super Card> predicate) {
        cards = cards.stream().filter(predicate).collect(Collectors.toList());

        return this;
    }

    public CardQuery page(int page) {
        List<Card> cards = this.cards.stream().skip((page - 1) * 10)
                .limit(10).collect(Collectors.toList());

        return new CardQuery(cards);
    }

    public int pages() {
        return (int) Math.ceil((float) cards.size() / 10);
    }

    public List<Card> get() {
        return cards;
    }

    public int size() {
        return cards.size();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public String raw() {
        return cards.stream().map(c -> "(" + Util.star(c.getStar()) + ") "
                + c.getName() + " [ID" + c.getId() + "]")
                .collect(Collectors.joining("\n"));
    }

    public String format() {
        return cards.stream().map(c -> "(" + Util.star(c.getStar()) + ") **"
                + c.getName() + "** [`ID" + c.getId() + "`]")
                .collect(Collectors.joining("\n"));
    }

    @Override
    public String toString() {
        return cards.toString();
    }
}
