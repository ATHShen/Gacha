package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.user.Bank;
import com.oopsjpeg.gacha.object.user.UserInfo;
import com.oopsjpeg.gacha.util.AmountParser;
import com.oopsjpeg.gacha.util.CardQuery;
import com.oopsjpeg.gacha.util.Constants;
import com.oopsjpeg.gacha.util.PullType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Command interface.
 * Created by oopsjpeg on 1/30/2019.
 */
@Getter
@RequiredArgsConstructor
public enum Command {
    HELP("help", null, "View helpful information about Gacha.") {
        @Override
        public void execute(Message message, String alias, String[] args) {
            MessageChannel channel = message.getChannel();
            User author = message.getAuthor();
            User self = message.getJDA().getSelfUser();

            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Util.getColor(self, channel.getIdLong()));
            builder.setAuthor("Gacha Commands", null, self.getAvatarUrl());
            builder.setDescription(Arrays.stream(Command.values())
                    .sorted(Comparator.comparing(Command::getName))
                    .map(c -> "`" + getInstance().getPrefix() + c.getName() + "`: " + c.getDescription())
                    .collect(Collectors.joining("\n")));
            Util.sendEmbed(channel, "Viewing all **Gacha** commands.", builder.build());
        }
    },

    PROFILE("profile", null, "View your profile.") {
        @Override
        public void execute(Message message, String alias, String[] args) {
            MessageChannel channel = message.getChannel();
            User author = message.getAuthor();
            UserInfo info = getInstance().getUser(author.getIdLong());
            Bank bank = info.getBank();

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
            String fromBank = bank.hasCrystals() ? " (" + Util.comma(info.getCrystals()) + " in hand)" : "";
            builder.addField("Crystals", crystals + fromBank, true);
            // Timelies
            List<String> timelies = new ArrayList<>();
            timelies.add(info.hasDaily() ? "**Daily** is available." : "**Daily** is available in " + Util.timeDiff(LocalDateTime.now(), info.getDailyDate().plusDays(1)) + ".");
            timelies.add(info.hasWeekly() ? "**Weekly** is available." : "**Weekly** is available in " + Util.timeDiff(LocalDateTime.now(), info.getWeeklyDate().plusWeeks(1)) + ".");
            builder.addField("Timelies", String.join("\n", timelies), false);

            Util.sendEmbed(channel, "Viewing " + Util.formatUsername(author) + "'s profile.", builder.build());
        }
    },
    REGISTER("register", null, "Register a new Gacha account.") {
        @Override
        public void execute(Message message, String alias, String[] args) {
            MessageChannel channel = message.getChannel();
            User author = message.getAuthor();

            if (getInstance().getUsers().containsKey(author.getIdLong()))
                Util.sendError(channel, author, "You are already registered with Gacha.");
            else {
                Util.send(channel, "You are now registered with Gacha!",
                        "Check your profile with `/profile`."
                                + "\nPull a random card for " + PullType.STANDARD.getCost() + " crystals with `/pull`."
                                + "\nLearn how to forge new cards with `/forge`.",
                        Util.getColor(author, channel.getIdLong()));
                getInstance().getMongo().saveUser(getInstance().registerUser(author.getIdLong()));
            }
        }
    },
    DESCRIPTION("description", "[\"clear\"/new description]", "Update your profile description.") {
        @Override
        public void execute(Message message, String alias, String[] args) {
            MessageChannel channel = message.getChannel();
            User author = message.getAuthor();
            UserInfo info = getInstance().getUser(author.getIdLong());

            if (args.length == 0)
                Util.sendError(channel, author, "You must enter a new profile description.");
            else if (args[0].equalsIgnoreCase("clear")) {
                info.setDescription(null);
                Util.sendSuccess(channel, author, "Your profile description has been cleared.");
                getInstance().getMongo().saveUser(info);
            } else {
                String description = String.join(" ", args);
                if (description.length() > Constants.DESCRIPTION_MAX)
                    Util.sendError(channel, author, "Your profile description must be at most " + Constants.DESCRIPTION_MAX + " characters.");
                else {
                    info.setDescription(description);
                    Util.sendSuccess(channel, author, "Your profile description has been updated.");
                    getInstance().getMongo().saveUser(info);
                }
            }
        }
    },
    BANK("bank", null, "Check your bank balance.") {
        @Override
        public void execute(Message message, String alias, String[] args) {
            MessageChannel channel = message.getChannel();
            User author = message.getAuthor();
            UserInfo info = getInstance().getUser(author.getIdLong());
            Bank bank = info.getBank();

            if (args.length >= 1 && args[0].equalsIgnoreCase("deposit"))
                DEPOSIT.execute(message, "deposit", Arrays.copyOfRange(args, 1, args.length));
            else if (args.length >= 1 && args[0].equalsIgnoreCase("withdraw"))
                WITHDRAW.execute(message, "withdraw", Arrays.copyOfRange(args, 1, args.length));
            else {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setAuthor(author.getName() + "'s Bank", null, author.getAvatarUrl());
                builder.setColor(Util.getColor(author, channel.getIdLong()));
                builder.setDescription("You may withdraw from the bank every " + Constants.BANK_COOLDOWN + " days."
                        + "\nCrystals stored in the bank earn interest at a rate of **" + Util.percent(Constants.BANK_RATE) + "** with daily compounding.");
                builder.addField("Crystals", Util.comma(bank.getCrystals()), true);

                if (bank.hasWithdrawal())
                    builder.addField("Next Withdrawal", "You may make a withdrawal.", true);
                else
                    builder.addField("Next Withdrawal", bank.nextWithdrawal(), true);

                Util.sendEmbed(channel, "Viewing " + Util.formatUsername(author) + "'s bank.", builder.build());
            }
        }
    },

    DEPOSIT("deposit", "<amount>", "Deposit crystals into your bank.") {
        @Override
        public void execute(Message message, String alias, String[] args) {
            MessageChannel channel = message.getChannel();
            User author = message.getAuthor();
            UserInfo info = getInstance().getUser(author.getIdLong());
            Bank bank = info.getBank();

            if (args.length < 1)
                Util.sendError(channel, author, "You must enter an amount to deposit.");
            else {
                try {
                    int amount = AmountParser.getInt(args[0], info.getCrystals());

                    if (info.getCrystals() < amount)
                        Util.sendError(channel, author, "You do not have enough crystals to make this deposit.");

                    bank.addCrystals(amount);
                    info.removeCrystals(amount);
                    getInstance().getMongo().saveUser(info);

                    Util.sendSuccess(channel, author, "**" + Util.comma(amount) + "** crystal(s) have been deposited into your bank.");
                } catch (NumberFormatException error) {
                    Util.sendError(channel, author, "Invalid deposit amount.");
                }
            }
        }
    },
    WITHDRAW("withdraw", "<amount>", "Withdraw crystals from your bank.") {
        @Override
        public void execute(Message message, String alias, String[] args) {
            MessageChannel channel = message.getChannel();
            User author = message.getAuthor();
            UserInfo info = getInstance().getUser(author.getIdLong());
            Bank bank = info.getBank();

            if (!bank.hasWithdrawal())
                Util.sendError(channel, author, "You must wait " + bank.nextWithdrawal() + " before making another withdrawal.");
            else if (args.length < 1)
                Util.sendError(channel, author, "You must enter an amount to withdraw.");
            else {
                try {
                    int amount = AmountParser.getInt(args[0], bank.getCrystals());

                    if (bank.getCrystals() < amount)
                        Util.sendError(channel, author, "You do not have enough crystals to make this withdrawal.");

                    info.addCrystals(amount);
                    bank.removeCrystals(amount);
                    bank.setWithdrawalDate(LocalDateTime.now());
                    getInstance().getMongo().saveUser(info);

                    Util.sendSuccess(channel, author, "**" + Util.comma(amount) + "** crystal(s) have been withdrawn from your bank.");
                } catch (NumberFormatException error) {
                    Util.sendError(channel, author, "Invalid withdrawal amount.");
                }
            }
        }
    },

    CARD("card", "[id]", "Show one of your cards.") {
        @Override
        public void execute(Message message, String alias, String[] args) {
            MessageChannel channel = message.getChannel();
            User author = message.getAuthor();
            UserInfo info = getInstance().getUser(author.getIdLong());

            if (info.getCards().isEmpty())
                Util.sendError(channel, author, "You do not have any cards.");
            else {
                Card card = null;

                if (args.length <= 0)
                    card = info.getCards().get(Util.RANDOM.nextInt(info.getCards().size()));
                else {
                    String search = String.join(" ", args);
                    CardQuery query = new CardQuery(info.getCards()).search(search);
                    if (!query.isEmpty()) card = query.get().get(Util.RANDOM.nextInt(query.size()));
                }

                if (card == null)
                    Util.sendError(channel, author, "You either do not have that card, or it does not exist.");
                else {
                    try {
                        Util.sendCard(channel, author, card, Util.formatUsername(author) + " is viewing **" + card.getName() + "**.");
                    } catch (IOException error) {
                        throw new CommandException(error, this);
                    }
                }
            }
        }
    },
    CARDS("cards", "[page/\"all\"]", "View your cards.") {
        @Override
        public void execute(Message message, String alias, String[] args) {
            MessageChannel channel = message.getChannel();
            User author = message.getAuthor();
            UserInfo info = getInstance().getUser(author.getIdLong());

            if (info.getCards().isEmpty())
                Util.sendError(channel, author, "You do not have any cards.");
            else {
                if (args.length >= 1 && args[0].equalsIgnoreCase("all")) {
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        baos.write((author.getName() + "'s Cards (" + Util.comma(info.getCards().size()) + ")\n"
                                + new CardQuery(info.getCards()).raw())
                                .getBytes(StandardCharsets.UTF_8));

                        channel.sendFile(new ByteArrayInputStream(baos.toByteArray()),
                                "Cards_" + Util.fileName(LocalDateTime.now().toString()) + ".txt",
                                new MessageBuilder("Viewing all of " + Util.formatUsername(author) + "'s cards.").build()).complete();
                    } catch (IOException error) {
                        throw new CommandException(error, this);
                    }
                }

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

                    Util.sendEmbed(channel, "Viewing " + Util.formatUsername(author) + "'s cards.", b.build());
                }
            }
        }
    },
    PULL("pull", null, "Pull a random card.") {
        @Override
        public void execute(Message message, String alias, String[] args) {
            MessageChannel channel = message.getChannel();
            User author = message.getAuthor();
            UserInfo info = getInstance().getUser(author.getIdLong());
            PullType type = PullType.STANDARD; // TODO Add selection when more types are available

            if (info.getCrystals() < type.getCost())
                Util.sendError(channel, author, "You need **" + Util.comma(type.getCost()) + "** to pull from **" + type.getName() + "**.");
            else {
                Card card = type.get();
                info.removeCrystals(type.getCost());
                info.addCard(card);

                try {
                    Util.sendCard(channel, author, card, Util.formatUsername(author) + " pulled **" + card.getName() + "** from **" + type.getName() + "**.");
                } catch (IOException error) {
                    throw new CommandException(error, this);
                }

                getInstance().getMongo().saveUser(info);
            }
        }
    },
    FORGE("forge", "[ids seperated by spaces]", "Combine 3 cards of equal tier for a new card.") {
        @Override
        public void execute(Message message, String alias, String[] args) {
            User author = message.getAuthor();
            MessageChannel channel = message.getChannel();

            if (args.length == 0)
                Util.send(channel, "Forging",
                        "Combine 3 cards of equal tier to forge a new card of equal or above tier."
                                + "\nIdentical cards increase the chance of getting the above tier."
                                + "\nUse `/forge <ids separated by spaces>` to combine the cards."
                                + "\n\nFor example, to forge IDs 31, 12, and 17, use `/forge 31 12 17`.",
                        Util.getColor(author, channel.getIdLong()));
            else {
                UserInfo info = getInstance().getUser(author.getIdLong());
                int[] ids = Arrays.stream(args).mapToInt(s -> {
                    try {
                        return Integer.parseInt(s);
                    } catch (NumberFormatException error) {
                        return -1;
                    }
                }).toArray();

                List<Card> available = new ArrayList<>(info.getCards());
                List<Card> combine = new ArrayList<>();

                // Store already specified ids
                for (int i = 0; i < Math.min(3, ids.length); i++) {
                    Card c = getInstance().getCard(ids[i]);
                    if (!available.contains(c)) {
                        Util.sendError(channel, author, "One or more of the specified card IDs is invalid.");
                        return;
                    }
                    combine.add(c);
                    available.remove(c);
                }

                // Attempt to reuse already specified ids if needed
                if (combine.size() < 3) for (int i = 0; i <= 3 - combine.size(); i++) {
                    for (int id : ids) {
                        Card c = getInstance().getCard(id);
                        if (available.contains(c)) {
                            combine.add(c);
                            available.remove(c);
                            break;
                        }
                    }
                }

                // Minimum of 3 cards required
                if (combine.size() < 3) {
                    Util.sendError(channel, author, "You require at least 3 cards to forge.");
                    return;
                }

                int star = combine.get(0).getStar();
                // Legends cannot be forged
                if (star == 6) {
                    Util.sendError(channel, author, "You cannot forge Legend cards.");
                    return;
                }
                // Equal tier required
                if (combine.stream().anyMatch(c -> c.getStar() != star)) {
                    Util.sendError(channel, author, "You can only forge cards of equal tier.");
                    return;
                }

                // Increase above tier chance from identical cards
                float chance = 0.34f;
                for (Card c : new HashSet<>(combine))
                    chance += (Collections.frequency(combine, c) - 1) * 0.33f;

                // Take the cards and gacha a new card
                info.setCards(available);

                boolean above = Util.RANDOM.nextFloat() <= chance;
                List<Card> pool = getInstance().getCardsByStar(above ? star + 1 : star);
                pool.removeIf(Card::isExclusive);
                Card card = pool.get(Util.RANDOM.nextInt(pool.size()));
                info.addCard(card);

                try {
                    Util.sendCard(channel, author, card, Util.formatUsername(author) + " got **"
                            + card.getName() + "** from **Standard Forge**.");
                } catch (IOException error) {
                    throw new CommandException(error, this);
                }

                getInstance().getMongo().saveUser(info);
            }
        }
    },

    DAILY("daily", null, "Collect your daily bonus.") {
        @Override
        public void execute(Message message, String alias, String[] args) {
            MessageChannel channel = message.getChannel();
            User author = message.getAuthor();
            UserInfo info = getInstance().getUser(author.getIdLong());

            if (!info.hasDaily())
                Util.sendError(channel, author, "Your **Daily** is available in " + Util.timeDiff(LocalDateTime.now(), info.getDailyDate().plusDays(1)) + ".");

            int amount = Constants.TIMELY_DAY;
            info.addCrystals(amount);
            info.setDailyDate(LocalDateTime.now());
            getInstance().getMongo().saveUser(info);
            Util.sendSuccess(channel, author, "Collected **" + Util.comma(amount) + "** from **Daily**.");
        }
    },
    WEEKLY("weekly", null, "Collect your weekly bonus.") {
        @Override
        public void execute(Message message, String alias, String[] args) {
            MessageChannel channel = message.getChannel();
            User author = message.getAuthor();
            UserInfo info = getInstance().getUser(author.getIdLong());

            if (!info.hasWeekly())
                Util.sendError(channel, author, "Your **Weekly** is available in "
                        + Util.timeDiff(LocalDateTime.now(), info.getWeeklyDate().plusWeeks(1)) + ".");
            else {
                int amount = Constants.TIMELY_WEEK;
                info.addCrystals(amount);
                info.setWeeklyDate(LocalDateTime.now());
                Util.sendSuccess(channel, author, "Collected **" + Util.comma(amount) + "** from **Weekly**.");
                getInstance().getMongo().saveUser(info);
            }
        }
    },

    TEST_CARD("testcard", "<id>", "Display a card by ID.") {
        @Override
        public void execute(Message message, String alias, String[] args) {
            MessageChannel channel = message.getChannel();
            Card card = getInstance().getCard(Integer.parseInt(args[0]));
            try {
                Util.sendCard(channel, message.getAuthor(), card, "");
            } catch (IOException error) {
                throw new CommandException(error, this);
            }
        }
    };

    static {
        HELP.aliases = new String[]{"?", "about"};

        PROFILE.aliases = new String[]{"account"};
        PROFILE.registeredOnly = true;
        DESCRIPTION.aliases = new String[]{"desc", "bio"};
        DESCRIPTION.registeredOnly = true;
        BANK.aliases = new String[]{"balance"};
        BANK.registeredOnly = true;

        DEPOSIT.registeredOnly = true;
        WITHDRAW.registeredOnly = true;

        CARD.aliases = new String[]{"show", "summon", "sum"};
        CARD.registeredOnly = true;
        CARDS.registeredOnly = true;
        PULL.aliases = new String[]{"gacha"};
        PULL.registeredOnly = true;
        FORGE.aliases = new String[]{"craft", "combine"};
        FORGE.registeredOnly = true;

        DAILY.registeredOnly = true;
        WEEKLY.registeredOnly = true;

        TEST_CARD.registeredOnly = true;
        TEST_CARD.developerOnly = true;
    }

    private final String name;
    private final String usage;
    private final String description;
    private String[] aliases = new String[0];
    private EnumSet<Permission> permissions = EnumSet.noneOf(Permission.class);
    private boolean guildOnly;
    private boolean ownerOnly;
    private boolean developerOnly;
    private boolean registeredOnly;
    private boolean adminOnly;

    public static Command getFromAlias(String alias) {
        for (Command command : values())
            if (command.name.equalsIgnoreCase(alias) || Arrays.stream(command.aliases).anyMatch(a -> a.equalsIgnoreCase(alias)))
                return command;
        return null;
    }

    public abstract void execute(Message message, String alias, String[] args) throws CommandException;

    public void execute0(Message message, String alias, String[] args) throws CommandException {
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
        if (registeredOnly && !getInstance().getUsers().containsKey(user.getIdLong()))
            throw new NotRegisteredException(this);
        // TODO: Invalid usage

        execute(message, alias, args);
    }

    protected Gacha getInstance() {
        return Gacha.getInstance();
    }
}
