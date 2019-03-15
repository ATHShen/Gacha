package com.oopsjpeg.gacha.object;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by oopsjpeg on 3/14/2019.
 */
@RequiredArgsConstructor
@Getter
public class MissionTask {
    private MissionTaskType type;
    private Object[] modifiers;
}
