package lmu.hradio.hradioshowcase.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import lmu.hradio.hradioshowcase.view.fragment.dialog.SelectPlaylistDialogFragment;


public class PlayListSelectionRecyclerViewAdapter extends RecyclerView.Adapter<PlayListSelectionRecyclerViewAdapter.ViewHolder> {

    private List<TrackLikeService.PlayList> mValues;
    private String selectedID;
    private final SelectPlaylistDialogFragment.OnSelectPlaylistListener listener;

    public PlayListSelectionRecyclerViewAdapter(List<TrackLikeService.PlayList> values, String selectedID, SelectPlaylistDialogFragment.OnSelectPlaylistListener listener) {
        this.mValues = values;
        this.listener = listener;
        this.selectedID = selectedID;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_playlist_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.playList = mValues.get(position);
        holder.nameTextView.setText(holder.playList.getName());
        holder.itemView.setOnClickListener(v -> listener.onSelectPlaylist(holder.playList));

        int attr = (holder.playList.getId().equals(selectedID))? R.attr.colorAccent : R.attr.colorSecondary;
        @ColorInt int color = ColorUtils.resolveAttributeColor(attr,  holder.itemView.getContext());
        holder.itemView.setBackgroundColor(color);
        Glide.with(holder.itemView.getContext()).load(holder.playList.getCoverUrl()).placeholder(R.drawable.outline_radio_white_48dp).into(holder.coverImageView);
    }


    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void setSelectedId(String id) {
        this.selectedID = id;
        notifyDataSetChanged();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.cover_image_view)
        ImageView coverImageView;
        @BindView(R.id.name_text_view)
        TextView nameTextView;

        TrackLikeService.PlayList playList;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + nameTextView.getText() + "'";
        }
    }
}
