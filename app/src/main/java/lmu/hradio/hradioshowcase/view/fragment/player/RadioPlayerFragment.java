package lmu.hradio.hradioshowcase.view.fragment.player;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import org.omri.radioservice.RadioService;
import org.omri.radioservice.RadioServiceDabEdi;
import org.omri.radioservice.RadioServiceType;
import org.omri.tuner.ReceptionQuality;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.hradio.httprequestwrapper.util.TimeUtils;
import eu.hradio.timeshiftplayer.SkipItem;
import lmu.hradio.hradioshowcase.BuildConfig;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.broadcast.SignalStrengthBroadCast;
import lmu.hradio.hradioshowcase.database.Database;
import lmu.hradio.hradioshowcase.error.GeneralError;
import lmu.hradio.hradioshowcase.listener.OnEPGUpdateListener;
import lmu.hradio.hradioshowcase.listener.OnRadioServiceUpdateListener;
import lmu.hradio.hradioshowcase.listener.OnServiceUpdateTunerScanListener;
import lmu.hradio.hradioshowcase.listener.PlayBackListener;
import lmu.hradio.hradioshowcase.listener.PlayRadioServiceListener;
import lmu.hradio.hradioshowcase.listener.State;
import lmu.hradio.hradioshowcase.model.state.PlayBackState;
import lmu.hradio.hradioshowcase.model.substitiution.SubstitutionItem;
import lmu.hradio.hradioshowcase.model.view.ImageData;
import lmu.hradio.hradioshowcase.model.view.LocationViewModel;
import lmu.hradio.hradioshowcase.model.view.ProgrammeInformationViewModel;
import lmu.hradio.hradioshowcase.model.view.ProgrammeViewModel;
import lmu.hradio.hradioshowcase.model.view.RadioServiceViewModel;
import lmu.hradio.hradioshowcase.model.view.ScheduleViewModel;
import lmu.hradio.hradioshowcase.model.view.TextData;
import lmu.hradio.hradioshowcase.model.view.TimeViewModel;
import lmu.hradio.hradioshowcase.model.view.UserActionEnum;
import lmu.hradio.hradioshowcase.util.ColorUtils;
import lmu.hradio.hradioshowcase.util.DeviceUtils;
import lmu.hradio.hradioshowcase.util.SharedPreferencesHelper;
import lmu.hradio.hradioshowcase.view.activity.MainActivity;
import lmu.hradio.hradioshowcase.view.adapter.EPGProgrammListAdapter;
import lmu.hradio.hradioshowcase.view.adapter.RadioServiceRecyclerViewAdapter;
import lmu.hradio.hradioshowcase.view.component.FavoriteImageButton;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link RadioPlayerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RadioPlayerFragment extends Fragment implements OnRadioServiceUpdateListener, PlayBackListener, OnEPGUpdateListener, SignalStrengthBroadCast.SignalStrengthListener, PlayBackState.OnChangeServiceListener {

    private static final String TAG = RadioPlayerFragment.class.getSimpleName();
    private static final String SELECTED_TAG = "selected";

    private OnMinimizeClickListener minimizeListener;
    private PlayRadioServiceListener radioServiceListListener;
    private State.PlayBackStateHolder playbackStateHolder;

    private int selectedTab = 0;

    private List<RadioServiceViewModel> serviceList;
    private ProgrammeInformationViewModel currentEPG;

    private Animation zoomin, zoomout;

    @BindView(R.id.programme_name_text_view)
    TextView currentProgrammeTextView;

    @BindView(R.id.programme_schedule_text_view)
    TextView currentProgrammeScheduleTextView;

    @BindView(R.id.substitution_logo)
    ImageView substitutionLogo;

    @BindView(R.id.minimize_button)
    ImageView minimizeButton;

    @BindView(R.id.search_epg_text_view)
    TextView searchEPGTextView;

    @BindView(R.id.services_tabs)
    TabLayout servicesTabLayout;

    @BindView(R.id.program_list)
    RecyclerView programList;

    @BindView(R.id.services_list)
    RecyclerView radioServiceRecyclerView;

    @BindView(R.id.tuner_type_image_view)
    ImageView tunertypeImageView;

    @BindView(R.id.signal_strength_image_view)
    ImageView signalStrengthImageView;

    @BindView(R.id.favorite_button)
    FavoriteImageButton favoriteImageButton;

    @BindView(R.id.epg_badge_image_view)
    ImageView epgBagdeImageView;

    @BindView(R.id.recommendations_container)
    LinearLayout recommendationsContainer;

    private RadioServiceRecyclerViewAdapter serviceListAdapter;

    private OnLockBottomSheetListener lockBottomSheetListener;

    private SignalStrengthBroadCast.Receiver receiver;

    private OnLoadProgrammePodcastListener podcastListener;

    public RadioPlayerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RadioPlayerFragment.
     */
    public static RadioPlayerFragment newInstance() {
        return new RadioPlayerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_radio_player, container, false);
        ButterKnife.bind(this, view);
        currentProgrammeTextView.setSelected(true);
        radioServiceRecyclerView.setNestedScrollingEnabled(true);
        programList.setNestedScrollingEnabled(true);

        if (programList == null || programList.getAdapter() == null || programList.getAdapter().getItemCount() == 0) {
            searchEPGTextView.setVisibility(View.VISIBLE);
        } else {
            searchEPGTextView.setVisibility(View.INVISIBLE);

        }
        if(savedInstanceState != null) {
            selectedTab = savedInstanceState.getInt(SELECTED_TAG, 0);
        }

        servicesTabLayout.addTab(servicesTabLayout.newTab().setText(R.string.recommendations_title));
        servicesTabLayout.addTab(servicesTabLayout.newTab().setText(R.string.title_favorites));
        servicesTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener(){

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTab = servicesTabLayout.getSelectedTabPosition();
                updateServiceList();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        closeDrawerIfOpen();
        setImageTints();

        zoomin = AnimationUtils.loadAnimation(getContext(), R.anim.zoomin);
        zoomout = AnimationUtils.loadAnimation(getContext(), R.anim.zoomout);

        return view;
    }

    private void setImageTints() {
        if(getContext() != null) {
            int color = ColorUtils.resolveAttributeColor(R.attr.colorPrimaryText,  getContext());
            signalStrengthImageView.setColorFilter(color);
            tunertypeImageView.setColorFilter(color);
            minimizeButton.setColorFilter(color);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_TAG , selectedTab);
    }

    private void setTunerTypeBadge(RadioServiceType tunerTypeBadge) {
        if(tunerTypeBadge == null){
            tunertypeImageView.setVisibility(View.INVISIBLE);
            return;
        }

        TooltipCompat.setTooltipText(tunertypeImageView, String.format(getResources().getString(R.string.tooltip_tuner_type_image), tunerTypeToString(tunerTypeBadge)));
        tunertypeImageView.setVisibility(View.VISIBLE);
        switch (tunerTypeBadge){
            case RADIOSERVICE_TYPE_FM: tunertypeImageView.setImageResource(R.drawable.fm_badge); break;
            case RADIOSERVICE_TYPE_SIRIUS: tunertypeImageView.setImageResource(R.drawable.fm_badge); break;
            case RADIOSERVICE_TYPE_DAB: tunertypeImageView.setImageResource(R.drawable.dab_badge); break;
            case RADIOSERVICE_TYPE_EDI: {
                PlayBackState state = playbackStateHolder.getState();
                for(RadioService srv : state.getRunningService().getRadioServices()) {
                    if(srv.getRadioServiceType() == RadioServiceType.RADIOSERVICE_TYPE_EDI) {
                        if(((RadioServiceDabEdi)srv).sbtEnabled()) {
                            //TODO SBT badge
                            tunertypeImageView.setImageResource(R.drawable.edi_badge);
                        } else {
                            tunertypeImageView.setImageResource(R.drawable.edi_badge);
                        }

                        break;
                    }
                }
                break;
            }
            case RADIOSERVICE_TYPE_HDRADIO: tunertypeImageView.setImageResource(R.drawable.ip_badge); break;
            case RADIOSERVICE_TYPE_IP: tunertypeImageView.setImageResource(R.drawable.ip_badge); break;
            default: tunertypeImageView.setVisibility(View.INVISIBLE); break;
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        unregisterListeners();

    }

    @Override
    public void onResume() {
        super.onResume();
        registerListeners();
    }

    public void unregisterListeners() {
        if(minimizeListener != null) {
            minimizeListener = null;
            radioServiceListListener = null;
            playbackStateHolder.getState().unregisterOnChangeServiceListener(this);

            if (getContext() != null) {
                ((MainActivity) getContext()).getPlayBackDelegate().unregisterPlayBackListener(this);
                ((MainActivity) getContext()).unregisterOnMetaDataUpdateListener(this);
                this.receiver.unregister(getContext());
                this.receiver = null;
            }
        }
    }

    public void registerListeners() {
        Context context = getContext();
        if (context instanceof MainActivity) {
            minimizeListener = (OnMinimizeClickListener) context;
            radioServiceListListener = (PlayRadioServiceListener) context;
            playbackStateHolder = (State.PlayBackStateHolder) context;
            lockBottomSheetListener = (OnLockBottomSheetListener) context;
            podcastListener = (OnLoadProgrammePodcastListener) context;
            playbackStateHolder.getState().registerOnChangeServiceListener(this);
            ((MainActivity) context).registerOnMetaDataUpdateListener(this);
            ((MainActivity) context).getPlayBackDelegate().registerPlayBackListener(this);
            this.receiver = new SignalStrengthBroadCast.Receiver(this, context);
            onChangeService();

        } else
            throw new RuntimeException("Context must implement PlayRadioServiceListener");
    }

    public void closeDrawerIfOpen() {
        View view = getView();
        if (view != null) {
            DrawerLayout mDrawerLayout = view.findViewById(R.id.epg_program_drawer);
            if (mDrawerLayout != null && ( mDrawerLayout.isDrawerOpen(GravityCompat.START) || mDrawerLayout.isDrawerOpen(GravityCompat.END))){
                mDrawerLayout.closeDrawers();
            }
        }
    }

    private void initDrawerIfExists() {
        closeDrawerIfOpen();
        View view = getView();
        if (view != null) {
            ImageButton openEPG = view.findViewById(R.id.openProgrammeButton);
            ImageButton openRecommendations = view.findViewById(R.id.openRecommendationsButton);

            int color = ColorUtils.resolveAttributeColor(R.attr.colorPrimaryText, view.getContext());
            int colorDisabled = ColorUtils.resolveAttributeColor(R.attr.colorDisabled, view.getContext());

            if(openEPG != null ) {
                TooltipCompat.setTooltipText(openEPG, getString(R.string.tooltip_open_epg));
                openEPG.setOnClickListener(v -> onOpenProgrammeClicked());
                openEPG.setColorFilter( currentEPG != null ? color : colorDisabled);
            } if(openRecommendations != null){
                TooltipCompat.setTooltipText(openRecommendations, getString(R.string.tooltip_open_recommendation));
                openRecommendations.setOnClickListener(v -> onOpenRecommendationClicked());
                openRecommendations.setColorFilter(color);

            }
            DrawerLayout mDrawerLayout = view.findViewById(R.id.epg_program_drawer);
            if (mDrawerLayout != null){
                    mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
                        @Override
                        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {}

                        @Override
                        public void onDrawerOpened(@NonNull View drawerView) {
                            lockBottomSheetListener.lockBottomSheet(true);
                        }

                        @Override
                        public void onDrawerClosed(@NonNull View drawerView) {
                            lockBottomSheetListener.lockBottomSheet(false);

                        }

                        @Override
                        public void onDrawerStateChanged(int newState) {
                        }
                    });
            }
        }
    }

    private void onOpenProgrammeClicked(){
        if(currentEPG != null) {
            View view = getView();
            if (view != null) {
                DrawerLayout mDrawerLayout = view.findViewById(R.id.epg_program_drawer);
                if (mDrawerLayout != null && (mDrawerLayout.isDrawerOpen(GravityCompat.START) || mDrawerLayout.isDrawerOpen(GravityCompat.END))) {
                    mDrawerLayout.closeDrawers();
                    return;
                }
                if (mDrawerLayout != null)
                    mDrawerLayout.openDrawer(GravityCompat.END);

            }
        }
    }

    private void onOpenRecommendationClicked(){
        if(serviceList != null && !serviceList.isEmpty()) {
            View view = getView();
            if (view != null) {
                DrawerLayout mDrawerLayout = view.findViewById(R.id.epg_program_drawer);
                if (mDrawerLayout != null && (mDrawerLayout.isDrawerOpen(GravityCompat.START) || mDrawerLayout.isDrawerOpen(GravityCompat.END))) {
                    mDrawerLayout.closeDrawers();
                } else if (mDrawerLayout != null && view.findViewById(R.id.nav_view_service) != null) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                } else{
                    int newVisibility = recommendationsContainer.getVisibility() == View.VISIBLE ? GONE : VISIBLE;
                    recommendationsContainer.setVisibility(newVisibility);
                    if(newVisibility == VISIBLE)
                        scheduleRecommendationHideTimer();
                }

            }
        } else {
            if(serviceList == null) {
                if(getActivity() != null) {
                    Snackbar.make(getActivity().findViewById(R.id.container), R.string.recommendations_empty_err, Snackbar.LENGTH_LONG).show();
                }
            }
        }
    }


    @OnClick(R.id.minimize_button)
    void onMinimizeClicked() {
        minimizeListener.onMinimizeClicked();
    }



    @Override
    public void onEPGUpdate(ProgrammeInformationViewModel programmeInformation) {
        currentEPG = programmeInformation;
        if (getActivity() != null)
            //get todays schedule
            getActivity().runOnUiThread(this::loadEPG);
    }

    @Override
    public void sbtSeeked() {
        //loadEPG();
    }

    private void setupTooltips() {
        TooltipCompat.setTooltipText(substitutionLogo, getResources().getString(R.string.tooltip_substitution_image));
        TooltipCompat.setTooltipText(minimizeButton, getResources().getString(R.string.tooltip_minimize_image));
        TooltipCompat.setTooltipText(epgBagdeImageView, getResources().getString(R.string.tooltip_epg_image));
    }

    private static String tunerTypeToString(RadioServiceType type){
        if(type ==null)
            return "IP";
        switch (type){
            case RADIOSERVICE_TYPE_FM: return "FM";
            case RADIOSERVICE_TYPE_EDI: return "EDI";
            case RADIOSERVICE_TYPE_DAB: return "DAB";
            default: return "IP";
        }
    }

    private void loadEPG() {
        if(getActivity() != null && getView() != null) {
            View view = getView();
            ImageButton openEPG = view.findViewById(R.id.openProgrammeButton);

            if (currentEPG == null || currentEPG.getCurrentRunningProgramme() == null) {
                searchEPGTextView.setVisibility(View.VISIBLE);
                programList.setAdapter(null);
                currentProgrammeTextView.setEnabled(false);
                currentProgrammeTextView.setText("");
                currentProgrammeScheduleTextView.setText("");
                currentProgrammeScheduleTextView.setOnClickListener(null);
                currentProgrammeTextView.setOnClickListener(null);
                epgBagdeImageView.setVisibility(GONE);
                currentProgrammeScheduleTextView.setVisibility(GONE);


                if (openEPG != null) {
                    int colorDisabled = ColorUtils.resolveAttributeColor(R.attr.colorDisabled, view.getContext());
                    openEPG.setColorFilter(colorDisabled);
                }
                return;
            }
            int color = ColorUtils.resolveAttributeColor(R.attr.colorPrimaryText, view.getContext());
            if (openEPG != null) {
                openEPG.setColorFilter(color);
            }
            epgBagdeImageView.setColorFilter(color);

            currentProgrammeScheduleTextView.setOnClickListener(v -> onOpenProgrammeClicked());
            currentProgrammeTextView.setOnClickListener(v -> onOpenProgrammeClicked());

            searchEPGTextView.setVisibility(View.INVISIBLE);
            epgBagdeImageView.setVisibility(View.VISIBLE);
            currentProgrammeScheduleTextView.setVisibility(View.VISIBLE);

            ProgrammeViewModel programme = currentEPG.getCurrentRunningProgramme();

            String nameString = programme.getQualifiedName();
            for (LocationViewModel location : programme.getLocations()) {
                for (TimeViewModel t : location.getTimes()) {
                    String startTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(t.getStartTime());
                    String endTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(t.getEndTime());

                    String timeBuilder = String.format(getString(R.string.programme_name_label), startTime, endTime);
                    currentProgrammeTextView.setEnabled(true);
                    currentProgrammeTextView.setText(nameString);
                    currentProgrammeScheduleTextView.setText(timeBuilder);
                }
            }
            if (currentEPG.getSchedules().size() == 1) {
                initProgrameList(currentEPG.getSchedules().get(0));

            } else {
                for (ScheduleViewModel schedule : currentEPG.getSchedules()) {
                    if (DateUtils.isToday(schedule.getScope().getStartTime().getTime())) {
                        initProgrameList(schedule);
                        break;
                    }
                }
            }
        }
    }


    private void initProgrameList(ScheduleViewModel schedule) {
        final EPGProgrammListAdapter adapter = new EPGProgrammListAdapter(schedule.getProgrammes(), podcasts ->{
            closeDrawerIfOpen();
            podcastListener.loadPodcasts(podcasts);
        });
        programList.setAdapter(adapter);

        RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(programList.getContext()) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };
        smoothScroller.setTargetPosition(adapter.getIndexOfCurrent());
        Objects.requireNonNull(programList.getLayoutManager()).startSmoothScroll(smoothScroller);
        searchEPGTextView.setVisibility(View.INVISIBLE);
    }


    public void serviceUpdate(List<RadioServiceViewModel> content) {
        if (content != null && !content.isEmpty()) {
            serviceList = new ArrayList<>(content);
            if (this.getActivity() != null)
                this.getActivity().runOnUiThread(() -> {
                    if (serviceListAdapter == null || radioServiceRecyclerView == null || radioServiceRecyclerView.getAdapter() == null) {
                        if (radioServiceRecyclerView != null) {
                            LinearLayoutManager layoutManager = (LinearLayoutManager) radioServiceRecyclerView.getLayoutManager();
                            int layoutResource = R.layout.fragment_radioservice_list_item;
                            if (layoutManager != null && layoutManager.getOrientation() == RecyclerView.HORIZONTAL) {
                                layoutResource = R.layout.fragment_radioservice_grid_item;
                                radioServiceRecyclerView.setLayoutManager(new GridLayoutManager(radioServiceRecyclerView.getContext(), 1, RecyclerView.HORIZONTAL, false));
                            }
                            serviceListAdapter = new RadioServiceRecyclerViewAdapter(serviceList, radioServiceListListener, layoutResource);
                            radioServiceRecyclerView.setAdapter(serviceListAdapter);
                        }
                    } else {
                        serviceListAdapter.addContent(serviceList);
                    }
                });
        }else{
            if(serviceListAdapter != null)
                serviceListAdapter.addContent(new ArrayList<>());

        }
    }

    @Override
    public void onServiceUpdate(List<RadioServiceViewModel> newServices) {
        serviceUpdate(newServices);
    }


    @Override
    public void started() {
        PlayBackState state = playbackStateHolder.getState();

        if (state != null && getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                substitutionLogo.setVisibility(View.INVISIBLE);
                tunertypeImageView.setVisibility(View.VISIBLE);
                setTunerTypeBadge(state.getTunerType());
                favoriteImageButton.setRadioService(playbackStateHolder.getState().getRunningService());
                currentProgrammeTextView.setText(state.getRunningService().getServiceLabel());
                onChangeService();
                onEPGUpdate(state.getProgrammeInformationViewModel());

                if(state.getRunningService().getLogo() != null) {
                    byte[] data = state.getRunningService().getLogo().getImageData();
                    if(data != null) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        createGradientBackground(bitmap);
                    }else{
                        state.getRunningService().setListener(() ->{
                            byte[] img = state.getRunningService().getLogo().getImageData();
                            Bitmap bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
                            createGradientBackground(bitmap);
                        });
                    }
                }

            });
        }
    }

    @Override
    public void stopped() {
        PlayBackState state = playbackStateHolder.getState();
        if (state != null && getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                favoriteImageButton.setRadioService(playbackStateHolder.getState().getRunningService());
                currentProgrammeTextView.setText(state.getRunningService().getServiceLabel());
                serviceUpdate(state.getRelateRecommendedServices());
                onEPGUpdate(state.getProgrammeInformationViewModel());
            });
        }
    }

    @Override
    public void paused() { }

    @Override
    public void playProgress(long current, long total) {
        //if(BuildConfig.DEBUG)Log.d(TAG, "Progress: " + current + " : " + total);
    }

    @Override
    public void playProgressRealtime(long realTimePosix, long streamTimePosix, long curPos, long totalDuration) {

    }


    @Override
    public void skipItemRemoved(SkipItem skipItem) {

    }

    @Override
    public void started(SubstitutionItem substitution) {
        if(getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                resolveSubstituionLogo(playbackStateHolder.getState().getSubstitution());
           if (substitution.getCover()!=null) createGradientBackground(substitution.getCover().decode());
            });
        }
    }

    private void resolveSubstituionLogo(PlayBackState.SubstituionState substituionState){

        switch (substituionState){
            case INACTIVE:
                if (substitutionLogo.getAnimation()!=null) substitutionLogo.getAnimation().cancel();
                substitutionLogo.clearAnimation();
                zoomin.setAnimationListener(null);
                zoomout.setAnimationListener(null);

                substitutionLogo.setVisibility(View.INVISIBLE);
                tunertypeImageView.setVisibility(View.VISIBLE);
                break ;
            case NATIVE:
                if (substitutionLogo.getAnimation()!=null) substitutionLogo.getAnimation().cancel();
                substitutionLogo.clearAnimation();
                zoomin.setAnimationListener(null);
                zoomout.setAnimationListener(null);

                substitutionLogo.setVisibility(View.VISIBLE);
                tunertypeImageView.setVisibility(View.INVISIBLE);
                ImageData logo = playbackStateHolder.getState().getRunningService().getLogo();
                if( logo!= null && logo.getImageData() != null ){
                    Bitmap bitmap = BitmapFactory.decodeByteArray(logo.getImageData(), 0 , logo.getImageData().length);
                    substitutionLogo.setImageBitmap(bitmap);
                } else {
                    substitutionLogo.setImageResource(R.drawable.outline_radio_white_48dp);
                }
                break;
            case SPOTIFY:
                substitutionLogo.setVisibility(View.VISIBLE);
                tunertypeImageView.setVisibility(View.INVISIBLE);

                Drawable drawable = ContextCompat.getDrawable(substitutionLogo.getContext(), R.drawable.spotify_icon_rgb_white);

                int color = ColorUtils.resolveAttributeColor(R.attr.colorPrimaryText, substitutionLogo.getContext());

                assert drawable != null;
                DrawableCompat.setTint(drawable,color);
                substitutionLogo.setImageResource(R.drawable.spotify_icon_rgb_white);

                substitutionLogo.setAnimation(zoomin);
                substitutionLogo.setAnimation(zoomout);

                zoomin.setAnimationListener(new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation arg0) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation arg0) {
                    }

                    @Override
                    public void onAnimationEnd(Animation arg0) {
                        substitutionLogo.startAnimation(zoomout);
                    }
                });

                zoomout.setAnimationListener(new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation arg0) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation arg0) {
                    }

                    @Override
                    public void onAnimationEnd(Animation arg0) {
                        substitutionLogo.startAnimation(zoomin);
                    }
                });

                substitutionLogo.startAnimation(zoomin);

        }
    }

    @Override
    public void stopped(SubstitutionItem substitution) {
        if(getActivity() != null)
        getActivity().runOnUiThread(() -> {
            substitutionLogo.setVisibility(View.INVISIBLE);
            tunertypeImageView.setVisibility(View.VISIBLE);
        });


    }

    @Override
    public void textualContent(TextData content) {}

    @Override
    public void visualContent(ImageData visual) {
    }

    private void createGradientBackground(Bitmap bitmap){
        if(bitmap != null) {
            Palette.from(bitmap).generate(palette -> {
                if (palette == null || getActivity() == null) return;
                int color = ColorUtils.resolveAttributeColor(R.attr.colorSecondary, getActivity());

                int dominantColor = palette.getDominantColor(color);
                dominantColor = ColorUtils.adjustAlpha(dominantColor, 0.7f);
                View layout = getView();

                if (layout != null) {
                    int mainColor = ColorUtils.resolveAttributeColor(R.attr.colorPrimary, getActivity());
                    GradientDrawable gd = new GradientDrawable(
                            GradientDrawable.Orientation.TOP_BOTTOM,
                            new int[]{dominantColor, mainColor});
                    gd.setCornerRadius(0f);
                    layout.setBackground(gd);
                }
            });
        }
    }


    @Override
    public void playProgress(SubstitutionItem substitution, long current, long total) {

    }

    @Override
    public void skipItemAdded(SkipItem skipItem) {

    }

    @Override
    public void itemStarted(SkipItem skipItem) {

    }

    @Override
    public void onSignalStrengthChanged(ReceptionQuality newQuality) {
        if (getActivity() != null)
            getActivity().runOnUiThread(() -> {

                if(newQuality == null)
                    signalStrengthImageView.setImageResource(R.drawable.outline_signal_cellular_off_white_36);
                else
                switch (newQuality){
                    case NO_SIGNAL: signalStrengthImageView.setImageResource(R.drawable.outline_signal_cellular_off_white_36); break;
                    case BAD: signalStrengthImageView.setImageResource(R.drawable.outline_signal_cellular_0_bar_white_36); break;
                    case POOR: signalStrengthImageView.setImageResource(R.drawable.outline_signal_cellular_1_bar_white_36); break;
                    case OKAY: signalStrengthImageView.setImageResource(R.drawable.outline_signal_cellular_2_bar_white_36); break;
                    case GOOD: signalStrengthImageView.setImageResource(R.drawable.outline_signal_cellular_3_bar_white_36); break;
                    case BEST: signalStrengthImageView.setImageResource(R.drawable.outline_signal_cellular_4_bar_white_36); break;
                }
            });
    }

    @Override
    public void onChangeService() {

        PlayBackState state = playbackStateHolder.getState();
        if (state != null) {
            selectedTab = state.getRelateRecommendedServices() != null && !state.getRelateRecommendedServices().isEmpty()? selectedTab : 1;
            servicesTabLayout.getTabAt(selectedTab).select();

            if(selectedTab == 1 && getContext() != null && DeviceUtils.isTablet(getContext())){
                recommendationsContainer.setVisibility(GONE);
                playbackStateHolder.getRecommendations(new OnServiceUpdateTunerScanListener() {
                    @Override
                    public void onResult(List<RadioServiceViewModel> services) {
                        if(services == null || services.isEmpty()) return;
                        selectedTab = 0;
                        scheduleRecommendationHideTimer();
                        if(getActivity() != null){
                            getActivity().runOnUiThread(() ->{
                                updateServiceList();
                                servicesTabLayout.getTabAt(selectedTab).select();
                                recommendationsContainer.setVisibility(VISIBLE);});
                        }
                    }

                    @Override
                    public void onServiceFound(RadioServiceViewModel service, RadioService type) { }
                });

            } else if(getContext() != null && DeviceUtils.isTablet(getContext())){
                scheduleRecommendationHideTimer();
            }

            if(state.getRunningService() != null) {
                favoriteImageButton.setRadioService(state.getRunningService());
                currentProgrammeTextView.setText(state.getRunningService().getServiceLabel());
                onEPGUpdate(state.getProgrammeInformationViewModel());

                setTunerTypeBadge(state.getTunerType());

                if(state.getRunningService().getLogo() != null) {
                    byte[] data = state.getRunningService().getLogo().getImageData();
                    if(data != null) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        createGradientBackground(bitmap);
                    }else{
                        state.getRunningService().setListener(() ->{
                            byte[] img = state.getRunningService().getLogo().getImageData();
                            Bitmap bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
                            createGradientBackground(bitmap);
                        });
                    }
                }
                resolveSubstituionLogo(state.getSubstitution());
                setupTooltips();
                visualContent(state.getCover());

               updateServiceList();

            }

        }
        initDrawerIfExists();
    }

    private Handler recommendationHideHandler = new Handler();
    private Runnable recommendationHideRunnable;

    private void scheduleRecommendationHideTimer() {
        if(getContext() != null && DeviceUtils.isTablet(getContext())){
            if(recommendationHideRunnable != null)
                recommendationHideHandler.removeCallbacks(recommendationHideRunnable);
            recommendationHideRunnable = () -> recommendationsContainer.setVisibility(GONE);
            recommendationHideHandler.postDelayed(recommendationHideRunnable, 5000);
            servicesTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    scheduleRecommendationHideTimer();
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                    scheduleRecommendationHideTimer();
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                    scheduleRecommendationHideTimer();
                }
            });
            radioServiceRecyclerView.setOnTouchListener( (v, event) -> {
                scheduleRecommendationHideTimer();
                return v.performClick();
            });
        }
    }


    private void updateServiceList(){
        PlayBackState state = playbackStateHolder.getState();
        serviceUpdate(new ArrayList<>());
        if (state != null)
        if (selectedTab == 0) {
            if(state.getRelateRecommendedServices() != null && !state.getRelateRecommendedServices().isEmpty())

                serviceUpdate(state.getRelateRecommendedServices());
            else
                playbackStateHolder.getRecommendations(new OnServiceUpdateTunerScanListener() {
                    @Override
                    public void onResult(List<RadioServiceViewModel> services) {
                        serviceUpdate(services);
                    }

                    @Override
                    public void onServiceFound(RadioServiceViewModel service, RadioService type) { }
                });
        } else {

            Database.getInstance().readFavorites(this::serviceUpdate, this.getContext());
        }
    }

    @Override
    public void onError(@NonNull GeneralError error) {

    }


    @FunctionalInterface
    public interface OnMinimizeClickListener {
        void onMinimizeClicked();
    }

    @FunctionalInterface
    public interface OnLockBottomSheetListener {
        void lockBottomSheet(boolean lock);
    }

    @FunctionalInterface
    public interface OnLoadProgrammePodcastListener {
        void loadPodcasts(List<String> urls);
    }
}
