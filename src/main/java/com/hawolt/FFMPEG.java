package com.hawolt;

import com.hawolt.routines.routines.UploadRoutine;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created: 25/06/2022 22:07
 * Author: Twitter @hawolt
 **/

public class FFMPEG {
    public static String FFMPEG_PATH, FILTER_PATH;

    private static String formatDuration(long duration) {
        long ms = duration % 1000;
        long flat = duration - ms;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(flat);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(flat - TimeUnit.MINUTES.toMillis(minutes));
        return String.format("00:%02d:%02d.%03d", minutes, seconds, ms);
    }

    public static File create(PersistentSettings settings) throws IOException, InterruptedException {
        String filter = new String(Files.readAllBytes(Paths.get(FILTER_PATH)));
        Path base = UploadRoutine.TMP_DIR.resolve(settings.getUUID());
        String out = base.resolve("out.mp4").toFile().getAbsolutePath();
        boolean windows = System.getProperty("os.name").startsWith("Windows");
        ProcessBuilder builder = new ProcessBuilder(windows ? FFMPEG_PATH : "ffmpeg",
                "-y",
                "-loop", "1", "-framerate", "1", "-i", settings.getImage().getAbsolutePath(), "-i", settings.getAudio().getAbsolutePath(),
                "-filter_complex", filter,
                "-c:v", "libx264", "-c:a", "aac", "-b:a", "320k", "-strict", "-2", "-t", formatDuration(settings.getDuration()), out
        );
        builder.redirectErrorStream(true);
        Process process = builder.start();
        try (FileWriter writer = new FileWriter(base.resolve("log").toFile())) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println(line);
                    writer.write(line + System.lineSeparator());
                }
            }
        }
        process.waitFor();
        return new File(out);
    }

}
