package lmu.hradio.hradioshowcase.model.substitiution;

import android.os.Parcel;

import androidx.annotation.UiThread;

import eu.hradio.substitutionapi.SubstitutionType;
import lmu.hradio.hradioshowcase.model.view.ImageData;
import lmu.hradio.hradioshowcase.spotify.web.model.Image;
import lmu.hradio.hradioshowcase.spotify.web.model.Track;
import lmu.hradio.hradioshowcase.util.ImageDataHelper;

public class TrackSpotifySubstitution extends SpotifySubstitution  {

    private static final long serialVersionUID = 2512882839761904747L;
    private ImageData cover;
    private String uri;
    private String coverUrl;
    private String artist;
    private String title;
    private long duration;
    private Image[] images;

    private OnImageDownloadedListener listener;

    public TrackSpotifySubstitution(Track track){
        this.uri = track.getUri();
        this.artist = resolveArtist(track);
        this.title = track.getName();
        this.duration = track.getDuration_ms();
        if(track.getAlbum() != null) {
            this.coverUrl = ImageDataHelper.fromSpotifyImages(track.getAlbum().getImages(), images -> {
                for (ImageData i : images) cover = i;
                fireListener();
            });
            images = track.getAlbum().getImages();
        }
    }

    @UiThread
    private void fireListener(){
        if (listener != null) {
            listener.onDownloaded();
            listener = null;
        }
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
    public String getDescription() {
        return getTitle();
    }

    @Override
    public ImageData getCover() {
        return cover;
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public Image[] getImages() {
        return images;
    }

    @Override
    public void setListener(OnImageDownloadedListener listener) {
        this.listener = listener;
    }

    @Override
    public SubstitutionType getType() {
        return SubstitutionType.SUBSTITUTION_IPSTREAM;
    }

    @Override
    public String getArtist() {
        return artist;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getGenre() {
        return "Song";
    }


    @Override
    public String getCoverUrl() {
        return coverUrl;
    }

    @Override
    public long getDuration() {
        return duration;
    }

    private String resolveArtist(Track track){
        StringBuilder artist = new StringBuilder();
        for(int i = 0 ; i < track.getArtists().length; i++){
            artist.append(track.getArtists()[i].getName());
            if(i < track.getArtists().length-1)
                artist.append(" ft. ");
        }
        return artist.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(uri);
        parcel.writeString(artist);
        parcel.writeString(title);
        parcel.writeLong(duration);
        parcel.writeString(coverUrl);
        parcel.writeSerializable(cover);
    }

    public TrackSpotifySubstitution(Parcel in) {
        this.uri = in.readString();
        this.artist = in.readString();
        this.title = in.readString();
        this.duration = in.readLong();
        this.coverUrl = in.readString();
        this.cover = (ImageData) in.readSerializable();
    }

    public static final Creator<TrackSpotifySubstitution> CREATOR = new Creator<TrackSpotifySubstitution>() {
        @Override
        public TrackSpotifySubstitution createFromParcel(Parcel in) {
            return new TrackSpotifySubstitution(in);
        }

        @Override
        public TrackSpotifySubstitution[] newArray(int size) {
            return new TrackSpotifySubstitution[size];
        }
    };

}
