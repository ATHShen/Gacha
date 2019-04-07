package com.oopsjpeg.gacha.command;

/**
 * Created by oopsjpeg on 1/30/2019.
 */
public class CommandException extends RuntimeException {
    private final Command command;

    public CommandException(Throwable cause, Command command) {
        super(cause);
        this.command = command;
    }

    public CommandException(String message, Command command) {
        super(message);
        this.command = command;
    }

    public Command getCommand() {
        return command;
    }
}
