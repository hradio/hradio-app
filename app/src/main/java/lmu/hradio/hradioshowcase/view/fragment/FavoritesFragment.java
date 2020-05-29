package lmu.hradio.hradioshowcase.view.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.database.Database;
import lmu.hradio.hradioshowcase.listener.PlayRadioServiceListener;
import lmu.hradio.hradioshowcase.model.view.RadioServiceViewModel;
import lmu.hradio.hradioshowcase.view.adapter.RadioServiceRecyclerViewAdapter;

public class FavoritesFragment extends Fragment implements Database.OnFavoritesChangeListener {

    private static final String ORIENTATION_KEY = "orientation";

    private PlayRadioServiceListener mListener;

    private RadioServiceRecyclerViewAdapter adapter;

    private int orientation;

    @BindView(R.id.favorite_list)
    RecyclerView favoritList;

    public FavoritesFragment() {
        // Required empty public constructor
    }


    public static FavoritesFragment newInstance() {
        return new FavoritesFragment();
    }

    public static FavoritesFragment newInstance(int orientation) {
        FavoritesFragment favoritesFragment = new FavoritesFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ORIENTATION_KEY, orientation);
        favoritesFragment.setArguments(bundle);
        return favoritesFragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        ButterKnife.bind(this, view);
        orientation = (getArguments() != null)? getArguments().getInt(ORIENTATION_KEY, -1): -1;
        loadFavorites(view);
        return view;
    }


    private void loadFavorites(View view) {
        Database.getInstance().readFavorites(favorite -> {
            if(getContext() == null)
                return;
            int layoutID;
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                orientation = (orientation != -1)? orientation: RecyclerView.VERTICAL;
            } else {
                orientation = (orientation != -1)? orientation: RecyclerView.HORIZONTAL;
            }

            if(orientation == RecyclerView.VERTICAL )
                layoutID = R.layout.fragment_radioservice_list_item;
            else
                layoutID = R.layout.fragment_radioservice_grid_item;


            favoritList.setLayoutManager(new LinearLayoutManager(view.getContext(), orientation, false));
             adapter = new RadioServiceRecyclerViewAdapter(favorite, mListener, layoutID);
            this.favoritList.setAdapter(adapter);
        }, view.getContext());
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof PlayRadioServiceListener) {
            mListener = (PlayRadioServiceListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        Database.getInstance().registerFavoritesChangeListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        Database.getInstance().unregisterFavoritesChangeListener(this);

    }

    @Override
    public void onAdded(RadioServiceViewModel service) {
        adapter.addContent(service, null);
    }

    @Override
    public void onRemoved(RadioServiceViewModel service) {
        adapter.remove(service);

    }
}
