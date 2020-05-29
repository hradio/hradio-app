package lmu.hradio.hradioshowcase.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.omri.tuner.ReceptionQuality;

import java.util.Objects;

import lmu.hradio.hradioshowcase.events.PlayerEvents;

/**
 * tuner signal strength broadcast class
 */
public final class SignalStrengthBroadCast {

    /**
     * tuner signal strength broadcast receiver
     */
    public static class Receiver extends BroadcastReceiver {

        /**
         * signal change callback listener
         */
        private SignalStrengthListener listener;

        /**
         * Creates a new broadcast receiver
         *
         * @param listener - the callback listener
         * @param context  - the context object
         */
        public Receiver(@NonNull SignalStrengthListener listener, @NonNull Context context) {
            this.listener = listener;
            IntentFilter filter = new IntentFilter();
            filter.addAction(PlayerEvents.SIGNAL_STRENGT_CHANGE);
            LocalBroadcastManager.getInstance(context.getApplicationContext()).registerReceiver(this, filter);
        }

        /**
         * Receives {@link PlayerEvents#SIGNAL_STRENGT_CHANGE} event and call listeners handle change method
         *
         * @param context - context object
         * @param intent  - the received intent
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.requireNonNull(intent.getAction()).equals(PlayerEvents.SIGNAL_STRENGT_CHANGE)) {
                ReceptionQuality quality = (ReceptionQuality) intent.getSerializableExtra(PlayerEvents.SIGNAL_STRENGT_CHANGE_EXTRA);
                listener.onSignalStrengthChanged(quality);
            }
        }

        /**
         * unregister the receiver instance
         *
         * @param context - context object
         */
        public void unregister(Context context) {
            LocalBroadcastManager.getInstance(context.getApplicationContext()).unregisterReceiver(this);
            listener = null;
        }
    }

    /**
     * tuner signal strength broadcast sender
     */
    public static class Sender {

        /**
         * the context object
         */
        private Context context;

        /**
         * creates new Sender objects
         *
         * @param context - the context object
         */
        public Sender(Context context) {
            this.context = context;
        }

        /**
         * Broadcast the new reception quality
         *
         * @param quality - the new reception quality
         */
        public void receptionQualityChanged(ReceptionQuality quality) {
            Intent intent = new Intent(PlayerEvents.SIGNAL_STRENGT_CHANGE);
            intent.putExtra(PlayerEvents.SIGNAL_STRENGT_CHANGE_EXTRA, quality);
            LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent);
        }

    }

    /**
     * Listener interface to handle signal strength change events
     */
    @FunctionalInterface
    public interface SignalStrengthListener {
        /**
         * callback method  to handle signal strength change events
         *
         * @param newQuality - the new quality
         */
        void onSignalStrengthChanged(ReceptionQuality newQuality);
    }

}
