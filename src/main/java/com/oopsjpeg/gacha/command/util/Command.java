package com.oopsjpeg.gacha.command.util;

import com.oopsjpeg.gacha.Gacha;
import lombok.Getter;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

import java.util.EnumSet;

/**
 * Command interface.
 * Created by oopsjpeg on 1/30/2019.
 */
@Getter
public abstract class Command {
    private final CommandManager manager;
    private final String name;
    protected String[] aliases = new String[0];
    protected String usage = "";
    protected String description = "";
    protected EnumSet<Permission> permissions = EnumSet.noneOf(Permission.class);
    protected boolean guildOnly = false;
    protected boolean ownerOnly = false;
    protected boolean developerOnly = false;
    protected boolean registeredOnly = false;
    protected boolean adminOnly = false;

    public Command(CommandManager manager, String name) {
        this.manager = manager;
        this.name = name;
    }

    public abstract void execute(Message message, String alias, String[] args) throws Exception;

    public void tryExecute(Message message, String alias, String[] args) throws Exception {
        User user = message.getAuthor();
        Guild guild = message.getGuild();

        // Developer only
        if (developerOnly && !user.equals(message.getJDA().asBot().getApplicationInfo().complete().getOwner()))
            throw new NotDeveloperException(this);
        // Not in guild
        if ((guildOnly || ownerOnly || adminOnly || !permissions.isEmpty()) && guild == null)
            throw new NotInGuildException(this);
        // Not owner
        if (ownerOnly && user.getIdLong() != guild.getOwnerIdLong())
            throw new NotOwnerException(this);
        // Not admin
        if (adminOnly && !guild.getMember(user).getPermissions().contains(Permission.ADMINISTRATOR))
            throw new NotAdminException(this);
        // Invalid perms
        if (!permissions.isEmpty() && !guild.getMember(user).getPermissions(message.getTextChannel()).containsAll(permissions))
            throw new InvalidPermsException(this);
        // Registered only
        if (registeredOnly && !getParent().getUsers().containsKey(user.getIdLong()))
            throw new NotRegisteredException(this);
        // TODO: Invalid usage
        
        execute(message, alias, args);
    }

    public Gacha getParent() {
        return Gacha.getInstance();
    }
}
