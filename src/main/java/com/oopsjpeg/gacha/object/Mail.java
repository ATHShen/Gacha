package com.oopsjpeg.gacha.object;

import com.oopsjpeg.gacha.Gacha;
import sx.blah.discord.handle.obj.IUser;

import java.util.ArrayList;
import java.util.List;

public class Mail {
	private Content content;
	private Gift gift;

	public Content getContent() {
		return content;
	}

	public void setContent(Content content) {
		this.content = content;
	}

	public Gift getGift() {
		return gift;
	}

	public void setGift(Gift gift) {
		this.gift = gift;
	}

	public boolean hasGift() {
		return getGift() != null;
	}

	public static class Content {
		private long authorID;
		private String subject;
		private String body;

		public IUser getAuthor() {
			return Gacha.getInstance().getClient().getUserByID(authorID);
		}

		public long getAuthorID() {
			return authorID;
		}

		public void setAuthorID(long authorID) {
			this.authorID = authorID;
		}

		public String getSubject() {
			return subject;
		}

		public void setSubject(String subject) {
			this.subject = subject;
		}

		public String getBody() {
			return body;
		}

		public void setBody(String body) {
			this.body = body;
		}
	}

	public static class Gift {
		private int crystals = 0;
		private List<Card> cards = new ArrayList<>();

		public int getCrystals() {
			return crystals;
		}

		public void setCrystals(int crystals) {
			this.crystals = crystals;
		}

		public List<Card> getCards() {
			return cards;
		}

		public void setCards(List<Card> cards) {
			this.cards = cards;
		}
	}
}
