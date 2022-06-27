package com.hawolt;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.hawolt.routines.MessageRoutine;
import com.hawolt.routines.RoutineHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created: 23/04/2022 00:09
 * Author: Twitter @hawolt
 **/

public class Corinthians {

    public static JDA jda;

    public static void main(String[] args) throws LoginException {
        Setup setup = new Setup(args);
        JsonSource source = setup.getSource();
        RoutineHandler handler = new RoutineHandler();
        FFMPEG.FFMPEG_PATH = source.get("ffmpeg.path");
        FFMPEG.FILTER_PATH = source.get("filter_complex.path");
        handler.addRoutine(MessageReceivedEvent.class, new MessageRoutine());
        Corinthians.jda = JDABuilder.createLight(source.get("discord.token"), GatewayIntent.GUILD_MESSAGES)
                .addEventListeners(handler)
                .setActivity(Activity.listening(source.get("discord.activity")))
                .build();
    }
}
