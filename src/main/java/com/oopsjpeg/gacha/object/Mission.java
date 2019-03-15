package com.oopsjpeg.gacha.object;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by oopsjpeg on 3/14/2019.
 */
@RequiredArgsConstructor
@Getter
public class Mission {
    private final int id;
    private String name;
    private String description;
    private int cooldown;
    private int[] dependencies;
    private MissionTask[] tasks;
}
