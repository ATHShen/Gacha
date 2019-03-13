package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.command.util.Command;
import com.oopsjpeg.gacha.command.util.CommandManager;
import com.oopsjpeg.gacha.object.user.UserInfo;
import com.oopsjpeg.gacha.util.Constants;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.time.LocalDateTime;

public class WeeklyCommand extends Command {
    public WeeklyCommand(CommandManager manager) {
        super(manager, "weekly");
        description = "Collect your weekly bonus.";
        registeredOnly = true;
    }

    @Override
    public void execute(Message message, String alias, String[] args) {
        MessageChannel channel = message.getChannel();
        User author = message.getAuthor();
        UserInfo info = getParent().getUser(author.getIdLong());

        if (!info.hasWeekly())
            Util.sendError(channel, author, "Your **Weekly** is available in "
                    + Util.timeDiff(LocalDateTime.now(), info.getWeeklyDate().plusWeeks(1)) + ".");
        else {
            int amount = Constants.TIMELY_WEEK;
            info.addCrystals(amount);
            info.setWeeklyDate(LocalDateTime.now());
            Util.sendSuccess(channel, author, "Collected **" + Util.comma(amount) + "** from **Weekly**.");
            getParent().getMongo().saveUser(info);
        }
    }
}
