package lmu.hradio.hradioshowcase.view.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.omri.radioservice.RadioService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.listener.PlayRadioServiceListener;
import lmu.hradio.hradioshowcase.model.view.RadioServiceViewModel;
import lmu.hradio.hradioshowcase.view.component.FavoriteImageButton;


public class RadioServiceRecyclerViewAdapter extends RecyclerView.Adapter<RadioServiceRecyclerViewAdapter.ViewHolder> {

    private List<RadioServiceViewModel> mValues;
    private final PlayRadioServiceListener mListener;
    private final int layoutRessource;

    private ExecutorService threadPool = Executors.newFixedThreadPool(2);


    public RadioServiceRecyclerViewAdapter(List<RadioServiceViewModel> values, PlayRadioServiceListener listener, int layoutRessource) {
        this.mValues = values;
        mListener = listener;
        this.layoutRessource = layoutRessource;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutRessource, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        if(mValues.isEmpty()){
            holder.favoriteImageButton.setVisibility(View.GONE);
            holder.coverImageView.setVisibility(View.GONE);
            holder.serviceName.setText(R.string.no_service_found);
            holder.tunerTypeRecyclerView.setAdapter(null);
            return;
        }
        holder.favoriteImageButton.setVisibility(View.VISIBLE);
        holder.coverImageView.setVisibility(View.VISIBLE);

        holder.radioService = mValues.get(position);
        holder.serviceName.setText(holder.radioService.getServiceLabel());
        holder.favoriteImageButton.setRadioService(holder.radioService);
        holder.tunerTypeRecyclerView.setAdapter(new TunerTypeAdapter(holder.radioService.getRadioServices()));
        holder.itemView.setOnClickListener(v -> mListener.onRadioServiceSelected(holder.radioService));

        new ViewLoadingTask().executeOnExecutor(threadPool, holder);

    }

    @Override
    public int getItemCount() {
        return mValues.isEmpty()? 1 : mValues.size();
    }

    public void addContent(List<RadioServiceViewModel> values) {
        mValues= values;
        notifyDataSetChanged();
    }

    public void remove(RadioServiceViewModel service) {
        mValues.remove(service);
        notifyDataSetChanged();
    }

    public void clear() {
        mValues = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addContent(RadioServiceViewModel service, RadioService type) {
        if(mValues.contains(service)){
            for(RadioServiceViewModel model : mValues){
                if(model.equals(service)){
                    if(!model.getRadioServices().contains(type))
                        model.getRadioServices().add(type);
                }
            }
        }else{
            mValues.add(service);
        }
        notifyItemChanged(mValues.indexOf(service));
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.cover_image_view)
        ImageView coverImageView;
        @BindView(R.id.name_text_view)
        TextView serviceName;
        @BindView(R.id.favorite_button)
        FavoriteImageButton favoriteImageButton;
        @BindView(R.id.tuner_type_list)
        RecyclerView tunerTypeRecyclerView;


        RadioServiceViewModel radioService;
        private View view;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            this.view =view;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + serviceName.getText() + "'";
        }
    }


    private static class ViewLoadingTask extends AsyncTask<ViewHolder, Void, Bitmap> {

        private ViewHolder holder;

        @Override
        protected Bitmap doInBackground(ViewHolder... viewHolders) {
            Bitmap bitmap = null;
            for (ViewHolder viewHolder : viewHolders) {
                if(viewHolder.radioService == null)
                    continue;
                holder = viewHolder;
                if(viewHolder.radioService.getLogo() != null && viewHolder.radioService.getLogo().getImageData() != null)
                    bitmap = BitmapFactory.decodeByteArray(viewHolder.radioService.getLogo().getImageData(), 0, viewHolder.radioService.getLogo().getImageData().length);
                else
                    holder.radioService.setListener(() -> new ViewLoadingTask().executeOnExecutor(Executors.newSingleThreadExecutor(),holder));

            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if(bitmap != null){
                holder.coverImageView.setImageBitmap(bitmap);
            }else {
                Drawable drawable = ContextCompat.getDrawable(holder.coverImageView.getContext(), R.drawable.outline_radio_white_48dp);
                if(drawable != null) {
                    DrawableCompat.setTint(drawable, ContextCompat.getColor(holder.itemView.getContext(), R.color.colorPrimaryDark));
                }

                holder.coverImageView.setImageDrawable(drawable);
                holder.coverImageView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorPrimaryTextDark));

            }

        }

        @Override
        protected void onCancelled(Bitmap bitmap) {
            super.onCancelled(bitmap);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

    }

}
