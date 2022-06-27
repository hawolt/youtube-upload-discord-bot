package com.hawolt.routines.routines;

import com.hawolt.routines.IRoutine;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.Instant;

/**
 * Created: 02/05/2022 03:10
 * Author: Twitter @hawolt
 **/

public class HelpRoutine implements IRoutine<MessageReceivedEvent> {
    @Override
    public void apply(MessageReceivedEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor("I can see you need some assistance");
        builder.setTitle("Here is how I work");
        builder.setTimestamp(Instant.now());
        builder.appendDescription("I have the following command:\n**upload**\n\n");
        builder.appendDescription("To upload a video use `?upload --track track.url --image image.url`\n");
        event.getChannel().sendMessageEmbeds(builder.build()).queue();
    }
}
