package lmu.hradio.hradioshowcase.view.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import org.omri.radioservice.RadioService;
import org.omri.radioservice.RadioServiceType;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.database.Database;
import lmu.hradio.hradioshowcase.model.view.RadioServiceViewModel;
import lmu.hradio.hradioshowcase.view.fragment.dialog.SearchDialogFragment;
import lmu.hradio.hradioshowcase.view.fragment.search.SearchFragment;


public class ContentFragment extends Fragment implements Database.OnFavoritesChangeListener {

    private static final String SEARCH_TAG = SearchFragment.class.getSimpleName();
    private static final String SERVICES_TAG = RadioServiceListFragment.class.getSimpleName();
    private static final String FAVORITES_TAG = FavoritesFragment.class.getSimpleName();
    public ContentFragment() {
        // Required empty public constructor
    }

    public static ContentFragment newInstance() {
        ContentFragment fragment = new ContentFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public static ContentFragment newInstance(boolean openSearch) {
        ContentFragment fragment = new ContentFragment();
        Bundle args = new Bundle();
        args.putBoolean(SEARCH_TAG, openSearch);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Database.getInstance().registerFavoritesChangeListener(this);


    }

    @Override
    public void onDetach() {
        super.onDetach();
        Database.getInstance().unregisterFavoritesChangeListener(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_content, container, false);
        ButterKnife.bind(this, view);
        FragmentManager fm = getChildFragmentManager();
        int favoritesScroll = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? RecyclerView.HORIZONTAL : RecyclerView.VERTICAL;
        Fragment favoritesFragment = FavoritesFragment.newInstance(favoritesScroll);
        Fragment servicesFragment = RadioServiceListFragment.newInstance(2, RecyclerView.VERTICAL);

        replaceFragment(fm, favoritesFragment, R.id.favorites_container, FAVORITES_TAG);
        replaceFragment(fm, servicesFragment, R.id.services_list_container, SERVICES_TAG);

        if(getArguments() != null && getArguments().getBoolean(SEARCH_TAG, false)){
            onOpenSearchViewClicked();
        }

        return view;
    }

    @OnClick(R.id.search_container)
    void onOpenSearchViewClicked() {
        SearchDialogFragment dialogFragment = SearchDialogFragment.newInstance();
        dialogFragment.show(getChildFragmentManager(), SEARCH_TAG);
    }


    private void replaceFragment(FragmentManager fm, Fragment fragment, int resource, String tag) {
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(resource, fragment, tag);
        ft.commit();
    }


    @Override
    public void onAdded(RadioServiceViewModel service) {
    }

    @Override
    public void onRemoved(RadioServiceViewModel service) {

    }

    public void onServiceUpdate(List<RadioServiceViewModel> radioServiceViewModels) {
        Fragment fragment = getChildFragmentManager().findFragmentByTag(SERVICES_TAG);
        if (fragment instanceof RadioServiceListFragment && getActivity() != null) {
            getActivity().runOnUiThread( () -> ((RadioServiceListFragment) fragment).onServiceUpdate(radioServiceViewModels));
        }
    }

    public void onServiceFound(RadioServiceViewModel service, RadioService type) {
        Fragment fragment = getChildFragmentManager().findFragmentByTag(SERVICES_TAG);
        if (fragment instanceof RadioServiceListFragment && getActivity() != null) {
            getActivity().runOnUiThread( () -> ((RadioServiceListFragment) fragment).onServiceFound(service, type));
        }
    }
}
