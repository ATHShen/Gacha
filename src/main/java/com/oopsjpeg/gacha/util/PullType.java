package com.oopsjpeg.gacha.util;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.object.Card;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Created by oopsjpeg on 3/12/2019.
 */
@Getter @RequiredArgsConstructor
public enum PullType {
    STANDARD("Standard", 1000) {
        @Override
        public Card get() {
            List<Card> pool;
            float f = Util.RANDOM.nextFloat();

            if (f <= 0.0075) pool = Gacha.getInstance().getCardsByStar(5);
            else if (f <= 0.0275) pool = Gacha.getInstance().getCardsByStar(4);
            else if (f <= 0.09) pool = Gacha.getInstance().getCardsByStar(3);
            else if (f <= 0.28) pool = Gacha.getInstance().getCardsByStar(2);
            else pool = Gacha.getInstance().getCardsByStar(1);

            pool.removeIf(c -> c.isSpecial() || c.isExclusive());

            return pool.get(Util.RANDOM.nextInt(pool.size()));
        }
    };

    private final String name;
    private final int cost;

    public abstract Card get();
}
