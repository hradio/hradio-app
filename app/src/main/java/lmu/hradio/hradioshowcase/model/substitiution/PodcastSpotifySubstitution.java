package lmu.hradio.hradioshowcase.model.substitiution;

import android.os.Parcel;

import androidx.annotation.UiThread;

import eu.hradio.substitutionapi.SubstitutionType;
import lmu.hradio.hradioshowcase.model.view.ImageData;
import lmu.hradio.hradioshowcase.spotify.web.model.Image;
import lmu.hradio.hradioshowcase.spotify.web.model.PodcastEpisodeShort;
import lmu.hradio.hradioshowcase.util.ImageDataHelper;

public class PodcastSpotifySubstitution extends SpotifySubstitution{
    private static final long serialVersionUID = 5408585408196352398L;
    private ImageData cover;
    private String title;
    private String description;
    private String uri;
    private String coverUrl;
    private long duration;
    private Image[] images;

    private transient OnImageDownloadedListener listener;

    public PodcastSpotifySubstitution(PodcastEpisodeShort episode) {
        ImageDataHelper.fromSpotifyImages(episode.getImages(), images ->{
            for (ImageData i : images) cover = i;
            fireListener();
        });
        this.title = episode.getName();
        this.description = episode.getDescription();
        this.uri = episode.getUri();
        this.coverUrl = episode.getImages()[0].getUrl();
        this.duration = episode.getDuration_ms();
        this.images = episode.getImages();
    }


    @UiThread
    private void fireListener(){
        if (listener != null) {
            listener.onDownloaded();
            listener = null;
        }
    }


    public ImageData getCover() {
        return cover;
    }

    public void setCover(ImageData cover) {
        this.cover = cover;
    }

    @Override
    public String getName() {
        return getTitle();
    }

    @Override
    public String getId() {
        return uri;
    }

    @Override
    public SubstitutionType getType() {
        return SubstitutionType.SUBSTITUTION_IPSTREAM;
    }

    @Override
    public String getArtist() {
        return "";
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String getGenre() {
        return "Podcast";
    }

    @Override
    public long getDuration() {
        return duration;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    protected PodcastSpotifySubstitution(Parcel in) {
        title = in.readString();
        description = in.readString();
        duration = in.readLong();
        cover = (ImageData) in.readSerializable();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(description);
        dest.writeLong(duration);
        dest.writeSerializable(cover);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PodcastSpotifySubstitution> CREATOR = new Creator<PodcastSpotifySubstitution>() {
        @Override
        public PodcastSpotifySubstitution createFromParcel(Parcel in) {
            return new PodcastSpotifySubstitution(in);
        }

        @Override
        public PodcastSpotifySubstitution[] newArray(int size) {
            return new PodcastSpotifySubstitution[size];
        }
    };

    public OnImageDownloadedListener getListener() {
        return listener;
    }

    public void setListener(OnImageDownloadedListener listener) {
        this.listener = listener;
    }

    public String getUri() {
        return uri;
    }

    @Override
    public Image[] getImages() {
        return images;
    }

    @Override
    public String getCoverUrl() {
        return coverUrl;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }


}
