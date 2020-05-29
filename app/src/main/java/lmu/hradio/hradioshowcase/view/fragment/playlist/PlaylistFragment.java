package lmu.hradio.hradioshowcase.view.fragment.playlist;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.listener.PlayBackDelegate;
import lmu.hradio.hradioshowcase.listener.TrackLikeService;
import lmu.hradio.hradioshowcase.view.adapter.PlaylistRecyclerViewAdapter;


public class PlaylistFragment extends Fragment implements TrackLikeService.TrackChangeListener {

    private static final String ARG_PLAYLIST_ID = "playlist-id";
    private String playListID;
    private OnRemoveItemClickedListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PlaylistFragment() {
    }

    public static PlaylistFragment newInstance(String id) {
        PlaylistFragment fragment = new PlaylistFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PLAYLIST_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            playListID = getArguments().getString(ARG_PLAYLIST_ID);

        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist_list, container, false);
        // Set the adapter
        if (getActivity() instanceof PlayBackDelegate){
            if (view instanceof RecyclerView) {
                RecyclerView recyclerView = (RecyclerView) view;
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                ((PlayBackDelegate) getActivity()).getTrackLikeService().getPlaylist(playListID ,
                        playlist-> recyclerView.setAdapter(new PlaylistRecyclerViewAdapter(playlist.getTracks(), mListener, playListID)));



            }
        }
        return view;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof PlayBackDelegate){
            mListener =  ((PlayBackDelegate) context).getTrackLikeService()::removeTrackFromPlayList;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onTrackChange(TrackLikeService.Track newTrack) {

    }


    public interface OnRemoveItemClickedListener {
        void remove(String id, TrackLikeService.Track item);
    }
}
