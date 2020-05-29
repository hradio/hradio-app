package lmu.hradio.hradioshowcase.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.omri.radioservice.RadioService;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.util.ColorUtils;

public class TunerTypeAdapter extends RecyclerView.Adapter<TunerTypeAdapter.ViewHolder> {

    private List<RadioService> serviceList;

    TunerTypeAdapter(List<RadioService> services){
        serviceList = services;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_tuner_type_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.service = serviceList.get(position);
        switch (holder.service.getRadioServiceType()){
            case RADIOSERVICE_TYPE_FM: holder.tunerTypeImageView.setImageResource(R.drawable.fm_badge); break;
            case RADIOSERVICE_TYPE_DAB: holder.tunerTypeImageView.setImageResource(R.drawable.dab_badge); break;
            case RADIOSERVICE_TYPE_EDI: holder.tunerTypeImageView.setImageResource(R.drawable.edi_badge); break;
            case RADIOSERVICE_TYPE_SIRIUS: holder.tunerTypeImageView.setImageResource(R.drawable.fm_badge); break;
            case RADIOSERVICE_TYPE_IP: holder.tunerTypeImageView.setImageResource(R.drawable.ip_badge); break;
        }

        @ColorInt int color = ColorUtils.resolveAttributeColor(R.attr.colorPrimaryText, holder.itemView.getContext());

        holder.tunerTypeImageView.setColorFilter( color);
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.tuner_type_image_view)
        ImageView tunerTypeImageView;

        RadioService service;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
