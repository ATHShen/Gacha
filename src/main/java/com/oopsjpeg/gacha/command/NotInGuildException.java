package com.oopsjpeg.gacha.command;

/**
 * Created by oopsjpeg on 1/30/2019.
 */
public class NotInGuildException extends CommandException {
    public NotInGuildException(Command command) {
        super("You must be in a guild to use this.", command);
    }
}
