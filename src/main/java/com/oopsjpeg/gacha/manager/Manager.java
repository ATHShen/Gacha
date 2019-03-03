package com.oopsjpeg.gacha.manager;

import com.oopsjpeg.gacha.Gacha;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * Created by oopsjpeg on 2/28/2019.
 */
public abstract class Manager extends ListenerAdapter {
    private final Gacha parent;

    public Manager(Gacha parent) {
        this.parent = parent;
    }

    public Gacha getParent() {
        return parent;
    }
}
