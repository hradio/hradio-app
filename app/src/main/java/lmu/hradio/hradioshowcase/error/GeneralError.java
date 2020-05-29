package lmu.hradio.hradioshowcase.error;

import android.os.Bundle;

import java.io.Serializable;

/**
 * Custom error class to handle failed actions
 */
public class GeneralError extends Error implements Serializable {
    private static final long serialVersionUID = 5943765937379811272L;

    public static final String SERIALIZABLE_EXTRA = "serializable-extra";

    //possible identifier codes
    /**
     * Exo player loading error (programme podcast streaming)
     */
    public static final int EXO_ERROR_LOAD = 0;
    /**
     * Exo player buffer error (programme podcast streaming)
     */
    public static final int EXO_BUFFER_ERROR = 1;
    /**
     * Exo player playback error (programme podcast streaming)
     */
    public static final int EXO_ERROR_PLAYBACK = 2;
    /**
     * Spotify error occured
     */
    public static final int SUBSTITUTION_ERROR = 3;
    /**
     * Spotify token expired
     */
    public static final int SPOTIFY_TOKEN_ERROR = 401;
    /**
     * Network exception occured
     */
    public static final int NETWORK_ERROR = 4;
    /**
     * playback controls failed
     */
    public static final int CONTROLLS_ERROR = 5;
    /**
     * Location requested but permission revoked
     */
    public static final int LOCATION_ERROR_PERMISSION_DENIED = 6;
    /**
     * tuner error occurred (signal lost or no tuner found)
     */
    public static final int TUNER_ERROR = 7;
    /**
     * no epg info found
     */
    public static final int EPG_ERROR = 8;
    /**
     * no service found
     */
    public static final int SERVICE_SEARCH = 9;
    /**
     * no recommendation found
     */
    public static final int RECOMMENDER = 10;
    /**
     * user data collection failed
     */
    public static final int USER_DATA_COLLECTION = 11;
    /**
     * timeshift player creation failed
     */
    public static final int TIMESHIFT = 12;
    /**
     * spotify disabled in settings
     */
    public static final int SUBSTITUTION_DISABLED = 13;
    /**
     * Location requested but disabled
     */
    public static final int LOCATION_ERROR_DISABLED = 14;
    /**
     * Dab or fm feature requested but no tuner available
     */
    public static final int NO_DAB_FM_TUNER = 15;
    /**
     * IP tuner feature requested but no tuner available
     */
    public static final int NO_IP_TUNER = 16;
    /**
     * Selected tuner not available
     */
    public static final int TUNER_NOT_AVAILABLE = 17;
    /**
     * The running tuner do not support seeking
     */
    public static final int SEEKING_NOT_SUPPORTED = 18;
    /**
     * no questionaires found
     */
    public static final int PRUDAC_ERROR = 19;
    /**
     * no tuner for selected favorite service available
     */
    public static final int FAVORITE_NOT_AVAILABLE_ERROR = 20;

    /**
     * external storage permission not granted
     */
    public static final int STORAGE_PERMISSION_DENIED =21 ;

    /**
     * invalid web view config
     */
    public static final int INVALID_WEBVIEW_CONFIG = 22;

    /**
     * play service clicked while tuner scanning
     */
    public static final int PLAY_WHEN_SCANNING = 23;

    /**
     * device api level to low
     */
    public static final int API_ERROR = 24;

    public static final int API_SPOTIFY_ERROR =25 ;

    /**
     * user report failed
     */
    public static final int USER_REPORT_ERROR =26 ;

    /**
     * the errors identifier
     */
    private int errorCode;

    private Bundle extra;

    /**
     * Creates new GeneralError objects
     *
     * @param errorCode - the error identifier
     */
    public GeneralError(int errorCode) {
        super();
        this.errorCode = errorCode;
    }


    /**
     * get the errors identifier
     *
     * @return errors identifier
     */
    public int getErrorCode() {
        return errorCode;
    }

    public Bundle getExtra() {
        return extra;
    }

    public void setExtra(Bundle extra) {
        this.extra = extra;
    }
}
