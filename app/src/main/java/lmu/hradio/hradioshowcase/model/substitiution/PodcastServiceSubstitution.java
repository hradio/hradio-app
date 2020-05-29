package lmu.hradio.hradioshowcase.model.substitiution;

import android.os.Parcel;

import androidx.annotation.UiThread;

import eu.hradio.httprequestwrapper.dtos.podcast.Podcast;
import eu.hradio.httprequestwrapper.dtos.podcast.PodcastItem;
import eu.hradio.substitutionapi.SubstitutionType;
import lmu.hradio.hradioshowcase.manager.ImageDownloadTask;
import lmu.hradio.hradioshowcase.model.view.ImageData;

public class PodcastServiceSubstitution extends SubstitutionItem {
    private ImageData cover;
    private String title;
    private String description;
    private String uri;
    private String author;
    private String category;
    private long duration;

    private OnImageDownloadedListener listener;

    public PodcastServiceSubstitution(PodcastItem nativePodcast, Podcast podcast){
        new ImageDownloadTask( images ->{
            for (ImageData i : images) cover = i;
            fireListener();
        }).execute(new ImageDownloadTask.UrlHolder(0, 0,nativePodcast.getImage()));

        this.title = podcast.getTitle() + ":\n" +nativePodcast.getTitle();
        this.description = nativePodcast.getDescription();
        this.uri = nativePodcast.getUrl();
        this.duration = nativePodcast.getDuration();
        this.author = nativePodcast.getAuthor();
        this.category = nativePodcast.getMimeType();
    }

    @UiThread
    private void fireListener(){
        if (listener != null) {
            listener.onDownloaded();
            listener = null;
        }
    }

    protected PodcastServiceSubstitution(Parcel in) {
        this.title = in.readString();
        this.description = in.readString();
        this.uri = in.readString();
        this.duration = in.readInt();
        this.cover = (ImageData) in.readSerializable();
        this.author = in.readString();
        this.category = in.readString();
    }

    public static final Creator<PodcastServiceSubstitution> CREATOR = new Creator<PodcastServiceSubstitution>() {
        @Override
        public PodcastServiceSubstitution createFromParcel(Parcel in) {
            return new PodcastServiceSubstitution(in);
        }

        @Override
        public PodcastServiceSubstitution[] newArray(int size) {
            return new PodcastServiceSubstitution[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeString(uri);
        parcel.writeLong(duration);
        parcel.writeSerializable(cover);
        parcel.writeString(author);
        parcel.writeString(category);

    }

    @Override
    public String getName() {
        return title;
    }

    @Override
    public String getSubstitutionType() {
        return NATIVE_TYPE;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public ImageData getCover() {
        return cover;
    }

    public String getUri() {
        return uri;
    }

    public void setListener(OnImageDownloadedListener listener) {
        this.listener = listener;
    }

    @Override
    public SubstitutionType getType() {
        return SubstitutionType.SUBSTITUTION_IPSTREAM;
    }

    @Override
    public String getArtist() {
        return author;
    }

    @Override
    public String getTitle() {
        return getName();
    }

    @Override
    public String getGenre() {
        return category;
    }

    @Override
    public long getDuration() {
        return duration;
    }
}
