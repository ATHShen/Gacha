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
			UserWrapper.QuestData qd = info.getActiveQuestDatas().get(i);
			if (qd.isComplete()) {
				Bufferer.sendMessage(channel, Util.nameThenID(user) + " has completed **" + qd.getQuest().getTitle() + "**.");
				qd.setCompleteDate(LocalDateTime.now());
				qd.setActive(false);
				info.giveCrystals(qd.getQuest().getReward());

				for (UserWrapper.QuestData data : info.getActiveQuestDatas())
					for (Quest.Condition cond : data.getConditionsByType(Quest.ConditionType.QUEST_ANY))
						data.setProgress(cond, 0, DataUtils.getInt(data.getProgress(cond, 0)) + 1);

				Gacha.getInstance().getMongo().saveUser(info);

				i = 0;
			}
		}
	}
}
