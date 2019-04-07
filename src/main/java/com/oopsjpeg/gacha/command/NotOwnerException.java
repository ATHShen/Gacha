package com.oopsjpeg.gacha.command;

/**
 * Created by oopsjpeg on 1/30/2019.
 */
public class NotOwnerException extends CommandException {
    public NotOwnerException(Command command) {
        super("You must be the server owner to use this.", command);
    }
}
