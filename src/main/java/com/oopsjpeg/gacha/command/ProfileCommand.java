package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.command.util.Command;
import com.oopsjpeg.gacha.command.util.CommandManager;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.user.UserBank;
import com.oopsjpeg.gacha.object.user.UserInfo;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
        UserBank bank = info.getBank();

        EmbedBuilder builder = new EmbedBuilder();

        int star = info.getCards().isEmpty() ? 1 : Collections.max(info.getCards().stream()
                .map(Card::getStar)
                .collect(Collectors.toList()));

        builder.setAuthor(author.getName() + " (" + Util.star(star) + ")", null, author.getAvatarUrl());
        builder.setColor(Util.getColor(author, channel.getIdLong()));
        builder.setDescription(info.getDescription());

        // Cards
        builder.addField("Cards", Util.comma(info.getCards().size()), true);
        // Crystals
        String crystals = Util.comma(info.getCrystals() + bank.getCrystals());
        String fromBank = bank.hasCrystals() ? " (" + Util.comma(bank.getCrystals()) + " from bank)" : "";
        builder.addField("Crystals", crystals + fromBank, true);
        // Timelies
        List<String> timelies = new ArrayList<>();
        timelies.add(info.hasDaily() ? "**Daily** is available." : "**Daily** is available in " + Util.timeDiff(LocalDateTime.now(), info.getDailyDate().plusDays(1)) + ".");
        timelies.add(info.hasWeekly() ? "**Weekly** is available." : "**Weekly** is available in " + Util.timeDiff(LocalDateTime.now(), info.getWeeklyDate().plusWeeks(1)) + ".");
        builder.addField("Timelies", String.join("\n", timelies), false);

        Util.sendEmbed(channel, "Viewing " + Util.nameThenId(author) + "'s profile.", builder.build());
    }
}
