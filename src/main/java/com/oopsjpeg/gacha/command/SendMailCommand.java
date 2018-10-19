package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.object.user.UserMail;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.handle.obj.IMessage;

public class SendMailCommand implements Command {
	private final Gacha instance = Gacha.getInstance();

	@Override
	public void execute(IMessage message, String alias, String[] args) {
		String id = args[0];
		if (instance.getLinkedMail().containsKey(id))
			Gacha.getInstance().getUsers().forEach(info -> info.sendMail(new UserMail(id)));
	}

	@Override
	public String getName() {
		return "sendmail";
	}

	@Override
	public boolean isOwnerOnly() {
		return true;
	}
}
