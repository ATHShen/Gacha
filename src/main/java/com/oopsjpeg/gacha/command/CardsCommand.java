package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.command.util.Command;
import com.oopsjpeg.gacha.command.util.CommandManager;
import com.oopsjpeg.gacha.object.user.UserInfo;
import com.oopsjpeg.gacha.util.CardQuery;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CardsCommand extends Command {
    public CardsCommand(CommandManager manager) {
        super(manager, "cards");
        usage = "[page/\"all\"]";
        description = "View your cards.";
        registeredOnly = true;
    }

    @Override
    public void execute(Message message, String alias, String[] args) throws IOException {
        MessageChannel channel = message.getChannel();
        User author = message.getAuthor();
        UserInfo info = getParent().getUser(author.getIdLong());

        if (info.getCards().isEmpty())
            Util.sendError(channel, author, "You do not have any cards.");
        else if (args.length >= 1 && args[0].equalsIgnoreCase("all")) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            baos.write((author.getName() + "'s Cards (" + Util.comma(info.getCards().size()) + ")\n"
                    + new CardQuery(info.getCards()).raw())
                    .getBytes(StandardCharsets.UTF_8));

            channel.sendFile(new ByteArrayInputStream(baos.toByteArray()),
                    "Cards_" + Util.fileName(LocalDateTime.now().toString()) + ".txt",
                    new MessageBuilder("Viewing all of " + Util.nameThenId(author) + "'s cards.").build()).queue();

            baos.close();
        } else {
            CardQuery query = new CardQuery(info.getCards());

            List<String> filters = new ArrayList<>();
            for (String arg : args) {
                arg = arg.toLowerCase();

                if (arg.contains("-ident")) {
                    query.filter(card -> query.get().stream().filter(card::equals).count() >= 2);
                    filters.add("Identical");
                }
            }

            int page = 1;
            if (args.length >= 1 && Util.isDigits(args[args.length - 1]))
                page = Integer.parseInt(args[args.length - 1]);

            if (page <= 0 || page > query.pages())
                Util.sendError(channel, author, "Invalid page.");
            else {
                EmbedBuilder b = new EmbedBuilder();
                b.setAuthor(author.getName() + "'s Cards (" + Util.comma(info.getCards().size()) + ")", null, author.getAvatarUrl());
                b.setColor(Util.getColor(author, channel.getIdLong()));
                b.setDescription(query.page(page).format());
                b.setFooter("Page " + page + " / " + Util.comma(query.pages()) + (filters.isEmpty() ? ""
                        : " [Filter: " + String.join(", ", filters) + "]"), null);

                Util.sendEmbed(channel, "Viewing " + Util.nameThenId(author) + "'s cards.", b.build());
            }
        }
    }
}
