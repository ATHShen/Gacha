package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;

/**
 * Created by oopsjpeg on 1/30/2019.
 */
public class InvalidUsageException extends CommandException {
    public InvalidUsageException(Command command) {
        super(String.format("Invalid usage. The correct usage is: `%s %s %s`.",
                Gacha.getInstance().getPrefix(), command.getName(), command.getUsage()), command);
    }
}
