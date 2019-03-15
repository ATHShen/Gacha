package com.oopsjpeg.gacha.object;

import com.oopsjpeg.gacha.Gacha;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by oopsjpeg on 3/14/2019.
 */
@RequiredArgsConstructor
@Getter
public enum MissionTaskType {
    CARD_OWN {
        @Override
        public String message(Object[] modifiers) {
            return "Own " + Gacha.getInstance().getCard((int) modifiers[0]).getName() + ".";
        }
    };

    public abstract String message(Object[] modifiers);
}
