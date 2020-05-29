package lmu.hradio.hradioshowcase.spotify.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SpotifyBroadcastReceiver extends BroadcastReceiver {
    static final class BroadcastTypes {
        static final String SPOTIFY_PACKAGE = "com.spotify.music";
        static final String PLAYBACK_STATE_CHANGED = SPOTIFY_PACKAGE + ".playbackstatechanged";
        static final String QUEUE_CHANGED = SPOTIFY_PACKAGE + ".queuechanged";
        static final String METADATA_CHANGED = SPOTIFY_PACKAGE + ".metadatachanged";
    }

    private OnTitleChangedListener listener;

    @Override
    public void onReceive(Context context, Intent intent) {
        // This is sent with all broadcasts, regardless of type. The value is taken from
        // System.currentTimeMillis(), which you can compare to in order to determine how
        // old the event is.
        long timeSentInMs = intent.getLongExtra("timeSent", 0L);

        String action = intent.getAction();

        if (BroadcastTypes.METADATA_CHANGED.equals(action)) {
            String trackId = intent.getStringExtra("id");
            String artistName = intent.getStringExtra("artist");
            String albumName = intent.getStringExtra("album");
            String trackName = intent.getStringExtra("track");
            int trackLengthInSec = intent.getIntExtra("length", 0);
            if(listener != null)
                listener.onTrackChanged(trackId);
            // Do something with extracted information...
        } else if (BroadcastTypes.PLAYBACK_STATE_CHANGED.equals(action)) {
            boolean playing = intent.getBooleanExtra("playing", false);
            int positionInMs = intent.getIntExtra("playbackPosition", 0);
            // Do something with extracted information
        } else if (BroadcastTypes.QUEUE_CHANGED.equals(action)) {
            // Sent only as a notification, your app may want to respond accordingly.
        }
    }

    public void setListener(OnTitleChangedListener listener) {
        this.listener = listener;
    }

    @FunctionalInterface
    public interface OnTitleChangedListener{
        void onTrackChanged(String id);
    }

}
