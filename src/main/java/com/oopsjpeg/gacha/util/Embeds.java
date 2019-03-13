package com.oopsjpeg.gacha.util;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.CardEmbed;
import com.oopsjpeg.gacha.object.user.UserInfo;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;

public class Embeds {
    public static MessageEmbed card(User user, Card card) {
        EmbedBuilder builder = new EmbedBuilder();
        UserInfo info = Gacha.getInstance().getUser(user.getIdLong());
        CardEmbed cardEmbed = Gacha.getInstance().getCardEmbed(card.getId());
        long amount = info.getCards().stream().filter(c -> c.equals(card)).count();

        builder.setColor(cardEmbed.getEmbedColor());
        builder.setAuthor(card.getName() + " (" + Util.star(card.getStar()) + ") [" + card.getId() + "]", card.getSource(), user.getAvatarUrl());
        builder.setImage("attachment://" + card.getId() + ".png");
        builder.setFooter("You have x" + amount + " of this card.", null);

        return builder.build();
    }
}
