package lmu.hradio.hradioshowcase.view.fragment.car;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.omri.radioservice.RadioServiceType;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.hradio.timeshiftplayer.SkipItem;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.error.GeneralError;
import lmu.hradio.hradioshowcase.listener.OnEPGUpdateListener;
import lmu.hradio.hradioshowcase.listener.OnTunerScanListener;
import lmu.hradio.hradioshowcase.listener.PlayBackDelegate;
import lmu.hradio.hradioshowcase.listener.PlayBackListener;
import lmu.hradio.hradioshowcase.listener.PlayRadioServiceListener;
import lmu.hradio.hradioshowcase.listener.State;
import lmu.hradio.hradioshowcase.listener.TrackLikeService;
import lmu.hradio.hradioshowcase.manager.MainAppController;
import lmu.hradio.hradioshowcase.model.state.PlayBackState;
import lmu.hradio.hradioshowcase.model.substitiution.SubstitutionItem;
import lmu.hradio.hradioshowcase.model.view.ImageData;
import lmu.hradio.hradioshowcase.model.view.ProgrammeInformationViewModel;
import lmu.hradio.hradioshowcase.model.view.RadioServiceViewModel;
import lmu.hradio.hradioshowcase.model.view.TextData;
import lmu.hradio.hradioshowcase.util.ColorUtils;
import lmu.hradio.hradioshowcase.view.activity.MainActivity;
import lmu.hradio.hradioshowcase.view.adapter.CarPagerAdapter;
import lmu.hradio.hradioshowcase.view.component.FavoriteImageButton;
import lmu.hradio.hradioshowcase.view.fragment.player.RadioPlayerPlaybackFragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


/**
 * A simple {@link Fragment} subclass.
 */
public class CarFragment extends Fragment implements PlayBackDelegate,
        PlayBackListener, OnEPGUpdateListener,
        State.PlayBackStateHolder,
        RadioPlayerPlaybackFragment.SkipItemProvider,
        PlayRadioServiceListener {

    private static final String TAG = CarFragment.class.getSimpleName();
    private static final String CAR_SERVICE_LIST_TAG = CarRSFragment.class.getSimpleName();
    private static final String CAR_PLAYBACK_TAG = CarPlaybackFragment.class.getSimpleName();

    private MainAppController mainAppController;

    private Bitmap spotifyIcon;
    private Bitmap currentLogo;

    private BottomSheetBehavior bottomSheetBehavior;

    @BindView(R.id.car_top)
    ConstraintLayout clTop;

    @BindView(R.id.car_bottom_visible)
    FrameLayout flBottom;

    @BindView(R.id.car_station_logo)
    ImageView logoIV;

    @BindView(R.id.car_radio_mode)
    ImageView modeIV;

    @BindView(R.id.car_headline)
    TextView headlineTV;

    @BindView(R.id.car_mode_toggle)
    ImageButton carModeToggleButton;

    @BindView(R.id.car_favorites_button)
    FavoriteImageButton favoriteImageButton;

    private OnCarModeClosedListener ocmListener;

    CarPagerAdapter adapterViewPager;

    @BindView(R.id.car_viewpager)
    ViewPager viewPager;


    private PlayBackDelegate playBackDelegate;

    public CarFragment() {
        // Required empty public constructor
    }

    public static CarFragment newInstance() {
        Bundle args = new Bundle();
        CarFragment fragment = new CarFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getActivity() != null){
            getActivity().getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_car, container, false);
        ButterKnife.bind(this, view);

        RadioServiceViewModel currentService;

        if (getController().getAppState().getPlayBackState().isRunning()) {
            currentService = getController().getAppState().getPlayBackState().getRunningService();
        } else {
            currentService = getController().getLastService(getContext());
        }

        setPager(currentService);
        spotifyIcon = BitmapFactory.decodeResource(getContext().getResources(),
                R.drawable.icon_spotify_white_48dp);
        applyThemeColor();

       Fragment fragment = getChildFragmentManager().findFragmentByTag(CAR_SERVICE_LIST_TAG);
        if (fragment == null)
            fragment = CarRSFragment.newInstance(2);
        replaceFragment(R.id.car_rs_container, fragment, CAR_SERVICE_LIST_TAG);

        this.bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.car_bottom));
        this.bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED){
                    flBottom.setVisibility(GONE);
                 }
                if (newState == BottomSheetBehavior.STATE_COLLAPSED){
                    flBottom.setVisibility(VISIBLE);
                 }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // React to dragging events
            }
        });

        return view;
    }



    private void applyThemeColor() {
        @ColorInt int colorText = ColorUtils.resolveAttributeColor(R.attr.colorPrimaryText, getContext());
        carModeToggleButton.setColorFilter(colorText);
        modeIV.setColorFilter(colorText);
        favoriteImageButton.setColorFilter(colorText);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnCarModeClosedListener) {
            this.ocmListener = (OnCarModeClosedListener) context;
        }
        if (context instanceof PlayBackDelegate) {
            this.playBackDelegate = (PlayBackDelegate) context;
            playBackDelegate.registerPlayBackListener(this);
        }
    }

    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        if (fragment instanceof CarPlaybackFragment) {
            CarPlaybackFragment cpFragment = (CarPlaybackFragment) fragment;
            cpFragment.setPlayBackDelegate(this);
            cpFragment.setProvider(this);
        } else if (fragment instanceof CarRSFragment) {
            CarRSFragment crsFragment = (CarRSFragment) fragment;
            crsFragment.setPlayRadioServiceListener(this);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        playBackDelegate.unregisterPlayBackListener(this);
    }


    private MainAppController getController() {
        if (mainAppController == null)
            mainAppController = MainAppController.getInstance((MainActivity) getContext(), () -> {
            });
        return mainAppController;
    }

    @Override
    public void onRadioServiceSelected(RadioServiceViewModel radioService) {
        if(getContext() != null) {
            this.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            getController().getSearchResultState(res ->{
                int position = getServicePosition(radioService, res );
                if(position == -1)
                    position = adapterViewPager.addItem(radioService);
                viewPager.setCurrentItem(position);
            });

        }
    }

    public void startService(RadioServiceViewModel radioService) {
        if(getContext() != null) {
            updateCurrentState();
            getController().startService(radioService, (MainActivity) getContext());
        }
    }

    private void switchToPlayer() {
        setTopLineVisible(true);
        setBottomLineVisible(true);
        getChildFragmentManager().popBackStack();
    }

    public void closeRS() {
        switchToPlayer();
    }


    private Handler serviceSwitchHandler = new Handler();
    private Runnable serviceSwitchRunnable;

    private void setPager(RadioServiceViewModel lastService) {
        getController().getSearchResultState(res -> {

            if ((res != null) && (res.size() != 0)) {

                adapterViewPager = new CarPagerAdapter(getChildFragmentManager(), 0, res);
                viewPager.setAdapter(adapterViewPager);
                int position = getLastServicePosition(lastService, res);

                viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    }

                    @Override
                    public void onPageSelected(int position) {

                        RadioServiceViewModel newService = res.get(position);

                        if (serviceSwitchRunnable != null) {
                            serviceSwitchHandler.removeCallbacks(serviceSwitchRunnable);
                        }
                        serviceSwitchRunnable = () -> startService(newService);
                        serviceSwitchHandler.postDelayed(serviceSwitchRunnable, 1000);

                        // TODO: User Activity: TabSelected
                        Log.i ("Car Mode", "Service changed to: "+ newService.getServiceLabel());
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {
                    }
                });
                viewPager.setCurrentItem(position);
            }
        });

    }

    @OnClick(R.id.car_mode_toggle)
    public void carModeToggled() {
        ocmListener.closeCarMode();
    }

    @Override
    public void onPlayClicked() {
        playBackDelegate.onPlayClicked();
    }

    @Override
    public void onPauseClicked() {
        playBackDelegate.onPauseClicked();
    }

    @Override
    public void onSkipNext(Activity activity) {
        playBackDelegate.onSkipNext(activity);

    }

    @Override
    public void onSubstitutePodcast(Activity activity) {
        playBackDelegate.onSubstitutePodcast(activity);
    }

    @Override
    public void onSkipBack() {
        playBackDelegate.onSkipBack();
    }

    @Override
    public void onJumpToLive() {
        playBackDelegate.onJumpToLive();
    }

    @Override
    public void seekTo(long progress) {
        playBackDelegate.seekTo(progress);
    }

    @Override
    public void shareSbtToken() {

    }

    @Override
    public void playProgressRealtime(long realTimePosix, long streamTimePosix, long curPos, long totalDuration) {
        playBackDelegate.seekTo(curPos);
    }

    @Override
    public void sbtSeeked() {

    }

    @Override
    public void skipItemRemoved(SkipItem skipItem) {

    }

    @Override
    public void registerPlayBackListener(PlayBackListener listener) {
        playBackDelegate.registerPlayBackListener(listener);
    }

    @Override
    public void unregisterPlayBackListener(PlayBackListener listener) {
        playBackDelegate.unregisterPlayBackListener(listener);
    }

    @Override
    public TrackLikeService getTrackLikeService() {
        return playBackDelegate.getTrackLikeService();
    }

    @Override
    public PlayBackState getState() {
        return getController().getAppState().getPlayBackState();
    }

    @Override
    public void getRecommendations(OnTunerScanListener listener) {

    }

    @Override
    public List<SkipItem> getSkipContent() {
        return getController().getSkipItems();
    }

    @Override
    public void onSkipItemClicked(SkipItem item) {
        getController().skipToItem(item);
    }

    @Override
    public boolean onSkipItemLongClicked(SkipItem item) {
        //TODO share menu
        return false;
    }

    @Override
    public long getTotalDuration() {
        return getController().getTotalTimeShiftDuration();
    }


    @Override
    public SkipItem getCurrentSkipItem() {
        return getController().getCurrentSkipItem();
    }

    public void updateHeadline(String headline, Bitmap logo, RadioServiceType serviceType) {
        headlineTV.setText(headline);
        if (logo != null) {
            logoIV.setImageBitmap(logo);
        }
        if (serviceType != null) {
            modeIV.setVisibility(View.VISIBLE);
            switch (serviceType) {
                case RADIOSERVICE_TYPE_DAB:
                    modeIV.setImageResource(R.drawable.dab_badge);
                    break;
                case RADIOSERVICE_TYPE_EDI:
                    modeIV.setImageResource(R.drawable.edi_badge);
                    break;
                case RADIOSERVICE_TYPE_IP:
                    modeIV.setImageResource(R.drawable.ip_badge);
                    break;
                default:
                    modeIV.setVisibility(View.INVISIBLE);
                    break;
            }
        } else modeIV.setVisibility(View.INVISIBLE);

        Fade mFade=new Fade(Fade.IN);

        TransitionManager.beginDelayedTransition(clTop,mFade);

    }


    private void replaceFragment(int resource, Fragment fragment, String tag) {
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.replace(resource, fragment, tag);
    //    ft.addToBackStack("CAR_TAG");
        ft.commit();
    }

    private void setTopLineVisible(boolean b) {
        clTop.setVisibility(b ? VISIBLE : GONE);
    }


    private void setBottomLineVisible(boolean b) {
 //       clBottom.setVisibility(b ? VISIBLE : GONE);
    }


    private void updateCurrentState() {
        onEPGUpdate(getState().getProgrammeInformationViewModel());

        if(getState().isRunning()){
            started();
        }

        if(!getState().isRunning()) {
            paused();
        }

        textualContent(getState().getTextData());
        visualContent(getState().getCover());

        if (adapterViewPager == null || viewPager == null) return;

        CarPlaybackFragment selectedPlaybackFragment = (CarPlaybackFragment) adapterViewPager.instantiateItem(viewPager, viewPager.getCurrentItem());
        selectedPlaybackFragment.clearSkipItems();

        for(SkipItem item : getSkipContent()){
            skipItemAdded(item);
        }
        itemStarted(getCurrentSkipItem());

        if(getState().getSubstitution() != PlayBackState.SubstituionState.INACTIVE) {
            started(getController().getCurrentSubstitution());
        }


    }

    @Override
    public void onEPGUpdate(ProgrammeInformationViewModel programmeInformation) {
        if (adapterViewPager == null || viewPager == null) return;

        CarPlaybackFragment selectedPlaybackFragment = (CarPlaybackFragment) adapterViewPager.instantiateItem(viewPager, viewPager.getCurrentItem());
        selectedPlaybackFragment.onEPGUpdate(programmeInformation);
    }

    @Override
    public void started() {
        if (adapterViewPager == null || viewPager == null || getActivity() == null) return;
        final Bitmap bitmap;
        if (getState().getRunningService().getLogo() != null && getState().getRunningService().getLogo().getImageData()!=null)
            bitmap = BitmapFactory.decodeByteArray(getState().getRunningService().getLogo().getImageData(), 0, getState().getRunningService().getLogo().getImageData().length);
        else
            bitmap = null;

        getActivity().runOnUiThread(() -> {
            favoriteImageButton.setRadioService(getState().getRunningService());
            updateHeadline(getState().getRunningService().getServiceLabel(), bitmap, getState().getTunerType());
            CarPlaybackFragment selectedPlaybackFragment = (CarPlaybackFragment) adapterViewPager.instantiateItem(viewPager, viewPager.getCurrentItem());
            selectedPlaybackFragment.started();
            onEPGUpdate(getState().getProgrammeInformationViewModel());
        });
    }

    @Override
    public void stopped() {
        if (adapterViewPager == null || viewPager == null) return;

        CarPlaybackFragment selectedPlaybackFragment = (CarPlaybackFragment) adapterViewPager.instantiateItem(viewPager, viewPager.getCurrentItem());
        selectedPlaybackFragment.stopped();
    }

    @Override
    public void paused() {
        if (adapterViewPager == null || viewPager == null) return;

        CarPlaybackFragment selectedPlaybackFragment = (CarPlaybackFragment) adapterViewPager.instantiateItem(viewPager, viewPager.getCurrentItem());
        selectedPlaybackFragment.paused();
    }

    @Override
    public void playProgress(long current, long total) {
        if (adapterViewPager == null || viewPager == null) return;

        if (getState().getSubstitution() == PlayBackState.SubstituionState.INACTIVE) {
            CarPlaybackFragment selectedPlaybackFragment = (CarPlaybackFragment) adapterViewPager.instantiateItem(viewPager, viewPager.getCurrentItem());
            selectedPlaybackFragment.playProgress(current, total, getState().isRunning());
        }
    }

    @Override
    public void textualContent(TextData content) {
        if (adapterViewPager == null || viewPager == null) return;

        CarPlaybackFragment selectedPlaybackFragment = (CarPlaybackFragment) adapterViewPager.instantiateItem(viewPager, viewPager.getCurrentItem());
        selectedPlaybackFragment.textualContent(content);
    }

    @Override
    public void visualContent(ImageData visual) {
        if (adapterViewPager == null || viewPager == null) return;
        CarPlaybackFragment selectedPlaybackFragment = (CarPlaybackFragment) adapterViewPager.instantiateItem(viewPager, viewPager.getCurrentItem());
        selectedPlaybackFragment.visualContent(visual);
    }

    @Override
    public void started(SubstitutionItem substitution) {
        if (adapterViewPager == null || viewPager == null) return;

        updateHeadline("SPOTIFY", spotifyIcon, null);
        CarPlaybackFragment selectedPlaybackFragment = (CarPlaybackFragment) adapterViewPager.instantiateItem(viewPager, viewPager.getCurrentItem());
        selectedPlaybackFragment.started(substitution);
    }

    @Override
    public void stopped(SubstitutionItem substitution) {
        if (adapterViewPager == null || viewPager == null) return;

        CarPlaybackFragment selectedPlaybackFragment = (CarPlaybackFragment) adapterViewPager.instantiateItem(viewPager, viewPager.getCurrentItem());
        selectedPlaybackFragment.stopped(substitution);
        selectedPlaybackFragment.textualContent(getState().getTextData());
        selectedPlaybackFragment.visualContent(getState().getCover());


        Bitmap bitmap = BitmapFactory.decodeByteArray(getState().getRunningService().getLogo().getImageData(), 0, getState().getRunningService().getLogo().getImageData().length);
        updateHeadline(getState().getRunningService().getServiceLabel(), bitmap, getState().getTunerType());

    }

    @Override
    public void playProgress(SubstitutionItem substitution, long current, long total) {
        if (adapterViewPager == null || viewPager == null) return;

        CarPlaybackFragment selectedPlaybackFragment = (CarPlaybackFragment) adapterViewPager.instantiateItem(viewPager, viewPager.getCurrentItem());
        selectedPlaybackFragment.playProgress(substitution, current, total);
    }

    @Override
    public void skipItemAdded(SkipItem skipItem) {
        if (adapterViewPager == null || viewPager == null) return;

        CarPlaybackFragment selectedPlaybackFragment = (CarPlaybackFragment) adapterViewPager.instantiateItem(viewPager, viewPager.getCurrentItem());
        selectedPlaybackFragment.skipItemAdded(skipItem);
    }

    @Override
    public void itemStarted(SkipItem skipItem) {
        if (adapterViewPager == null || viewPager == null) return;

        CarPlaybackFragment selectedPlaybackFragment = (CarPlaybackFragment) adapterViewPager.instantiateItem(viewPager, viewPager.getCurrentItem());
        selectedPlaybackFragment.itemStarted(skipItem);
    }

    @Override
    public void onError(@NonNull GeneralError error) {

    }


    public interface OnCarModeClosedListener {
        void closeCarMode();
    }

    private int getLastServicePosition(RadioServiceViewModel lastService, List<RadioServiceViewModel> servicesList) {
        if (lastService == null)
            return 0;

        int notFound = 0;
        if ((servicesList != null) && (servicesList.size() != 0)) {
            for (int i = 0; i < servicesList.size(); i++) {
                RadioServiceViewModel s = servicesList.get(i);
                if (s.getServiceLabel().equals(lastService.getServiceLabel()))
                    return i;
            }
        }
        return notFound;
    }

    private int getServicePosition(RadioServiceViewModel service, List<RadioServiceViewModel> servicesList) {
        if (service == null)
            return -1;

        int notFound = -1;
        if ((servicesList != null) && (servicesList.size() != 0)) {
            for (int i = 0; i < servicesList.size(); i++) {
                RadioServiceViewModel s = servicesList.get(i);
                if (s.equals(service))
                    return i;
            }
        }
        return notFound;
    }


}
