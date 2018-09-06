package com.oopsjpeg.gacha.data;

import com.oopsjpeg.gacha.data.impl.Event;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class EventUtils {
	public static String listEventsByDate(List<Event> events) {
		Map<LocalDateTime, List<Event>> map = new HashMap<>();

		for (Event e : events) {
			LocalDateTime ldt = e.getStartTime();
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
}
