package com.oopsjpeg.gacha.command.util;

/**
 * Created by oopsjpeg on 1/30/2019.
 */
public class NotRegisteredException extends CommandException {
    public NotRegisteredException(Command command) {
        super("You must be registered with Gacha to use this.", command);
    }
}
