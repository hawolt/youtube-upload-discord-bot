package com.hawolt;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import com.hawolt.Logger;
import com.hawolt.routines.routines.UploadRoutine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Created: 25/06/2022 22:24
 * Author: Twitter @hawolt
 **/

public class Youtube {

    private static final String CLIENT_SECRETS = "client_secret.json";
    private static final String APPLICATION_NAME = "API code samples";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final Collection<String> SCOPES = Collections.singletonList("https://www.googleapis.com/auth/youtube.upload");

    private static final File DATA_STORE_DIR = Paths.get(System.getProperty("user.dir")).toFile();
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    static {
        try {
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            Logger.error(t);
        }
    }

    public static Credential authorize(final NetHttpTransport httpTransport) throws IOException {
        InputStream in = new FileInputStream(Paths.get(CLIENT_SECRETS).toFile());
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline").build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        Logger.debug("Storing credentials to {}", DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    public static YouTube getService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = authorize(httpTransport);
        return new YouTube.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
    }

    public static void upload(PersistentSettings settings, File mediaFile) throws GeneralSecurityException, IOException {
        UploadRoutine.edit(settings, "uploading");
        YouTube youtubeService = getService();
        Video video = new Video();
        VideoSnippet snippet = new VideoSnippet();
        snippet.setCategoryId(settings.getCategoryId());
        snippet.setDescription(settings.getDescription());
        snippet.setTitle(settings.getTitle());
        video.setSnippet(snippet);

        VideoStatus status = new VideoStatus();
        status.setPrivacyStatus("public");
        video.setStatus(status);

        InputStreamContent mediaContent = new InputStreamContent("application/octet-stream", new BufferedInputStream(new FileInputStream(mediaFile)));
        mediaContent.setLength(mediaFile.length());

        YouTube.Videos.Insert request = youtubeService.videos().insert(Arrays.asList("snippet", "status"), video, mediaContent);
        Video response = request.execute();

        JSONObject result = new JSONObject(response);
        String id = result.getString("id");

        UploadRoutine.edit(settings, "done, your video will be available shortly at https://www.youtube.com/watch?v=" + id);
    }
}
