package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.MongoMaster;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.handle.obj.IMessage;

import java.io.File;
import java.time.LocalDateTime;

public class ForceBackupCommand implements Command {
	@Override
	public void execute(IMessage message, String alias, String[] args) {
		MongoMaster mongo = Gacha.getInstance().getMongo();
		File file = mongo.getBackupFile(LocalDateTime.now());
		mongo.forceBackup(file);
		Bufferer.sendMessage(message.getChannel(), "Backup to `" + file + "` complete.");
	}

	@Override
	public String getName() {
		return "forcebackup";
	}

	@Override
	public boolean isOwnerOnly() {
		return true;
	}
}
