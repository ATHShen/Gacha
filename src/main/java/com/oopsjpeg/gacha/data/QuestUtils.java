package com.oopsjpeg.gacha.data;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.wrapper.UserWrapper;
import com.oopsjpeg.roboops.framework.Bufferer;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.time.LocalDateTime;

public class QuestUtils {
	public static void check(IChannel channel, IUser user) {
		UserWrapper info = Gacha.getInstance().getUser(user);
		for (UserWrapper.QuestData data : info.getQuestDatas())
			if (data.getCompleteDate() == null && data.isComplete()) {
				Bufferer.sendMessage(channel, Util.nameThenID(user) + " has completed **" + data.getQuest().getTitle() + "**.");
				data.setCompleteDate(LocalDateTime.now());
				data.setActive(false);
				info.giveCrystals(data.getQuest().getReward());
				Gacha.getInstance().getMongo().saveUser(info);
			}
	}
}
