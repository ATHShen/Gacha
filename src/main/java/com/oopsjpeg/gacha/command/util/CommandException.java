package com.oopsjpeg.gacha.command.util;

/**
 * Created by oopsjpeg on 1/30/2019.
 */
public class CommandException extends RuntimeException {
    private final Command command;

    public CommandException(String message, Command command) {
        super(message);
        this.command = command;
    }

    public Command getCommand() {
        return command;
    }
}
