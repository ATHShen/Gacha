package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.command.util.Command;
import com.oopsjpeg.gacha.command.util.CommandManager;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.user.UserInfo;
import com.oopsjpeg.gacha.util.PullType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.io.IOException;

public class PullCommand extends Command {
    public PullCommand(CommandManager manager) {
        super(manager, "pull");
        aliases = new String[]{"gacha"};
        description = "Pull a random card.";
        registeredOnly = true;
    }

    @Override
    public void execute(Message message, String alias, String[] args) throws IOException {
        MessageChannel channel = message.getChannel();
        User author = message.getAuthor();
        UserInfo info = getParent().getUser(author.getIdLong());
        PullType type = PullType.STANDARD; // TODO Add selection when more types are available

        if (info.getCrystals() < type.getCost())
            Util.sendError(channel, author, "You need **" + Util.comma(type.getCost()) + "** to pull from **" + type.getName() + "**.");
        else {
            Card card = type.get();
            info.removeCrystals(type.getCost());
            info.addCard(card);

            Util.sendCard(channel, author, card, Util.nameThenId(author) + " pulled **" + card.getName() + "** from **" + type.getName() + "**.");

            getParent().getMongo().saveUser(info);
        }
    }
}
