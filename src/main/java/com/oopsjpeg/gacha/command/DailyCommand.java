package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.command.util.Command;
import com.oopsjpeg.gacha.command.util.CommandManager;
import com.oopsjpeg.gacha.object.UserInfo;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.time.LocalDateTime;

public class DailyCommand extends Command {
    public DailyCommand(CommandManager manager) {
        super(manager, "daily");
        description = "Collect your daily bonus.";
        registeredOnly = true;
    }

    @Override
    public void execute(Message message, String alias, String[] args) {
        MessageChannel channel = message.getChannel();
        User author = message.getAuthor();
        UserInfo info = getParent().getUser(author.getIdLong());

        if (!info.hasDaily())
            Util.sendError(channel, author, "Your **Daily** is available in "
                    + Util.timeDiff(LocalDateTime.now(), info.getDailyDate().plusDays(1)) + ".");
        else {
            int amount = 500;
            info.addCrystals(amount);
            info.setDailyDate(LocalDateTime.now());
            Util.send(channel, Util.nameThenId(author) + " collected **C" + Util.comma(amount) + "** from **Daily**.", Color.GREEN);
            Gacha.getInstance().getMongo().saveUser(info);
        }
    }
}
