package com.hawolt;

import com.hawolt.routines.routines.UploadRoutine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created: 25/06/2022 21:51
 * Author: Twitter @hawolt
 **/

public class PersistentSettings {
    private final String uuid;

    private String track, title, categoryId, description;
    private File audio, image;
    private long message, guild, channel, duration;

    public PersistentSettings(String uuid) {
        this.uuid = uuid;
    }

    public void setChannel(long channel) {
        this.channel = channel;
    }

    public void setGuild(long guild) {
        this.guild = guild;
    }

    public void setAudio(File audio) {
        this.audio = audio;
    }

    public void setImage(File image) {
        this.image = image;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getUuid() {
        return uuid;
    }

    public String getTrack() {
        return track;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public boolean isComplete() {
        return audio != null && image != null;
    }

    public String getUUID() {
        return uuid;
    }

    public long getDuration() {
        return duration;
    }

    public File getAudio() {
        return audio;
    }

    public File getImage() {
        return image;
    }

    public long getGuild() {
        return guild;
    }

    public long getChannel() {
        return channel;
    }

    public void setURL(String track) {
        this.track = track;
    }

    public String getURL() {
        return track;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(long message) {
        this.message = message;
    }

    public long getMessage() {
        return message;
    }
}
