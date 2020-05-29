package lmu.hradio.hradioshowcase.view.fragment.playlist;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.listener.PlayBackDelegate;
import lmu.hradio.hradioshowcase.listener.TrackLikeService;
import lmu.hradio.hradioshowcase.util.DeviceUtils;
import lmu.hradio.hradioshowcase.view.adapter.PlayListFragmentPagerAdapter;

import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;


public class PlaylistsPagerFragment extends Fragment {

    @BindView(R.id.playlist_view_pager)
    ViewPager pager;

    @BindView(R.id.tab_layout)
    TabLayout tabLayout;

    public PlaylistsPagerFragment() {
        // Required empty public constructor
    }


    public static PlaylistsPagerFragment newInstance() {
        return new PlaylistsPagerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_playlists, container, false);
        ButterKnife.bind(this, view);
        if(getActivity() instanceof PlayBackDelegate) {
            TrackLikeService service = ((PlayBackDelegate) getActivity()).getTrackLikeService();
            pager.setAdapter(new PlayListFragmentPagerAdapter(getChildFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, service, view.getContext()));
            tabLayout.setupWithViewPager(pager);
            pager.setOffscreenPageLimit(2);
            if(DeviceUtils.isTablet(getActivity()))
                tabLayout.setSelectedTabIndicator(new ColorDrawable(ContextCompat.getColor(view.getContext(),android.R.color.transparent)));
        }
        return view;
    }



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
