package com.oopsjpeg.gacha.object.user;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.object.Mail;

import java.util.UUID;

public class UserMail extends Mail {
	private final UUID uuid;
	private boolean giftCollected = false;
	private String linkID;

	public UserMail(UUID uuid) {
		this.uuid = uuid;
	}

	public UserMail(UUID uuid, String linkID) {
		this(uuid);
		this.linkID = linkID;
	}

	@Override
	public Content getContent() {
		return linkID == null ? super.getContent() : Gacha.getInstance().getLinkedMail().get(linkID).getContent();
	}

	@Override
	public Gift getGift() {
		return linkID == null ? super.getGift() : Gacha.getInstance().getLinkedMail().get(linkID).getGift();
	}

	public UUID getUUID() {
		return uuid;
	}

	public boolean isGiftCollected() {
		return giftCollected;
	}

	public void setGiftCollected(boolean giftCollected) {
		this.giftCollected = giftCollected;
	}

	public void collectGift(UserInfo info) {
		if (!isGiftCollected()) {
			Gift gift = getGift();
			if (gift.getCrystals() > 0) info.addCrystals(gift.getCrystals());
			giftCollected = true;
		}
	}

	public String getLinkID() {
		return linkID;
	}

	public void setLinkID(String linkID) {
		this.linkID = linkID;
	}
}
