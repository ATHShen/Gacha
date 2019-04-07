package com.oopsjpeg.gacha.command;

/**
 * Created by oopsjpeg on 1/30/2019.
 */
public class NotAdminException extends CommandException {
    public NotAdminException(Command command) {
        super("You must be a server admin to use this.", command);
    }
}
