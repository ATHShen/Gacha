package com.oopsjpeg.gacha.handler;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.StatusType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class StatusHandler {
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

	@EventSubscriber
	public void onReady(ReadyEvent evt) {
		Gacha.getInstance().postBuild();
		Gacha.LOGGER.info("Gacha is ready.");

		Gacha.SCHEDULER.scheduleAtFixedRate(() ->
				evt.getClient().changePresence(StatusType.ONLINE, ActivityType.PLAYING,
						games.get(Util.RANDOM.nextInt(games.size()))), 0, 10, TimeUnit.MINUTES);
	}
}
