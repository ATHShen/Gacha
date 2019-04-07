package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.SubscribeEvent;

import java.util.Arrays;

/**
 * Created by oopsjpeg on 2/28/2019.
 */
@Getter
@Setter
public class CommandListener {
    @SubscribeEvent
    public void onMessageReceived(MessageReceivedEvent event) {
        MessageChannel channel = event.getChannel();
        User user = event.getAuthor();
        Message message = event.getMessage();
        String content = message.getContentRaw();
        String[] split = content.split(" ");

        if (split[0].toLowerCase().startsWith(Gacha.getInstance().getPrefix().toLowerCase())) {
            String alias = split[0].replaceFirst(Gacha.getInstance().getPrefix(), "");
            String[] args = Arrays.copyOfRange(split, 1, split.length);
            Command command = Command.getFromAlias(alias);
            if (command != null) {
                try {
                    command.execute0(message, alias, args);
                } catch (CommandException error) {
                    Util.sendError(channel, user, error.getMessage());
                }
            }
        }
    }
}
