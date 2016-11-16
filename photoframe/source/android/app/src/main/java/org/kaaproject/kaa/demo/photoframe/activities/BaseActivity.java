package org.kaaproject.kaa.demo.photoframe.activities;

import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.kaaproject.kaa.demo.photoframe.PhotoFrameApplication;
import org.kaaproject.kaa.demo.photoframe.R;
import org.kaaproject.kaa.demo.photoframe.communication.Events;
import org.kaaproject.kaa.demo.photoframe.kaa.KaaManager;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onEvent(final Events.PlayAlbumEvent playAlbumEvent) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (getKaaManager().isUserAttached()) {
                    loadSlideshow(playAlbumEvent);
                } else {
                    // If you logout, but get this event
                    Toast.makeText(BaseActivity.this, R.string.fragment_base_interaction_event_text, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Subscribe
    public void onEvent(Events.UserDetachEvent userDetachEvent) {
        final String errorMessage = userDetachEvent.getErrorMessage();
        if (errorMessage != null) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    protected KaaManager getKaaManager() {
        return PhotoFrameApplication.kaaManager(this);
    }

    protected void loadSlideshow(Events.PlayAlbumEvent playAlbumEvent) {
        SlideshowActivity.start(this, playAlbumEvent.getBucketId());
    }
}
