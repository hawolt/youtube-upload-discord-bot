package com.hawolt.routines;

import com.hawolt.routines.routines.HelpRoutine;
import com.hawolt.routines.routines.UploadRoutine;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created: 25/04/2022 13:45
 * Author: Twitter @hawolt
 **/

public class MessageRoutine implements IRoutine<MessageReceivedEvent> {

    private final Map<String, IRoutine<MessageReceivedEvent>> map = new HashMap<String, IRoutine<MessageReceivedEvent>>() {{
        put("help", new HelpRoutine());
        put("upload", new UploadRoutine());
    }};

    @Override
    public void apply(MessageReceivedEvent event) {
        User author = event.getAuthor();
        if (author.isBot()) return;
        Message message = event.getMessage();
        if (event.getMember() == null || !event.getMember().hasPermission(Permission.ADMINISTRATOR)) return;
        String display = message.getContentDisplay();
        if (!display.startsWith("?")) return;
        String[] split = display.split(" ");
        String action = split[0].substring(1);
        if (!map.containsKey(action)) return;
        map.get(action).apply(event);
    }
}
