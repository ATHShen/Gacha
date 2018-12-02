package com.oopsjpeg.gacha.util;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.object.Event;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class EventUtils {
	public static String listEventsByDate(List<Event> events) {
		Map<LocalDateTime, List<Event>> map = new HashMap<>();

		for (Event e : events) {
			LocalDateTime ldt = e.getStartDate();
			LocalDateTime key = LocalDateTime.of(ldt.getYear(), ldt.getMonth(), ldt.getDayOfMonth(), 0, 0);
			if (!map.containsKey(key)) map.put(key, new ArrayList<>());
			map.get(key).add(e);
		}

		return map.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).map(e -> {
			LocalDateTime ldt = e.getKey();
			String output = "**" + ldt.getYear() + "/" + ldt.getMonthValue() + "/" + ldt.getDayOfMonth() + "**\n";
			for (Event evt : e.getValue())
				output += "- " + evt.format() + "\n";
			return output;
		}).collect(Collectors.joining("\n"));
	}

	public static List<Event> activeEvents() {
		return Gacha.getInstance().getEvents().stream()
				.filter(e -> e.getState() == Event.ACTIVE).collect(Collectors.toList());
	}

	private static float doubleGrind() {
		return activeEvents().stream().anyMatch(e -> e.getType() == Event.Type.DOUBLE_GRIND) ? 2 : 1;
	}

	public static int gacha() {
		if (activeEvents().stream().anyMatch(e -> e.getType() == Event.Type.GACHA_DISCOUNT_25))
			return 375;
		else if (activeEvents().stream().anyMatch(e -> e.getType() == Event.Type.GACHA_DISCOUNT_50))
			return 250;
		return 500;
	}

	public static int vcc() {
		return Math.round(30 * doubleGrind());
	}

	public static int vccMax() {
		return Math.round(1000 * doubleGrind());
	}

	public static int cimg() {
		return Math.round(125 * doubleGrind());
	}
}
