package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.command.util.Command;
import com.oopsjpeg.gacha.command.util.CommandManager;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * Created by oopsjpeg on 3/2/2019.
 */
public class RegisterCommand extends Command {
    public RegisterCommand(CommandManager manager) {
        super(manager, "register");
    }

    @Override
    public void execute(Message message, String alias, String[] args) {
        MessageChannel channel = message.getChannel();
        User author = message.getAuthor();

        if (getParent().getUsers().containsKey(author.getIdLong()))
            Util.sendError(channel, author, "You are already registered with Gacha.");
        else {
            Util.send(channel, author, "You are now registered with Gacha!",
                    "Check your profile with `/profile`."
                            + "\nCollect your daily bonus with `/daily`."
                            + "\nLearn how to forge new cards with `/forge`.");
            getParent().getMongo().saveUser(getParent().registerUser(author.getIdLong()));
        }
    }
}
