package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.command.util.Command;
import com.oopsjpeg.gacha.command.util.CommandManager;
import com.oopsjpeg.gacha.object.user.UserInfo;
import com.oopsjpeg.gacha.util.Constants;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * Created by oopsjpeg on 3/10/2019.
 */
public class DescriptionCommand extends Command {
    public DescriptionCommand(CommandManager manager) {
        super(manager, "description");
        aliases = new String[]{"desc", "bio"};
        description = "Update your profile description.";
        registeredOnly = true;
    }

    @Override
    public void execute(Message message, String alias, String[] args) {
        MessageChannel channel = message.getChannel();
        User author = message.getAuthor();
        UserInfo info = getParent().getUser(author.getIdLong());

        if (args.length == 0)
            Util.sendError(channel, author, "You must enter a new profile description.");
        else if (args[0].equalsIgnoreCase("clear")) {
            info.setDescription(null);
            Util.sendSuccess(channel, author, "Your profile description has been cleared.");
            getParent().getMongo().saveUser(info);
        } else {
            String description = String.join(" ", args);
            if (description.length() > Constants.DESCRIPTION_MAX)
                Util.sendError(channel, author, "Your profile description must be at most " + Constants.DESCRIPTION_MAX + " characters.");
            else {
                info.setDescription(description);
                Util.sendSuccess(channel, author, "Your profile description has been updated.");
                getParent().getMongo().saveUser(info);
            }
        }
    }
}
