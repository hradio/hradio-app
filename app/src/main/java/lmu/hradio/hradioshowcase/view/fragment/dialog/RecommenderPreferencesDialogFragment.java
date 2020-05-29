package lmu.hradio.hradioshowcase.view.fragment.dialog;


import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.hradio.httprequestwrapper.dtos.recommendation.Recommender;
import eu.hradio.httprequestwrapper.dtos.recommendation.WeightedRecommender;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.util.SharedPreferencesHelper;
import lmu.hradio.hradioshowcase.view.adapter.RecommenderRecyclerViewAdapter;

public class RecommenderPreferencesDialogFragment extends DialogFragment {

    private static final String TAG = RecommenderPreferencesDialogFragment.class.getSimpleName();
    private static final String AVAIlABLE_RECOMMENDER_KEY = "available-recommender";

    @BindView(R.id.recommender_recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.distance_seek_bar)
    SeekBar distanceSeekbar;

    @BindView(R.id.selected_distance_text_view)
    TextView distanceTextView;

    private RecommenderRecyclerViewAdapter adapter;

    public RecommenderPreferencesDialogFragment() {
        // Required empty public constructor
    }


    public static RecommenderPreferencesDialogFragment newInstance(Recommender... available) {
        RecommenderPreferencesDialogFragment fragmentDialog = new RecommenderPreferencesDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArray(AVAIlABLE_RECOMMENDER_KEY, available);
        fragmentDialog.setArguments(bundle);
        return fragmentDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dialog_recommender_preferences, container, false);
        ButterKnife.bind(this, view);
        List<WeightedRecommender> recommenders = new ArrayList<>(SharedPreferencesHelper.readRecommenderPreferences(view.getContext()));
        Recommender[] availableRecommenders = new Recommender[0];
        if (getArguments() != null) {
            availableRecommenders = (Recommender[]) getArguments().getParcelableArray(AVAIlABLE_RECOMMENDER_KEY);
        }
        if(recommenders.isEmpty() && availableRecommenders != null){
            for(Recommender recommender:  availableRecommenders){
                if(recommender.getRecommenderName().equals("MoreLikeThis")){
                    recommenders.add(new WeightedRecommender(recommender, 0.05));
                } else if(recommender.getRecommenderName().equals("Expert")){
                    recommenders.add(new WeightedRecommender(recommender, 0.4));
                }else if(recommender.getRecommenderName().equals("Trend")){
                    recommenders.add(new WeightedRecommender(recommender, 0.1));
                }else if(recommender.getRecommenderName().equals("Location")){
                    recommenders.add(new WeightedRecommender(recommender, 0.25));
                }else if(recommender.getRecommenderName().equals("Category")){
                    recommenders.add(new WeightedRecommender(recommender, 0.1));
                }else if(recommender.getRecommenderName().equals("Histogram")){
                    recommenders.add(new WeightedRecommender(recommender, 0.1));
                }
            }
        }

        adapter = new RecommenderRecyclerViewAdapter();
        adapter.setConfig(recommenders, availableRecommenders);
        recyclerView.setAdapter(adapter);
        distanceSeekbar.setMax(2000);
        int progress=SharedPreferencesHelper.readUserDistance(getActivity());
        distanceSeekbar.setProgress(progress);
        distanceTextView.setText(getString(R.string.distance_unit, progress));
        adapter.setConfigChangeListener(config -> SharedPreferencesHelper.putRecommenderPreferences(getActivity(), config));

        // Sets initial value for recommendation preferences.
        SharedPreferencesHelper.putRecommenderPreferences(getActivity(), adapter.getConfig());

        distanceSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                distanceTextView.setText(getString(R.string.distance_unit, i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferencesHelper.putDistance(getActivity(), distanceSeekbar.getProgress());
            }
        });
        return view;
    }

    @OnClick(R.id.ok_button)
    void onOk(){
        dismiss();
    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        if(manager.isStateSaved())
            return;
        super.show(manager, tag);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getDialog() != null && getDialog().getWindow() != null)
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

     @NonNull
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        return super.onCreateDialog(bundle);
    }


}
