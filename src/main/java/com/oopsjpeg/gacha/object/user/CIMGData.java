package com.oopsjpeg.gacha.object.user;

import com.oopsjpeg.gacha.Gacha;
import sx.blah.discord.handle.obj.IMessage;

import java.time.LocalDateTime;

public class CIMGData {
	private final int group;
	private long messageID;
	private int reward;
	private LocalDateTime sentDate;

	public CIMGData(int group) {
		this.group = group;
	}

	public boolean canEarn() {
		return sentDate == null || LocalDateTime.now().isAfter(sentDate.plusDays(1));
	}

	public int getGroup() {
		return group;
	}

	public long getMessageID() {
		return messageID;
	}

	public void setMessageID(long messageID) {
		this.messageID = messageID;
	}

	public IMessage getMessage() {
		return Gacha.getInstance().getClient().getMessageByID(messageID);
	}

	public int getReward() {
		return reward;
	}

	public void setReward(int reward) {
		this.reward = reward;
	}

	public LocalDateTime getSentDate() {
		return sentDate;
	}

	public void setSentDate(LocalDateTime sentDate) {
		this.sentDate = sentDate;
	}
}