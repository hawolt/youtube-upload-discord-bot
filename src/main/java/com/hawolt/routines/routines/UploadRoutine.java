package com.hawolt.routines.routines;

import com.hawolt.*;
import com.hawolt.artwork.ArtCallback;
import com.hawolt.artwork.ArtworkLoader;
import com.hawolt.data.media.MediaManager;
import com.hawolt.data.media.Soundcloud;
import com.hawolt.data.media.Track;
import com.hawolt.data.media.download.DownloadCallback;
import com.hawolt.data.media.hydratable.Hydratable;
import com.hawolt.routines.IRoutine;
import com.hawolt.soundcloud.SoundcloudMediaManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Created: 02/05/2022 03:10
 * Author: Twitter @hawolt
 **/

public class UploadRoutine extends MediaManager implements ArtCallback, IRoutine<MessageReceivedEvent> {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    private static final Path BASE_PATH = Paths.get(System.getProperty("user.dir"));

    public static final Path TMP_DIR = BASE_PATH.resolve("tmp");

    static {
        Hydratable.EXECUTOR_SERVICE = Executors.newFixedThreadPool(1);
        try {
            Files.createDirectories(TMP_DIR);
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    private HashMap<String, PersistentSettings> map = new HashMap<>();

    private static String[] convert(String in) {
        String[] actual = new String[0];
        String[] basic = in.split(" ");
        int offset = 0;
        for (int i = 0; i < basic.length; i++) {
            String current = basic[i];
            if (!current.startsWith("\"")) {
                actual = Arrays.copyOf(actual, actual.length + 1);
                actual[offset++] = basic[i];
            } else {
                StringBuilder builder = new StringBuilder();
                builder.append(current.substring(1));
                do {
                    int index = ++i;
                    if (i >= basic.length) break;
                    current = basic[index];
                    if (!current.endsWith("\"")) {
                        builder.append(" ").append(current);
                    } else {
                        builder.append(" ").append(current, 0, current.length() - 1);
                    }
                } while (!current.endsWith("\""));
                actual = Arrays.copyOf(actual, actual.length + 1);
                actual[offset++] = builder.toString();
            }
        }
        return actual;
    }

    public static void edit(PersistentSettings settings, String message) {
        Guild guild = Corinthians.jda.getGuildById(settings.getGuild());
        if (guild == null) return;
        TextChannel channel = guild.getTextChannelById(settings.getChannel());
        if (channel == null) return;
        channel.retrieveMessageById(settings.getMessage()).queue(msg -> {
            msg.editMessage(message).queue();
        });
    }

    @Override
    public void apply(MessageReceivedEvent event) {
        String uuid = UUID.randomUUID().toString();
        PersistentSettings settings = new PersistentSettings(uuid);
        event.getTextChannel().sendMessage("initializing").queue(message -> settings.setMessage(message.getIdLong()));
        Parser parser = new Parser();
        parser.add(Argument.create("tr", "track", "url to track", true, true, false));
        parser.add(Argument.create("ti", "title", "video title", true, true, false));
        parser.add(Argument.create("i", "image", "url to image", true, true, false));
        parser.add(Argument.create("c", "category", "youtube category", true, true, false));
        parser.add(Argument.create("d", "description", "video description", true, true, false));
        String[] args = convert(Arrays.stream(event.getMessage().getContentDisplay().split(" ")).skip(1).collect(Collectors.joining(" ")));
        try {
            CLI cli = parser.check(args);
            try {
                Files.createDirectories(UploadRoutine.TMP_DIR.resolve(uuid));
            } catch (IOException e) {
                Logger.error(e);
            }
            edit(settings, "processing");
            settings.setChannel(event.getChannel().getIdLong());
            settings.setGuild(event.getGuild().getIdLong());
            settings.setTitle(cli.getValue("title"));
            settings.setDescription(cli.getValue("description"));
            settings.setCategoryId(cli.getValue("category"));
            String track = cli.getValue("track").trim();
            map.put(track, settings);
            settings.setURL(track);
            String image = cli.getValue("image").trim();
            map.put(image, settings);
            load(track);
            EXECUTOR_SERVICE.execute(new ArtworkLoader(image, this));
            event.getMessage().suppressEmbeds(true).queue();
        } catch (ParserException e) {
            String response = String.join("\n\n", e.getMessage(), parser.getHelp());
            edit(settings, String.format("```%s```", response));
        }
    }

    private void setup(Path path) {
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    private void create(PersistentSettings settings) {
        edit(settings, "crunching numbers");
        try {
            File file = FFMPEG.create(settings);
            Youtube.upload(settings, file);
            TMP_DIR.resolve(settings.getUUID()).toFile().delete();
        } catch (IOException | InterruptedException | GeneralSecurityException e) {
            Logger.error(e);
        }
    }

    @Override
    public void ping(Track track) {
        track.retrieveMP3().whenComplete((mp3, throwable) -> {
            if (throwable != null) Logger.error(throwable);
            if (mp3 == null) return;
            mp3.download(this);
        });
    }

    @Override
    public void onCompletion(Track track, byte[] bytes) {
        PersistentSettings settings = map.get(track.getLink());
        Path path = TMP_DIR.resolve(settings.getUUID());
        setup(path);
        String filename = String.join(".", track.getPermalink(), "mp3");
        Path audio = path.resolve(filename);
        try {
            Files.write(audio, bytes);
            settings.setDuration(track.getDuration());
            settings.setAudio(audio.toFile());
        } catch (IOException e) {
            Logger.error(e);
        }
        if (settings.isComplete()) {
            create(settings);
        }
    }

    @Override
    public void onFailure(Track track, int i) {
        edit(map.get(track.getLink()), "failed to load track from soundcloud");
    }

    @Override
    public void onLoadFailure(String s, IOException e) {
        edit(map.get(s), "failed to load soundcloud data");
    }

    @Override
    public void onLoad(String url, byte[] b) {
        PersistentSettings settings = map.get(url);
        Path path = TMP_DIR.resolve(settings.getUUID());
        setup(path);
        int last = url.lastIndexOf(".");
        String type = url.substring(last + 1);
        String filename = String.join(".", "image", type);
        Path image = path.resolve(filename);
        try {
            Files.write(image, b);
            settings.setImage(image.toFile());
        } catch (IOException e) {
            Logger.error(e);
        }
        if (settings.isComplete()) {
            create(settings);
        }
    }

    @Override
    public void onFailure(String url, IOException e) {
        edit(map.get(url), "failed to load artwork");
    }
}
