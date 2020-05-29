package lmu.hradio.hradioshowcase.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.manager.substitution.SubstitutionProviderType;
import lmu.hradio.hradioshowcase.util.SharedPreferencesHelper;

public class SelectSubstitutionProviderAdapter extends RecyclerView.Adapter<SelectSubstitutionProviderAdapter.ViewHolder> {

    private SubstitutionProviderType selected;

    public SelectSubstitutionProviderAdapter(SubstitutionProviderType selected) {
        this.selected = selected;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_multiple_choice_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.toggle.setOnCheckedChangeListener((c,b) ->{});
        holder.toggle.setChecked(SubstitutionProviderType.values()[position] == selected);
        holder.toggle.setText(holder.itemView.getResources().getString(SubstitutionProviderType.values()[position].getDisplayResource()));
        holder.toggle.setOnCheckedChangeListener((compoundButton, b) ->{
            selected = SubstitutionProviderType.values()[position];
            SharedPreferencesHelper.put(holder.itemView.getContext(), SharedPreferencesHelper.SUBSTITUTION_PROVIDER_TYPE, position);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return SubstitutionProviderType.values().length;
    }


    static class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.selected_toggle)
        CheckBox toggle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
