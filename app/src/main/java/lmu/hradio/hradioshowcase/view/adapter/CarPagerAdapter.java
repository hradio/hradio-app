package lmu.hradio.hradioshowcase.view.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;

import lmu.hradio.hradioshowcase.model.view.RadioServiceViewModel;
import lmu.hradio.hradioshowcase.view.fragment.car.CarPlaybackFragment;

public class CarPagerAdapter extends FragmentStatePagerAdapter {

    private List<RadioServiceViewModel> mServices;

    public CarPagerAdapter(FragmentManager fragmentManager, int behavior, List<RadioServiceViewModel> services) {
        super(fragmentManager, behavior);
        mServices=services;
      }

    // Returns total number of pages
    @Override
    public int getCount() {
        return mServices.size();
    }

    // Returns the fragment to display for that page
    @NonNull
    @Override
    public CarPlaybackFragment getItem(int position) {
        return createPosition(position);
    }

    private CarPlaybackFragment createPosition(int position) {
        RadioServiceViewModel service = mServices.get(position);
        return CarPlaybackFragment.newInstance(service);
    }

    public int addItem(RadioServiceViewModel radioService) {
        mServices.add(radioService);
        this.notifyDataSetChanged();
        return mServices.size()-1;
    }
}
