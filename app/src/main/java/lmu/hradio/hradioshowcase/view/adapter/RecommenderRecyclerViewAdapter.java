package lmu.hradio.hradioshowcase.view.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.hradio.httprequestwrapper.dtos.recommendation.Recommender;
import eu.hradio.httprequestwrapper.dtos.recommendation.WeightedRecommender;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.util.ColorUtils;
import lmu.hradio.hradioshowcase.util.Parser;

public class RecommenderRecyclerViewAdapter extends RecyclerView.Adapter<RecommenderRecyclerViewAdapter.ViewHolder> {

    private List<Recommender> availableRecommender = new ArrayList<>();
    private List<WeightedRecommender> weightedRecommenders = new ArrayList<>();

    private OnConfigChangeListener changeListener;


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_search_recommender_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Drawable buttonImage;
        if(position < weightedRecommenders.size()){
            holder.itemView.setVisibility(View.VISIBLE);
            buttonImage = ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.baseline_remove_white_36);

            holder.deleteButton.setImageDrawable(buttonImage);
            holder.weightedRecommender = weightedRecommenders.get(position);
            holder.recommenderSpinner.setVisibility(View.GONE);
            holder.recommenderContainer.setVisibility(View.VISIBLE);
            holder.recommenderTextView.setText( holder.weightedRecommender.getRecommender().getRecommenderName());
            holder.weigthtEditText.setText( String.valueOf(holder.weightedRecommender.getWeigth()));
            holder.weigthtEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void afterTextChanged(Editable editable) {
                    holder.weightedRecommender.setWeigth(editable.toString().isEmpty()? 0.0 : Double.valueOf(editable.toString()));
                    changeListener.onConfigChanges(getConfig());
                }
            });

            holder.deleteButton.setOnClickListener(view -> {
                WeightedRecommender deleted = weightedRecommenders.remove(position);
                availableRecommender.add(deleted.getRecommender());
                changeListener.onConfigChanges(getConfig());
                notifyDataSetChanged();
            });

        }else{
                holder.weigthtEditText.setText(String.valueOf(1.0));
                holder.itemView.setVisibility(View.VISIBLE);
                holder.recommenderSpinner.setVisibility(View.VISIBLE);
                holder.recommenderContainer.setVisibility(View.GONE);
                buttonImage = ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.baseline_add_white_36);
                holder.deleteButton.setImageDrawable(buttonImage);
                holder.recommenderSpinner.setAdapter(createSpinnerAdapter(holder.itemView.getContext()));
                holder.deleteButton.setOnClickListener(view -> {
                    Recommender recommender = availableRecommender.remove(holder.recommenderSpinner.getSelectedItemPosition());
                    weightedRecommenders.add(new WeightedRecommender(recommender, Double.parseDouble(holder.weigthtEditText.getText().toString())));
                    changeListener.onConfigChanges(getConfig());
                    notifyDataSetChanged();
                });

        }
    }

    private SpinnerAdapter createSpinnerAdapter(Context context){
        ArrayAdapter<Recommender> adapter = new ArrayAdapter<>(context,R.layout.simple_spinner_item , availableRecommender);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        return adapter;
    }

    @Override
    public int getItemCount() {
        return availableRecommender.isEmpty()? weightedRecommenders.size() : weightedRecommenders.size() +1;
    }

    public void setConfig(List<WeightedRecommender> recommenders, Recommender[] available){
        weightedRecommenders = new ArrayList<>();
        availableRecommender = new ArrayList<>(Arrays.asList(available));
        //Update shared
        for(WeightedRecommender r: recommenders){
            if(availableRecommender.contains(r.getRecommender())){
                weightedRecommenders.add(r);
            }
        }
        //calc remaining unused
        for(WeightedRecommender r: weightedRecommenders){
            availableRecommender.remove(r.getRecommender());
        }
        notifyDataSetChanged();
    }

    public List<WeightedRecommender> getConfig(){
        return weightedRecommenders;
    }

    public void setConfigChangeListener(OnConfigChangeListener listener){
        this.changeListener = listener;
    }

    @FunctionalInterface
    public interface OnConfigChangeListener{
        void onConfigChanges(List<WeightedRecommender> config);
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        WeightedRecommender weightedRecommender;
        @BindView(R.id.delete_button)
        ImageButton deleteButton;
        @BindView(R.id.weight_text_view)
        EditText weigthtEditText;
        @BindView(R.id.recommender_spinner)
        Spinner recommenderSpinner;
        @BindView(R.id.recommender_text_view)
        TextView recommenderTextView;

        @BindView(R.id.selected_recommender_container)
        View recommenderContainer;


        @BindView(R.id.info_button)
        ImageButton infoButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            @ColorInt int color = ColorUtils.resolveAttributeColor(R.attr.colorPrimaryText, itemView.getContext());
            deleteButton.setColorFilter(color);
            infoButton.setColorFilter(color);
        }

        @OnClick(R.id.info_button)
        public void onClickInfo(){
            new AlertDialog.Builder(itemView.getContext())
                    .setTitle(weightedRecommender.getRecommender().getRecommenderName())
                    .setMessage(Parser.parseRecommender(weightedRecommender.getRecommender(), itemView.getContext()))
                    .create().show();
        }

    }
}
