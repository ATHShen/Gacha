package com.oopsjpeg.gacha.object.user;

import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.util.Constants;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Created by oopsjpeg on 3/10/2019.
 */
public class UserBank {
    @Getter @Setter private int crystals;
    @Getter @Setter private LocalDateTime interestDate;
    @Getter @Setter private LocalDateTime withdrawalDate;

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
        crystals += crystals * Constants.BANK_RATE;
    }

    public boolean hasInterest() {
        return interestDate == null || LocalDateTime.now().isAfter(interestDate.plusDays(1));
    }

    public boolean hasWithdrawal() {
        return withdrawalDate == null || LocalDateTime.now().isAfter(withdrawalDate.plusDays(Constants.BANK_COOLDOWN));
    }

    public String nextWithdrawal() {
        return Util.timeDiff(LocalDateTime.now(), getWithdrawalDate().plusDays(Constants.BANK_COOLDOWN));
    }
}
