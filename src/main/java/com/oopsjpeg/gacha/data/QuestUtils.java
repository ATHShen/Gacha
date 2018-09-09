package com.oopsjpeg.gacha.data;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.data.impl.Quest;
import com.oopsjpeg.gacha.wrapper.UserWrapper;
import com.oopsjpeg.roboops.framework.Bufferer;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.time.LocalDateTime;

public class QuestUtils {
	public static void check(IChannel channel, IUser user) {
		UserWrapper info = Gacha.getInstance().getUser(user);
		for (int i = 0; i < info.getActiveQuestDatas().size(); i++) {
			UserWrapper.QuestData data = info.getActiveQuestDatas().get(i);
			if (data.isComplete()) {
				Bufferer.sendMessage(channel, Util.nameThenID(user) + " has completed **" + data.getQuest().getTitle() + "**.");
				data.setCompleteDate(LocalDateTime.now());
				data.setActive(false);
				info.giveCrystals(data.getQuest().getReward());

				for (UserWrapper.QuestData data2 : info.getActiveQuestDatas())
					for (Quest.Condition cond : data2.getConditionsByType(Quest.ConditionType.QUEST_ANY))
						data2.setProgress(cond, 0, DataUtils.getInt(data.getProgress(cond, 0)) + 1);

				Gacha.getInstance().getMongo().saveUser(info);

				i = 0;
			}
		}
	}
}
