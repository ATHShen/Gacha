package com.oopsjpeg.gacha.object.user;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.mission.MissionProgress;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.core.entities.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class UserInfo {
    @Getter private final long id;

    @Getter @Setter private int crystals;
    @Setter
    private Bank bank;
    @Getter @Setter private List<Integer> cardIds = new ArrayList<>();
    @Getter @Setter private List<MissionProgress> missions = new ArrayList<>();

    @Getter @Setter private LocalDateTime dailyDate;
    @Getter @Setter private LocalDateTime weeklyDate;

    @Getter @Setter private String description;

    public User getUser() {
        return Gacha.getInstance().getClient().getUserById(id);
    }

    public void addCrystals(int crystals) {
        this.crystals += crystals;
    }

    public void removeCrystals(int crystals) {
        this.crystals -= crystals;
    }

    public Bank getBank() {
        if (bank == null) bank = new Bank();
        return bank;
    }

    public List<Card> getCards() {
        return cardIds.stream().map(id -> Gacha.getInstance().getCard(id)).collect(Collectors.toList());
    }

    public void setCards(List<Card> cards) {
        cardIds = cards.stream().map(Card::getId).collect(Collectors.toList());
    }

    public void addCard(Card card) {
        cardIds.add(card.getId());
    }

    public void removeCard(Card card) {
        cardIds.remove(card.getId());
    }

    public boolean hasDaily() {
        return dailyDate == null || LocalDateTime.now().isAfter(dailyDate.plusDays(1));
    }

    public boolean hasWeekly() {
        return weeklyDate == null || LocalDateTime.now().isAfter(weeklyDate.plusWeeks(1));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof UserInfo && ((UserInfo) obj).id == id;
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }
}
