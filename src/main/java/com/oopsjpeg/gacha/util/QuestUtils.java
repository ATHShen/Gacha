package com.oopsjpeg.gacha.util;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.object.Quest;
import com.oopsjpeg.gacha.object.user.QuestData;
import com.oopsjpeg.gacha.object.user.UserInfo;
import com.oopsjpeg.roboops.framework.Bufferer;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.time.LocalDateTime;
import java.util.HashMap;

public class QuestUtils {
	public static void check(IChannel channel, IUser user) {
		UserInfo info = Gacha.getInstance().getUser(user);
		if (info != null) for (int i = 0; i < info.getActiveQuestDatas().size(); i++) {
			QuestData qd = info.getActiveQuestDatas().get(i);
			if (qd.isComplete()) {
				Bufferer.sendMessage(channel, Util.nameThenID(user) + " has completed **" + qd.getQuest().getTitle() + "**.");
				qd.setCompleteDate(LocalDateTime.now());
				qd.setProgress(new HashMap<>());
				info.addCrystals(qd.getQuest().getReward());

				for (QuestData data : info.getActiveQuestDatas())
					for (Quest.Condition cond : data.getConditionsByType(Quest.ConditionType.QUEST_ANY))
						data.setProgress(cond, 0, DataUtils.getInt(data.getProgress(cond, 0)) + 1);

				Gacha.getInstance().getMongo().saveUser(info);

				i = 0;
			}
		}
	}
}
