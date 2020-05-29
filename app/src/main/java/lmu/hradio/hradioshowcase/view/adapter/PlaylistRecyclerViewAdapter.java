package lmu.hradio.hradioshowcase.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.listener.TrackLikeService;
import lmu.hradio.hradioshowcase.util.ColorUtils;
import lmu.hradio.hradioshowcase.view.fragment.playlist.PlaylistFragment.OnRemoveItemClickedListener;


public class PlaylistRecyclerViewAdapter extends RecyclerView.Adapter<PlaylistRecyclerViewAdapter.ViewHolder> {

    private final List<TrackLikeService.Track> mValues;
    private final OnRemoveItemClickedListener mListener;
    private final String mPlaylistId;

    public PlaylistRecyclerViewAdapter(List<TrackLikeService.Track> items, OnRemoveItemClickedListener listener, String id) {
        mValues = items;
        mListener = listener;
        mPlaylistId = id;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_playlist_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.setTrack(mValues.get(position), v -> {
            if (null != mListener) {
                mListener.remove(mPlaylistId, mValues.get(position));
            }
            mValues.remove(position);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        if(mValues == null)
            return 0;
        return mValues.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.artist_text_view)
        TextView artistTextView;
        @BindView(R.id.song_text_view)
        TextView songTextView;
        @BindView(R.id.cover_image_view)
        ImageView coverImageView;

        @BindView(R.id.remove_button)
        ImageButton removeButton;

        TrackLikeService.Track mItem;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void setTrack(TrackLikeService.Track track, View.OnClickListener onClickListener){
            mItem = track;
            removeButton.setOnClickListener(onClickListener);
            @ColorInt int color = ColorUtils.resolveAttributeColor(R.attr.colorPrimaryText, itemView.getContext());
            removeButton.setColorFilter(color);
            songTextView.setText(track.getName());
            if(track.getImages() != null && track.getImages().length > 0)
                 Glide.with(itemView).load(track.getImages()[0].getUrl()).placeholder(R.drawable.outline_radio_white_48dp).into(coverImageView);

            artistTextView.setText(track.getArtist());
        }
    }
}
