package lmu.hradio.hradioshowcase.view.adapter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.recyclerview.widget.RecyclerView;

import org.omri.radioservice.metadata.Textual;
import org.omri.radioservice.metadata.TextualDabDynamicLabel;
import org.omri.radioservice.metadata.TextualType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.hradio.timeshiftplayer.SkipItem;
import lmu.hradio.hradioshowcase.BuildConfig;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.model.view.TextData;
import lmu.hradio.hradioshowcase.util.ColorUtils;
import lmu.hradio.hradioshowcase.util.DabUtlis;


public class TimeShiftRecyclerViewAdapter extends RecyclerView.Adapter<TimeShiftRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = TimeShiftRecyclerViewAdapter.class.getSimpleName();

    //private List<SkipItem> skipItems;
    private LongSparseArray<SkipItem> skipItems;

    private SkipItemClickListener listener;
    private SkipItemLongClickListener longClickListener;
    private SkipItem currentSkipItem;
    private boolean carMode;

    private ExecutorService threadPool = Executors.newFixedThreadPool(2);

    public TimeShiftRecyclerViewAdapter(List<SkipItem> items, SkipItemClickListener listener, SkipItemLongClickListener longListener) {
        this(items, listener, longListener, false);
    }


    public TimeShiftRecyclerViewAdapter(List<SkipItem> items, SkipItemClickListener listener, SkipItemLongClickListener longListener, boolean mode) {
        //this.skipItems = new ArrayList<>(items);
        this.skipItems = new LongSparseArray<>();
        for(SkipItem item : items) {
            if(item.getSbtRealTime() == 0) {
                this.skipItems.append(item.getRelativeTimepoint(), item);
            } else {
                this.skipItems.append(item.getSbtRealTime(), item);
            }
        }
        this.listener = listener;
        this.longClickListener = longListener;
        this.carMode = mode;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (carMode) view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.skip_item_car_view, parent, false);
        else view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.skip_item_recycler_view_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.skipItem = skipItems.valueAt(position);
        holder.skipCover.setVisibility(View.GONE);
        if (holder.skipItem.getSkipVisual() != null)
            holder.skipLoadingProgress.setVisibility(View.VISIBLE);
        holder.itemView.setOnClickListener(v -> listener.onItemClicked(holder.skipItem));
        holder.itemView.setOnLongClickListener(v -> longClickListener.onItemLongClicked(holder.skipItem));

        if (holder.skipItem.equals(currentSkipItem)) {
            @ColorInt int color = ColorUtils.resolveAttributeColor(R.attr.timeshift_selected_background, holder.itemView.getContext());
            holder.itemView.findViewById(R.id.content_container).setBackgroundColor(color);

        } else {
            @ColorInt int color = ColorUtils.resolveAttributeColor(R.attr.timeshift_background, holder.itemView.getContext());
            holder.itemView.findViewById(R.id.content_container).setBackgroundColor(color);
        }

        parseTextual(holder);


        if (mViewtasks.get(holder.itemView) != null) {
            mViewtasks.get(holder.itemView).cancel(true);
            mViewtasks.remove(holder.itemView);
        }


        ViewLoadingTask viewTask = new ViewLoadingTask();
        mViewtasks.put(holder.itemView, viewTask);
        viewTask.executeOnExecutor(this.threadPool, holder);
    }


    private void parseTextual(ViewHolder holder) {
        Textual textual = holder.skipItem.getSkipTextual();
        if (textual != null && textual.getType() != TextualType.METADATA_TEXTUAL_TYPE_DAB_DLS) {
            holder.songName.setText(holder.skipItem.getSkipTextual().getText().trim());
            holder.artistName.setText("");
        } else if (textual != null) {
            TextData textData = DabUtlis.parseDLPlus((TextualDabDynamicLabel) textual);
            if (!textData.getContent().isEmpty() || !textData.getTitle().isEmpty()) {
                holder.songName.setText(textData.getTitle().trim());
                holder.artistName.setText(textData.getContent().trim());
            } else {
                holder.songName.setText(textData.getText());
                holder.artistName.setText("");
            }
        } else {
            if(BuildConfig.DEBUG)Log.d(TAG, "SkipItem textual is null");
        }
    }

    private ConcurrentHashMap<View, ViewLoadingTask> mViewtasks = new ConcurrentHashMap<>();


    @Override
    public int getItemCount() {
        return skipItems.size();
    }

    public List<SkipItem> getData() {
        /*
        return skipItems;
        */

        if (skipItems == null) {
            return null;
        }

        List<SkipItem> arrayList = new ArrayList<SkipItem>(skipItems.size());
        for (int i = 0; i < skipItems.size(); i++) {
            arrayList.add(skipItems.valueAt(i));
        }

        return arrayList;
    }


    @UiThread
    public void setCurrentItem(SkipItem currentSkipItem) {
        //int last = this.currentSkipItem != null ? skipItems.indexOf(this.currentSkipItem) : 0;
        int last = this.currentSkipItem != null ? skipItems.indexOfValue(this.currentSkipItem) : 0;
        this.currentSkipItem = currentSkipItem;
        //int thisItem = skipItems.indexOf(currentSkipItem);
        int thisItem = skipItems.indexOfValue(currentSkipItem);
        this.notifyItemChanged(last);
        this.notifyItemChanged(thisItem);

    }

    @UiThread
    public void addItem(SkipItem skipItem) {
        if(skipItem.getSkipTextual() != null) {
            if (BuildConfig.DEBUG)Log.d(TAG, "SkipItem added: " + new Date(skipItem.getSbtRealTime()) + " : " + skipItem.getSkipTextual().getText());
        }
        //this.skipItems.add(skipItem);
        if(skipItem.getSbtRealTime() == 0) {
            this.skipItems.append(skipItem.getRelativeTimepoint(), skipItem);
        } else {
            this.skipItems.append(skipItem.getSbtRealTime(), skipItem);
        }

        //this.notifyItemChanged(skipItems.size()-1);
        this.notifyDataSetChanged();
    }

    @UiThread
    public void removeItem(SkipItem skipItem) {

        int sparseKey = skipItems.indexOfKey(skipItem.getSbtRealTime());
        if(sparseKey >= 0) {
            skipItems.removeAt(sparseKey);
            this.notifyItemRemoved(sparseKey);
        }
    }

    public int getCurrentIndex() {
        //return skipItems.indexOf(currentSkipItem);
        return skipItems.indexOfValue(currentSkipItem);
    }

    public void clear() {
        skipItems.clear();
        notifyDataSetChanged();
    }

    @FunctionalInterface
    public interface SkipItemClickListener {
        void onItemClicked(SkipItem item);
    }

    @FunctionalInterface
    public interface SkipItemLongClickListener {
        boolean onItemLongClicked(SkipItem item);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.song_text_view)
        TextView songName;
        @BindView(R.id.artist_text_view)
        TextView artistName;
        @BindView(R.id.skip_cover_view)
        ImageView skipCover;
        @BindView(R.id.skip_loading_progress)
        ProgressBar skipLoadingProgress;
        SkipItem skipItem;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + songName.getText() + "'";
        }
    }


    private static class ViewLoadingTask extends AsyncTask<ViewHolder, Void, Bitmap> {

        private ViewHolder holder;

        @Override
        protected Bitmap doInBackground(ViewHolder... viewHolders) {
            Bitmap bitmap = null;
            for (ViewHolder viewHolder : viewHolders) {
                holder = viewHolder;
                if (viewHolder.skipItem.getSkipVisual() != null)
                    bitmap = BitmapFactory.decodeByteArray(viewHolder.skipItem.getSkipVisual().getVisualData(), 0, viewHolder.skipItem.getSkipVisual().getVisualData().length);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            holder.skipCover.setVisibility(View.VISIBLE);
            holder.skipLoadingProgress.setVisibility(View.GONE);

            if (holder.skipItem.getSkipVisual() != null && holder.skipItem.getSkipVisual().getVisualData() != null) {

                if (bitmap != null)
                    holder.skipCover.setImageBitmap(bitmap);
                else
                    holder.skipCover.setImageResource(R.drawable.outline_radio_white_48dp);
            } else {
                holder.skipCover.setImageResource(R.drawable.outline_radio_white_48dp);
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
