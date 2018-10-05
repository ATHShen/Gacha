package com.oopsjpeg.gacha;

import sx.blah.discord.handle.obj.IChannel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CIMGBox extends ArrayList<List<IChannel>> {
	public CIMGBox() { }

	public CIMGBox(Collection<? extends List<IChannel>> c) {
		super(c);
	}

	public boolean contains(IChannel channel) {
		return stream().anyMatch(group -> group.contains(channel));
	}

	public int groupOf(IChannel channel) {
		return indexOf(stream().filter(group -> group.contains(channel)).findAny().orElse(null));
	}
}
