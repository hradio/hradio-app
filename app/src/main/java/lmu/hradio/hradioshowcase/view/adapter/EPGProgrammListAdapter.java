package lmu.hradio.hradioshowcase.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.model.view.LocationViewModel;
import lmu.hradio.hradioshowcase.model.view.ProgrammeViewModel;
import lmu.hradio.hradioshowcase.model.view.TimeViewModel;
import lmu.hradio.hradioshowcase.util.ColorUtils;
import lmu.hradio.hradioshowcase.util.DateUtils;
import lmu.hradio.hradioshowcase.view.fragment.player.RadioPlayerFragment;

public class EPGProgrammListAdapter extends RecyclerView.Adapter<EPGProgrammListAdapter.ViewHolder> {

    private List<ProgrammeViewModel> programList;

    private final RadioPlayerFragment.OnLoadProgrammePodcastListener listener;

    private int selected = -1;

    public EPGProgrammListAdapter(List<ProgrammeViewModel> schedules, RadioPlayerFragment.OnLoadProgrammePodcastListener listener) {
        programList = schedules;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.program_epg_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.programme = programList.get(position);
        // fill schedule time view
        StringBuilder timeBuilder = new StringBuilder();
        for (LocationViewModel location : holder.programme.getLocations()) {
            Iterator<TimeViewModel> timeIterator = location.getTimes().iterator();
            while (timeIterator.hasNext()) {
                TimeViewModel t = timeIterator.next();
                String startTime = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(t.getStartTime());
                String endTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(t.getEndTime());
                timeBuilder.append(startTime).append("-").append(endTime);
                if (timeIterator.hasNext())
                    timeBuilder.append(" / ");
            }
        }
        holder.scheduleTextView.setText(timeBuilder.toString());
        // fill programme label view
        holder.programLabelTextView.setText(holder.programme.getQualifiedName());
        // fill programme description view
        holder.descriptionTextView.setText(holder.programme.getQualifiedDescription());
        // check if programme is current


        int attrColor = isCurrentProgramme(holder.programme) ? R.attr.colorEPGSelected : R.attr.colorEPGUnselected;
        int attrColorTextPrimary = isCurrentProgramme(holder.programme) ? R.attr.colorPrimary : R.attr.colorPrimaryText;
        int attrColorTextSecondary = isCurrentProgramme(holder.programme) ? R.attr.colorPrimary : R.attr.colorSecondaryText;
        int attrColorTextTertiary = isCurrentProgramme(holder.programme) ? R.attr.colorSecondary : R.attr.colorTertiaryText;
        int color = ColorUtils.resolveAttributeColor(attrColor, holder.itemView.getContext());
        int colorTextPrimary = ColorUtils.resolveAttributeColor(attrColorTextPrimary, holder.itemView.getContext());
        int colorTextSecondary = ColorUtils.resolveAttributeColor(attrColorTextSecondary, holder.itemView.getContext());
        int colorTextTertiary = ColorUtils.resolveAttributeColor(attrColorTextTertiary, holder.itemView.getContext());
        holder.setBackroundColor(color);
        holder.descriptionTextView.setTextColor(colorTextSecondary);
        holder.scheduleTextView.setTextColor(colorTextTertiary);
        holder.programLabelTextView.setTextColor(colorTextPrimary);
        holder.podcastImageView.setColorFilter(colorTextPrimary);
        holder.podcastImageView.setVisibility(View.GONE);
        holder.itemView.setOnClickListener((v) -> {
            if (selected == position)
                selected = -1;
            else
                selected = position;
            notifyDataSetChanged();
        });

        if (holder.programme.getPodcastUrls() != null && !holder.programme.getPodcastUrls().isEmpty()) {
            holder.podcastImageView.setVisibility(View.VISIBLE);
            holder.podcastImageView.setOnClickListener(v -> listener.loadPodcasts(holder.programme.getPodcastUrls()));
        }

        holder.descriptionTextView.setMaxLines((selected == position) ? Integer.MAX_VALUE : 2);
    }


    public int getIndexOfCurrent(){
        int i = 0;
        for (ProgrammeViewModel p: programList){
            if(isCurrentProgramme(p))
                break;
            i++;
        }
        return i;
    }

    private boolean isCurrentProgramme(ProgrammeViewModel programme) {
        for (LocationViewModel location : programme.getLocations()) {
            for (TimeViewModel t : location.getTimes()) {
                if (DateUtils.isNowBeetween(t.getStartTime(), t.getEndTime())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return programList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.schedule_date_text_view)
        TextView scheduleTextView;
        @BindView(R.id.program_label_text_view)
        TextView programLabelTextView;
        @BindView(R.id.description_text_view)
        TextView descriptionTextView;
        @BindView(R.id.container)
        ConstraintLayout container;

        @BindView(R.id.podcast_image_view)
        ImageView podcastImageView;

        ProgrammeViewModel programme;


        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        private void setBackroundColor(int colorId) {
            container.setBackgroundColor(colorId);
        }

    }
}
