package com.oopsjpeg.gacha.command.util;

/**
 * Created by oopsjpeg on 1/30/2019.
 */
public class InvalidPermsException extends CommandException {
    public InvalidPermsException(Command command) {
        super("Invalid permission(s).", command);
    }
}
