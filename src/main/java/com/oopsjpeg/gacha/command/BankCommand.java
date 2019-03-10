package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.command.util.Command;
import com.oopsjpeg.gacha.command.util.CommandManager;
import com.oopsjpeg.gacha.object.user.UserBank;
import com.oopsjpeg.gacha.object.user.UserInfo;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.time.LocalDateTime;

/**
 * Created by oopsjpeg on 3/10/2019.
 */
public class BankCommand extends Command {
    public BankCommand(CommandManager manager) {
        super(manager, "bank");
        description = "View your bank balance and deposit/withdraw funds.";
        registeredOnly = true;
    }

    @Override
    public void execute(Message message, String alias, String[] args) {
        MessageChannel channel = message.getChannel();
        User author = message.getAuthor();
        UserInfo info = getParent().getUser(author.getIdLong());
        UserBank bank = info.getBank();

        if (args.length >= 1 && args[0].equalsIgnoreCase("deposit")) {
            if (!bank.hasTransaction())
                Util.sendError(channel, author, "You must wait " + Util.timeDiff(LocalDateTime.now(), bank.getTransactionDate().plusDays(3)) + " before making another transaction.");
            else if (args.length < 2)
                Util.sendError(channel, author, "You must enter an amount to deposit.");
            else if (!Util.isDigits(args[1]))
                Util.sendError(channel, author, "Invalid deposit amount.");
            else {
                int amount = Integer.parseInt(args[1]);
                if (info.getCrystals() < amount)
                    Util.sendError(channel, author, "You do not have enough crystals to make this deposit.");
                else {
                    bank.setTransactionDate(LocalDateTime.now());
                    bank.addCrystals(amount);
                    info.removeCrystals(amount);
                    Util.sendSuccess(channel, author, Util.comma(amount) + " has been deposited into your bank.");
                    getParent().getMongo().saveUser(info);
                }
            }
        } else if (args.length >= 1 && args[0].equalsIgnoreCase("withdraw")) {
            if (!bank.hasTransaction())
                Util.sendError(channel, author, "You must wait " + Util.timeDiff(LocalDateTime.now(), bank.getTransactionDate().plusDays(3)) + " before making another transaction.");
            else if (args.length < 2)
                Util.sendError(channel, author, "You must enter an amount to withdraw.");
            else if (!Util.isDigits(args[1]))
                Util.sendError(channel, author, "Invalid withdrawal amount.");
            else {
                int amount = Integer.parseInt(args[1]);
                if (bank.getCrystals() < amount)
                    Util.sendError(channel, author, "You do not have enough crystals to make this withdrawal.");
                else {
                    bank.setTransactionDate(LocalDateTime.now());
                    bank.removeCrystals(amount);
                    info.addCrystals(amount);
                    Util.sendSuccess(channel, author, Util.comma(amount) + " has been withdrawn from your bank.");
                    getParent().getMongo().saveUser(info);
                }
            }
        } else {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setAuthor(author.getName() + "'s Bank", null, author.getAvatarUrl());
            builder.setColor(Util.getColor(author, channel.getIdLong()));

            builder.addField("Crystals", Util.comma(bank.getCrystals()), true);

            if (bank.hasTransaction())
                builder.addField("Transaction", "Available", true);
            else
                builder.addField("Transaction", Util.timeDiff(LocalDateTime.now(), bank.getTransactionDate().plusDays(2)), true);

            Util.sendEmbed(channel, "Viewing " + Util.nameThenId(author) + "'s bank.", builder.build());
        }
    }
}
