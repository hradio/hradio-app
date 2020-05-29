package lmu.hradio.hradioshowcase.view.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.listener.TrackLikeService;
import lmu.hradio.hradioshowcase.util.DeviceUtils;
import lmu.hradio.hradioshowcase.view.fragment.playlist.PlaylistFragment;

public class PlayListFragmentPagerAdapter extends FragmentStatePagerAdapter {

    private Fragment[] fragments;

    private String blackListName;
    private String likeListName;

    private final int simultaneousPages;

    public PlayListFragmentPagerAdapter(@NonNull FragmentManager fm, int behavior, TrackLikeService trackLikeService, Context context) {
        super(fm, behavior);
        fragments = new Fragment[2];
        trackLikeService.getLikedTracks(playList -> fragments[0] = PlaylistFragment.newInstance(playList.getId()));
        trackLikeService.getDislikedTracks(playList -> fragments[1] = PlaylistFragment.newInstance(playList.getId()));
        blackListName = context.getString(R.string.dislike_list_name);
        likeListName = context.getString(R.string.like_list_name);
        simultaneousPages = DeviceUtils.isTablet(context) ? 2 : 1;
    }

    @Override public float getPageWidth(int position) {
        return(1f/simultaneousPages);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }

    @Override
    public int getCount() {
        return fragments.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0: return likeListName;
            case 1: return blackListName;
            default: return null;
        }
    }
}
