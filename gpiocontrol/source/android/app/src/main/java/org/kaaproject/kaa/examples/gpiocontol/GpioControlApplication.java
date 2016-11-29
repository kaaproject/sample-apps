package org.kaaproject.kaa.examples.gpiocontol;

import android.app.Application;

import org.kaaproject.kaa.examples.gpiocontol.utils.KaaManager;

public class GpioControlApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        KaaManager.getInstance().init(this);
    }
}
