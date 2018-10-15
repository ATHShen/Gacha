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
	public static final int SORT_STAR = 0;

	public static final int FILTER_IDENTICAL = 0;

	private List<Card> cards;

	private CardQuery(List<Card> cards) {
		this.cards = cards;
	}

	public static CardQuery of(Collection<Card> cards) {
		return new CardQuery(new ArrayList<>(cards));
	}

	public CardQuery search(String s) {
		String search = s.toLowerCase();
		cards = cards.stream()
				.filter(c -> c.getID().startsWith(search)
						|| c.getName().toLowerCase().startsWith(search))
				.collect(Collectors.toList());
		return this;
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

	public String raw() {
		return cards.stream().map(c -> "(" + Util.star(c.getStar()) + ") "
				+ c.getName() + " [" + c.getID() + "]")
				.collect(Collectors.joining("\n"));
	}

	public String format() {
		return cards.stream().map(c -> "(" + Util.star(c.getStar()) + ") **"
				+ c.getName() + "** [`" + c.getID() + "`]")
				.collect(Collectors.joining("\n"));
	}
}
