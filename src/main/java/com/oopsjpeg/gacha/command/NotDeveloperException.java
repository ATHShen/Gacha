package com.oopsjpeg.gacha.command;

/**
 * Created by oopsjpeg on 1/30/2019.
 */
public class NotDeveloperException extends CommandException {
    public NotDeveloperException(Command command) {
        super("You must be the bot's developer to use this.", command);
    }
}
