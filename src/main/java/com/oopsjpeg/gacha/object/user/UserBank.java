package com.oopsjpeg.gacha.object.user;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Created by oopsjpeg on 3/10/2019.
 */
public class UserBank {
    @Getter @Setter private int crystals;
    @Getter @Setter private LocalDateTime interestDate;
    @Getter @Setter private LocalDateTime transactionDate;

    public void addCrystals(int crystals) {
        this.crystals += crystals;
    }

    public void removeCrystals(int crystals) {
        this.crystals -= crystals;
    }

    public boolean hasCrystals() {
        return crystals > 0;
    }

    public void interest() {
        interestDate = LocalDateTime.now();
        crystals += Math.min(10000, crystals * 0.04f);
    }

    public boolean hasInterest() {
        return interestDate == null || LocalDateTime.now().isAfter(interestDate.plusDays(1));
    }

    public boolean hasTransaction() {
        return transactionDate == null || LocalDateTime.now().isAfter(transactionDate.plusDays(3));
    }
}
