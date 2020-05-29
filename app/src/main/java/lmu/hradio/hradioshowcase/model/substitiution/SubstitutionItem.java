package lmu.hradio.hradioshowcase.model.substitiution;

import android.os.Parcelable;

import eu.hradio.substitutionapi.Substitution;
import lmu.hradio.hradioshowcase.model.view.ImageData;

public abstract class SubstitutionItem extends Substitution implements Parcelable {

    public static String SPOTIFY_TYPE = "Spotify";
    public static String NATIVE_TYPE = "Podcast";

    public abstract String getName();
    public abstract String getSubstitutionType();
    public abstract String getDescription();
    public abstract ImageData getCover();
    public abstract String getUri();

    public abstract void setListener(OnImageDownloadedListener listener);

    @FunctionalInterface
    public interface OnImageDownloadedListener{
        void onDownloaded();
    }
}
