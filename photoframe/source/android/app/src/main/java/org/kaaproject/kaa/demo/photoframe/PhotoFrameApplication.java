package org.kaaproject.kaa.demo.photoframe;

import android.app.Application;
import android.content.Context;

import org.kaaproject.kaa.demo.photoframe.kaa.KaaManager;

public class PhotoFrameApplication extends Application {

    private final KaaManager mKaaManager = new KaaManager();

    public static KaaManager kaaManager(Context context) {
        return ((PhotoFrameApplication) context.getApplicationContext()).mKaaManager;
    }
}
