package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.object.Mail;
import com.oopsjpeg.gacha.object.user.UserInfo;
import com.oopsjpeg.gacha.object.user.UserMail;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

public class MailCommand implements Command {
	private final Gacha instance = Gacha.getInstance();

	@Override
	public void execute(IMessage message, String alias, String[] args) {
		IChannel channel = message.getChannel();
		IUser author = message.getAuthor();
		UserInfo info = instance.getUser(author);

		if (info.getMail().isEmpty())
			// User doesn't have any mail
			Util.sendError(channel, author, "you do not have any mail.");
		else if (args.length >= 1 && args[0].equalsIgnoreCase("collect")) {
			// Collect gifts from mail
			if (args.length >= 2 && args[1].equalsIgnoreCase("all"))
				// Collect all gifts available
				collectAllGifts(channel, info);
			else if (args.length >= 2) {
				// Collect specific gift from mail
				int index = Integer.parseInt(args[1]);
				if (index <= 0 || index > info.getMail().size())
					Util.sendError(channel, author, "invalid mail index.");
				else
					collectGift(channel, info, info.getMail(index - 1));
			} else if (info.getLastMail() != null && info.getLastMail().hasGift()
					&& !info.getLastMail().isGiftCollected())
				// Collect gift from last mail viewed
				collectGift(channel, info, info.getLastMail());
			else // Default to collecting all gifts
				collectAllGifts(channel, info);
		} else {
			// View specific mail / most recent mail
			int index = args.length == 0 ? info.getMail().size() : Integer.parseInt(args[0]);
			if (index <= 0 || index > info.getMail().size())
				Util.sendError(channel, author, "invalid mail index.");
			else {
				UserMail mail = info.getMail(index - 1);
				Bufferer.sendMessage(channel, "Viewing mail **" + index + "** of **"
						+ info.getMail().size() + "** for " + Util.nameThenID(author) + ".", embed(mail));
			}
		}
	}

	@Override
	public String getName() {
		return "mail";
	}

	@Override
	public boolean isOwnerOnly() {
		return true;
	}

	private EmbedObject embed(UserMail mail) {
		IUser mailAuthor = mail.getContent().getAuthor();

		EmbedBuilder builder = new EmbedBuilder();
		builder.withAuthorName("Sent by " + mailAuthor.getName() + "#" + mailAuthor.getDiscriminator());
		builder.withAuthorIcon(mailAuthor.getAvatarURL());
		builder.withTitle(mail.getContent().getSubject());
		builder.withDesc(mail.getContent().getBody());

		if (mail.getGift() != null) {
			Mail.Gift gift = mail.getGift();
			String giftField = "";
			if (gift.getCrystals() > 0)
				giftField = "Crystals: " + gift.getCrystals() + "\n";
			builder.appendField("Gift", giftField, false);
		}

		return builder.build();
	}

	private void collectAllGifts(IChannel channel, UserInfo info) {
		IUser user = info.getUser();
		if (info.getMail().stream().allMatch(mail -> !mail.hasGift() || mail.isGiftCollected()))
			Util.sendError(channel, user, "you do not have any gifts to collect.");
		else {
			info.getMail().forEach(mail -> mail.collectGift(info));
			instance.getMongo().saveUser(info);
			Bufferer.sendMessage(channel, Util.nameThenID(user) + " collected all their mail gifts.");
		}
	}

	private void collectGift(IChannel channel, UserInfo info, UserMail mail) {
		IUser user = info.getUser();
		if (mail.getGift() == null)
			Util.sendError(channel, user, "this mail does not have a gift.");
		else if (mail.isGiftCollected())
			Util.sendError(channel, user, "you already collected this gift.");
		else {
			mail.collectGift(info);
			instance.getMongo().saveUser(info);
			Bufferer.sendMessage(channel, Util.nameThenID(user) + " collected a mail gift.");
		}
	}
}
