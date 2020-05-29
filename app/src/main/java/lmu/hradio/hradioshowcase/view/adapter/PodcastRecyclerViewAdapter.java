package lmu.hradio.hradioshowcase.view.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.model.view.ImageData;
import lmu.hradio.hradioshowcase.model.substitiution.SubstitutionItem;
import lmu.hradio.hradioshowcase.util.ImageDataHelper;
import lmu.hradio.hradioshowcase.view.fragment.dialog.SelectPodcastDialogFragment;


public class PodcastRecyclerViewAdapter extends RecyclerView.Adapter<PodcastRecyclerViewAdapter.ViewHolder> {

    private ArrayList<SubstitutionItem> mValues;
    private final SelectPodcastDialogFragment.PodcastListInteractionListener mListener;

    public PodcastRecyclerViewAdapter(List<SubstitutionItem>  values, SelectPodcastDialogFragment.PodcastListInteractionListener listener) {
        this.mValues = new ArrayList<>();
        addItems(values);
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_podcast_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.podcast = mValues.get(position);
        holder.podcastNameTextView.setText(holder.podcast.getName());
        holder.publisherTextView.setVisibility(View.GONE);
        loadCover(holder, position);
        holder.view.setOnClickListener(v -> {
            if (null != mListener) {
                mListener.onPodcastSelected(holder.podcast);
            }
        });
    }

    private void loadCover(ViewHolder holder, int position){
        if(holder.podcast.getCover() != null){
            setImage(holder.coverImageView, holder.podcast.getCover());
        }else{

            holder.podcast.setListener(() -> notifyItemChanged(position));
        }
    }



    private void setImage(ImageView imageView, ImageData data){
        Bitmap logoBmp = ImageDataHelper.decodeToBitmap(data);
        if (logoBmp != null) {
            imageView.setImageBitmap(logoBmp);
        } else {
            imageView.setImageDrawable(ContextCompat.getDrawable(imageView.getContext(), R.drawable.outline_radio_white_48dp));
        }
    }



    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void addItems(List<SubstitutionItem>  podcasts) {
        mValues.addAll(podcasts);
        notifyDataSetChanged();
    }

    public ArrayList<SubstitutionItem> getItems() {
        return mValues;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.cover_image_view)
        ImageView coverImageView;
        @BindView(R.id.name_text_view) TextView podcastNameTextView;
        @BindView(R.id.publisher_text_view) TextView publisherTextView;
        SubstitutionItem podcast;
        private View view;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            this.view =view;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + podcastNameTextView.getText() + "'";
        }
    }
}
