package lmu.hradio.hradioshowcase.view.fragment.dialog;


import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.model.substitiution.SubstitutionItem;
import lmu.hradio.hradioshowcase.view.adapter.PodcastRecyclerViewAdapter;

public class SelectPodcastDialogFragment extends DialogFragment {

    private static final String TAG = SelectPodcastDialogFragment.class.getSimpleName();
    private static final String PODCAST_LIST_TAG = "podcasts";

    @BindView(R.id.podcast_recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.search_progress)
    ProgressBar searchProgress;


    private PodcastRecyclerViewAdapter adapter;


    public SelectPodcastDialogFragment() {
        // Required empty public constructor
    }

    public static SelectPodcastDialogFragment newInstance( SubstitutionItem... podcasts) {
        SelectPodcastDialogFragment fragmentDialog = new SelectPodcastDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(PODCAST_LIST_TAG, new ArrayList<>(Arrays.asList(podcasts)));
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
        View view = inflater.inflate(R.layout.fragment_dialog_podcast_list, container, false);
        ButterKnife.bind(this, view);
        Bundle args = this.getArguments();
        if(args != null) {
            List<SubstitutionItem> podcasts = args.getParcelableArrayList(PODCAST_LIST_TAG);
            if(podcasts.isEmpty()){
                searchProgress.setVisibility(View.VISIBLE);
            }else{
                searchProgress.setVisibility(View.GONE);

            }

            if(getActivity() instanceof PodcastListInteractionListener) {
                if (adapter == null) {
                    adapter = new PodcastRecyclerViewAdapter(podcasts, podcast ->{
                        Objects.requireNonNull(getDialog()).dismiss();
                        ((PodcastListInteractionListener) getActivity()).onPodcastSelected(podcast);
                    });
                    recyclerView.setAdapter(adapter);
                }else
                    adapter.addItems(podcasts);
            }else{
                throw new RuntimeException("Activity no instance of PodcastListInteractionListener");
            }
        }
        return view;
    }

    public void addPodcasts(SubstitutionItem[] podcasts){
        List<SubstitutionItem> podcastSubstitutions = Arrays.asList(podcasts);
        if(!podcastSubstitutions.isEmpty())
            searchProgress.setVisibility(View.GONE);

        if (adapter == null) {
            adapter = new PodcastRecyclerViewAdapter(podcastSubstitutions, (PodcastListInteractionListener) getActivity());
            recyclerView.setAdapter(adapter);
        }else
            adapter.addItems(podcastSubstitutions);

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(PODCAST_LIST_TAG, adapter.getItems());
        setArguments(bundle);

    }

    @Override
    public void onResume() {
        super.onResume();
        if(getDialog() != null && getDialog().getWindow() != null)
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }


    @OnClick(R.id.ok_button)
    void onOk(){
        dismiss();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        if(dismissListener != null){
            dismissListener.onDismiss();
            dismissListener = null;
        }
        super.onDismiss(dialog);
    }

    private DismissListener dismissListener;

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        if(manager.isStateSaved())
            return;
        super.show(manager, tag);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        return super.onCreateDialog(bundle);
    }

    public void setOnDismissListener(DismissListener listener) {
        dismissListener = listener;
    }

    @FunctionalInterface
    public interface DismissListener{
        void onDismiss();
    }

    @FunctionalInterface
    public interface PodcastListInteractionListener{
        void onPodcastSelected(SubstitutionItem podcast);
    }

}
