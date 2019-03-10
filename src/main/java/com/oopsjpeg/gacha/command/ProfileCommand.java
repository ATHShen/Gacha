package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.command.util.Command;
import com.oopsjpeg.gacha.command.util.CommandManager;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.user.UserInfo;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.stream.Collectors;

public class ProfileCommand extends Command {
    public ProfileCommand(CommandManager manager) {
        super(manager, "profile");
        aliases = new String[]{"account"};
        description = "View your profile.";
        registeredOnly = true;
    }


    @Override
    public void execute(Message message, String alias, String[] args) {
        MessageChannel channel = message.getChannel();
        User author = message.getAuthor();
        UserInfo info = getParent().getUser(author.getIdLong());

        EmbedBuilder builder = new EmbedBuilder();

        int star = info.getCards().isEmpty() ? 1 : Collections.max(info.getCards().stream()
                .map(Card::getStar)
                .collect(Collectors.toList()));

        builder.setAuthor(author.getName() + " (" + Util.star(star) + ")", null, author.getAvatarUrl());
        builder.setColor(Util.getColor(author, channel.getIdLong()));

        // Description
        // Crystals
        builder.appendDescription("**Crystals**: C" + Util.comma(info.getCrystals()) + "\n");
        // Daily
        if (info.hasDaily())
            builder.appendDescription("**Daily** is available.\n");
        else
            builder.appendDescription("**Daily** is available in " + Util.timeDiff(
                    LocalDateTime.now(), info.getDailyDate().plusDays(1)) + ".\n");
        // Weekly
        if (info.hasWeekly())
            builder.appendDescription("**Weekly** is available.\n");
        else
            builder.appendDescription("**Weekly** is available in " + Util.timeDiff(
                    LocalDateTime.now(), info.getWeeklyDate().plusWeeks(1)) + ".\n");

        // Fields
        // Cards
        builder.addField("Cards", Util.comma(info.getCards().size()), true);

        Util.sendEmbed(channel, "Viewing " + Util.nameThenId(author) + "'s profile.", builder.build());
    }
}
