package com.hawolt.artwork;

import java.io.IOException;

/**
 * Created: 25/06/2022 21:56
 * Author: Twitter @hawolt
 **/

public interface ArtCallback {
    void onLoad(String url, byte[] b);

    void onFailure(String url, IOException e);
}
