package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.wrapper.UserWrapper;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.time.LocalDateTime;

public class AccountCommand implements Command {
	@Override
	public void execute(IMessage message, String alias, String[] args) {
		Gacha gacha = Gacha.getInstance();
		IChannel channel = message.getChannel();
		IUser author = message.getAuthor();
		UserWrapper info = gacha.getUser(author);

		EmbedBuilder builder = new EmbedBuilder();
		builder.withAuthorName(author.getName());
		builder.withAuthorIcon(author.getAvatarURL());
		builder.withColor(Util.getColor(author, channel));

		if (!info.getFlags().isEmpty())
			builder.appendDesc("**" + info.getFlags().size() + "** flag(s) on account.\n");

		builder.appendDesc("**Crystals**: C" + Util.comma(info.getCrystals()) + "\n");

		if (info.hasDaily())
			builder.appendDesc("**Daily** is available.\n");
		else
			builder.appendDesc("**Daily** is available in " +
					Util.timeDiff(LocalDateTime.now(), info.getDaily().plusDays(1)) + ".\n");

		builder.appendField("Cards", Util.comma(info.getCards().size()), true);

		if (info.getVcCrystals() < 1500 * gacha.getVCCAndCIMGMultiplier()
				&& info.getVcCrystals() > 0)
			builder.appendField("VCC Earned", Math.round(((float) info.getVcCrystals() / (1500
					* gacha.getVCCAndCIMGMultiplier()) * 100)) + "%", true);
		else if (info.getVcDate() != null && LocalDateTime.now().isBefore(info.getVcDate().plusDays(1)))
			builder.appendField("VCC Reset", Util.timeDiff(LocalDateTime.now(), info.getVcDate().plusDays(1)), true);
		if (!info.getCimgDatas().isEmpty())
			builder.appendField("IMGC Earned",
					Math.round(((float) info.getCimgDatas().values().stream()
							.filter(c -> !c.canEarn()).count() / gacha.getCimgs().size()) * 100) + "%", true);

		Bufferer.sendMessage(channel, "Viewing " + Util.nameThenID(author) + "'s account.", builder.build());
	}

	@Override
	public String getName() {
		return "account";
	}
}
