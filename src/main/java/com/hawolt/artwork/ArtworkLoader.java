package com.hawolt.artwork;

import com.hawolt.Request;
import com.hawolt.Response;

import java.io.IOException;

/**
 * Created: 25/06/2022 21:54
 * Author: Twitter @hawolt
 **/

public class ArtworkLoader implements Runnable {
    private final ArtCallback callback;
    private final String url;

    public ArtworkLoader(String url, ArtCallback callback) {
        this.callback = callback;
        this.url = url;
    }

    @Override
    public void run() {
        try {
            Request request = new Request(url);
            request.addHeader("User-Agent", "ALIZE-UPLOADS");
            Response response = request.execute();
            callback.onLoad(url, response.getBody());
        } catch (IOException e) {
            callback.onFailure(url, e);
        }
    }
}
