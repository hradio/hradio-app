package lmu.hradio.hradioshowcase.view.fragment.dialog;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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
import lmu.hradio.hradioshowcase.listener.TrackLikeService;
import lmu.hradio.hradioshowcase.manager.prudac.PrudacRestClient;
import lmu.hradio.hradioshowcase.model.substitiution.SubstitutionItem;
import lmu.hradio.hradioshowcase.util.SharedPreferencesHelper;
import lmu.hradio.hradioshowcase.view.adapter.PlayListSelectionRecyclerViewAdapter;
import lmu.hradio.hradioshowcase.view.adapter.PodcastRecyclerViewAdapter;
import lmu.hradio.hradioshowcase.view.fragment.SettingsFragment;

import static lmu.hradio.hradioshowcase.util.SharedPreferencesHelper.SUBSTITUTION_PLAYLIST_ID_KEY;
import static lmu.hradio.hradioshowcase.util.SharedPreferencesHelper.SUBSTITUTION_PLAYLIST_NAME_KEY;

public class SelectPlaylistDialogFragment extends DialogFragment {

    private static final String TAG = SelectPlaylistDialogFragment.class.getSimpleName();
    private static final String PLAYLIST_TAG = "playlist";
    private String selectedID;
    private PlayListSelectionRecyclerViewAdapter adapter;

    @BindView(R.id.playlist_recycler_view)
    RecyclerView recyclerView;

    SettingsFragment.SettingsActivity activity;

    public SelectPlaylistDialogFragment() {
        // Required empty public constructor
    }

    public static SelectPlaylistDialogFragment newInstance(List<TrackLikeService.PlayList> playLists) {
        SelectPlaylistDialogFragment fragmentDialog = new SelectPlaylistDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(PLAYLIST_TAG, new ArrayList<>(playLists));
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
        View view = inflater.inflate(R.layout.fragment_dialog_playlist_list, container, false);
        ButterKnife.bind(this, view);
        Bundle args = this.getArguments();
        if(args != null) {
            List<TrackLikeService.PlayList> playLists = (List<TrackLikeService.PlayList>) args.getSerializable(PLAYLIST_TAG);
            selectedID = SharedPreferencesHelper.getString(view.getContext(), SUBSTITUTION_PLAYLIST_ID_KEY);
            adapter = new PlayListSelectionRecyclerViewAdapter(playLists, selectedID, playList -> {
                String selectedID = SharedPreferencesHelper.getString(view.getContext(), SUBSTITUTION_PLAYLIST_ID_KEY);
                if(selectedID.equals(playList.getId())) {
                    SharedPreferencesHelper.put(view.getContext(), SUBSTITUTION_PLAYLIST_NAME_KEY, "");
                    SharedPreferencesHelper.put(view.getContext(), SUBSTITUTION_PLAYLIST_ID_KEY, "");
                    adapter.setSelectedId("");
                    if(activity != null)
                        activity.removeSubstitutionPlayList();
                }else{
                    SharedPreferencesHelper.put(view.getContext(), SUBSTITUTION_PLAYLIST_NAME_KEY, playList.getName());
                    SharedPreferencesHelper.put(view.getContext(), SUBSTITUTION_PLAYLIST_ID_KEY, playList.getId());
                    adapter.setSelectedId(playList.getId());
                    if(activity != null)
                        activity.setSubstitutionPlayList(playList);
                }
            });
            recyclerView.setAdapter(adapter);
        }
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        if(getDialog() != null && getDialog().getWindow() != null)
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        if(getActivity() instanceof SettingsFragment.SettingsActivity)
            activity = (SettingsFragment.SettingsActivity) getActivity();
        return super.onCreateDialog(bundle);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if(listener != null){
            listener.onDismiss();
            listener = null;
        }
    }

    private OnDismissListener listener;

    public void registerOnDismissListener(OnDismissListener listener) {
        this.listener = listener;
    }

    @FunctionalInterface
    public interface OnSelectPlaylistListener{
        void onSelectPlaylist(TrackLikeService.PlayList playList);
    }

    @FunctionalInterface
    public interface OnDismissListener{
        void onDismiss();
    }

}
