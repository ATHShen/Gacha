package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.command.util.Command;
import com.oopsjpeg.gacha.command.util.CommandManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Created by oopsjpeg on 3/13/2019.
 */
public class HelpCommand extends Command {
    public HelpCommand(CommandManager manager) {
        super(manager, "help");
        aliases = new String[]{"?", "about"};
        description = "Show helpful information about Gacha.";
    }

    @Override
    public void execute(Message message, String alias, String[] args) {
        MessageChannel channel = message.getChannel();
        User author = message.getAuthor();
        User self = message.getJDA().getSelfUser();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Util.getColor(self, channel.getIdLong()));
        builder.setAuthor("Gacha Commands", null, self.getAvatarUrl());
        builder.setDescription(getParent().getCommands().stream()
                .sorted(Comparator.comparing(Command::getName))
                .map(c -> "`" + getManager().getPrefix() + c.getName() + "`: " + c.getDescription())
                .collect(Collectors.joining("\n")));
        Util.sendEmbed(channel, "Viewing all **Gacha** commands.", builder.build());
    }
}
