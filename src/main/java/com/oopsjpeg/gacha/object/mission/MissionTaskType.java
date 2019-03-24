package com.oopsjpeg.gacha.object.mission;

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
        public String message(MissionTask task) {
            return "Own " + Gacha.getInstance().getCard((int) task.getModifiers()[0]).getName() + ".";
        }

        @Override
        public boolean isComplete(MissionProgress progress) {
            return progress.getUserInfo().getCardIds().contains()
        }
    };

    public abstract String message(MissionTask task);

    public abstract boolean isComplete(MissionProgress progress);
}
