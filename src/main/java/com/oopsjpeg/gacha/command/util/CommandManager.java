package com.oopsjpeg.gacha.command.util;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.manager.Manager;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by oopsjpeg on 2/28/2019.
 */
public class CommandManager extends Manager {
    private String prefix;
    private List<Command> commands = new ArrayList<>();

    public CommandManager(Gacha parent, String prefix) {
        super(parent);
        this.prefix = prefix;
    }

    @Override
    public void onReady(ReadyEvent event) {
        Reflections reflections = new Reflections("com.oopsjpeg.gacha");
        Set<Class<? extends Command>> cls = reflections.getSubTypesOf(Command.class);

        commands.clear();
        commands.addAll(cls.stream().map(c -> {
            try {
                return c.getConstructor(CommandManager.class).newInstance(this);
            } catch (NoSuchMethodException | IllegalAccessException
                    | InstantiationException | InvocationTargetException error) {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList()));

        Gacha.LOGGER.info("Loaded " + commands.size() + " command(s).");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        MessageChannel channel = event.getChannel();
        User user = event.getAuthor();
        Message message = event.getMessage();
        String content = message.getContentRaw();
        String[] split = content.split(" ");

        if (split[0].toLowerCase().startsWith(prefix.toLowerCase())) {
            String alias = split[0].replaceFirst(prefix, "");
            String[] args = Arrays.copyOfRange(split, 1, split.length);
            Command command = get(alias);
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

    public Command get(String alias) {
        return commands.stream()
                .filter(c -> c.getName().equalsIgnoreCase(alias)
                        || Arrays.stream(c.getAliases()).anyMatch(a -> a.equalsIgnoreCase(alias)))
                .findAny().orElse(null);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public List<Command> getCommands() {
        return commands;
    }

    public void setCommands(List<Command> commands) {
        this.commands = commands;
    }
}
