package com.oopsjpeg.gacha.command.util;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.SubscribeEvent;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by oopsjpeg on 2/28/2019.
 */
@Getter
@Setter
public class CommandManager extends ArrayList<Command> {
    private String prefix;

    public CommandManager(String prefix) {
        this.prefix = prefix;
    }

    @SubscribeEvent
    public void onReady(ReadyEvent event) {
        Reflections reflections = new Reflections("com.oopsjpeg.gacha");
        Set<Class<? extends Command>> cls = reflections.getSubTypesOf(Command.class);

        clear();
        addAll(cls.stream().map(c -> {
            try {
                return c.getConstructor(CommandManager.class).newInstance(this);
            } catch (NoSuchMethodException | IllegalAccessException
                    | InstantiationException | InvocationTargetException error) {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList()));

        Gacha.LOGGER.info("Loaded " + size() + " command(s).");
    }

    @SubscribeEvent
    public void onMessageReceived(MessageReceivedEvent event) {
        MessageChannel channel = event.getChannel();
        User user = event.getAuthor();
        Message message = event.getMessage();
        String content = message.getContentRaw();
        String[] split = content.split(" ");

        if (split[0].toLowerCase().startsWith(prefix.toLowerCase())) {
            String alias = split[0].replaceFirst(prefix, "");
            String[] args = Arrays.copyOfRange(split, 1, split.length);
            Command command = getByAlias(alias);
            if (command != null) {
                try {
                    command.tryExecute(message, alias, args);
                } catch (CommandException error) {
                    Util.sendError(channel, user, error.getMessage());
                } catch (Exception error) {
                    Util.sendError(channel, user, "An unhandled error occurred.");
                    error.printStackTrace();
                }
            }
        }
    }

    public Command getByAlias(String alias) {
        return stream()
                .filter(c -> c.getName().equalsIgnoreCase(alias)
                        || Arrays.stream(c.getAliases()).anyMatch(a -> a.equalsIgnoreCase(alias)))
                .findAny().orElse(null);
    }
}
