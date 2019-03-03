package com.oopsjpeg.gacha.manager;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.ReadyEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class StatusManager extends Manager {
	private static final List<String> games = new ArrayList<>();

	static {
		games.addAll(Arrays.asList(
				"Azur Lane",
				"DanMachi - MEMORIA FREESE",
				"Danganronpa: Trigger Happy Havoc",
				"Danganronpa 2: Goodbye Despair",
				"Danganronpa V3: Killing Harmony",
				"Danganronpa Another Episode: Ultra Despair Girls",
				"Fate/Grand Order",
				"Girls' Frontline",
				"KanColle",
				"Touhou 6: Embodiment of Scarlet Devil",
				"Touhou 7: Perfect Cherry Blossom",
				"Touhou 8: Imperishable Night",
				"Touhou 9: Phantasmagoria of Flower View",
				"Touhou 10: Mountain of Faith",
				"Touhou 11: Subterranean Animism",
				"Touhou 12: Undefined Fantastic Object",
				"Touhou 13: Ten Desires",
				"Touhou 14: Double Dealing Character",
				"Touhou 15: Legacy of Lunatic Kingdom",
				"Touhou 16: Hidden Star in Four Seasons"
		));
	}

	public StatusManager(Gacha parent) {
		super(parent);
	}

	@Override
	public void onReady(ReadyEvent event) {
		Gacha.LOGGER.info("Gacha is ready.");

		Gacha.SCHEDULER.scheduleAtFixedRate(() -> event.getJDA().getPresence().setGame(
				Game.playing(games.get(Util.RANDOM.nextInt(games.size())))), 0, 10, TimeUnit.MINUTES);
	}
}
