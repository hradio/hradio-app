package lmu.hradio.hradioshowcase.view.fragment;


import android.graphics.drawable.Animatable;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.util.ColorUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class LauncherFragment extends Fragment {
    @BindView(R.id.loading_image_view)
    ImageView loadingImageView;

    public LauncherFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_launcher, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        @ColorInt int color = ColorUtils.resolveAttributeColor(R.attr.colorAccent, getContext());
        DrawableCompat.setTint(loadingImageView.getDrawable(), color);
        Animatable animatable = (Animatable)loadingImageView.getDrawable();
        animatable.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        Animatable animatable = (Animatable)loadingImageView.getDrawable();
        animatable.stop();
    }
}
