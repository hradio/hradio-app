package lmu.hradio.hradioshowcase.view.fragment.car;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.hradio.httprequestwrapper.util.TimeUtils;
import eu.hradio.timeshiftplayer.SkipItem;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.listener.PlayBackDelegate;
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
import lmu.hradio.hradioshowcase.util.DateUtils;
import lmu.hradio.hradioshowcase.view.adapter.EPGProgrammListAdapter;
import lmu.hradio.hradioshowcase.view.adapter.TimeShiftRecyclerViewAdapter;
import lmu.hradio.hradioshowcase.view.component.TrackLikeButton;
import lmu.hradio.hradioshowcase.view.fragment.player.RadioPlayerPlaybackFragment;

import static android.view.View.GONE;


public class CarPlaybackFragment extends Fragment {

    private static final String TAG = CarPlaybackFragment.class.getSimpleName();
    private static final String SERVICE_TAG = "service";

    private PlayBackDelegate playBackDelegate;
    private RadioPlayerPlaybackFragment.SkipItemProvider provider;
    private TimeShiftRecyclerViewAdapter timeShiftRecyclerViewAdapter;

    private RadioServiceViewModel currentService;

    private ProgrammeInformationViewModel currentEPG;

    private BottomSheetBehavior bottomSheetBehavior;

    private Bitmap currentLogo;

    @BindView(R.id.programme_panel)
    LinearLayout programmePanel;

    @BindView(R.id.car_play_button)
    ImageButton playButton;
    @BindView(R.id.car_pause_button)
    ImageButton pauseButton;
    @BindView(R.id.car_live_button)
    ImageButton liveButton;


    @BindView(R.id.car_epg_badge)
    ImageView epgBadge;

    @BindView(R.id.car_next_button)
    ImageButton nextButton;
    @BindView(R.id.car_back_button)
    ImageButton backButton;
    @BindView(R.id.car_podcast_button)
    ImageButton podcastButton;

    @BindView(R.id.car_track_like_button)
    TrackLikeButton trackLikeButton;


    @BindView(R.id.car_cover_image)
    ImageView coverImageView;
    @BindView(R.id.car_seekbar)
    SeekBar currentProgrammSeekBar;
    @BindView(R.id.car_progress_text)
    TextView progressTV;
    @BindView(R.id.car_title)
    TextView titleTV;
    @BindView(R.id.car_artist)
    TextView artistTV;
    @BindView(R.id.car_programm_title)
    TextView programmTitleTV;
    @BindView(R.id.car_programm_time)
    TextView programmTimeTV;
    @BindView(R.id.car_timeshift_list)
    RecyclerView timeshiftList;

    @BindView(R.id.car_epg_list)
    RecyclerView epgList;

    public CarPlaybackFragment() {
        // Required empty public constructor
    }

    public static CarPlaybackFragment newInstance(RadioServiceViewModel service) {
        CarPlaybackFragment cpFragment = new CarPlaybackFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(SERVICE_TAG, service);
        cpFragment.setArguments(bundle);
        return cpFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private boolean isSubstituted = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_car_playback, container, false);
        ButterKnife.bind(this, view);

        Bundle args = this.getArguments();
        if (args != null) {
            currentService = (RadioServiceViewModel) args.getSerializable(SERVICE_TAG);
            if (currentService != null && currentService.getLogo() != null) {
                byte[] data = currentService.getLogo().getImageData();
                if (data != null) {
                    currentLogo = BitmapFactory.decodeByteArray(data, 0, data.length);
                    createGradientBackground(currentLogo);
                    setCoverImage(currentLogo);

                }
            }
        }
        playButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.INVISIBLE);
        titleTV.setText(currentService.getServiceLabel());

        currentProgrammSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (!isSubstituted)
                        progressTV.setText(DateUtils.getAbsolut(i*1000, seekBar.getMax()*1000));
                    else
                        progressTV.setText(String.format("%s/%s", DateUtils.formatMinAndSeconds(seekBar.getProgress()*1000), DateUtils.formatMinAndSeconds(seekBar.getMax()*1000)));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if(playBackDelegate != null)
                    playBackDelegate.seekTo(progress * 1000);
                // TODO: User Activity: SeekBarChanged
                Log.i ("Car Mode", "SeekBar changed to :"+ progress);
}
        });
        initTimeShiftList();
        trackLikeButton.setTrackLikeService(playBackDelegate.getTrackLikeService());

        applyThemeColor();

        this.bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.drag_up_container));


        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    private void applyThemeColor() {
        @ColorInt int colorText = ColorUtils.resolveAttributeColor(R.attr.colorPrimaryText, getContext());
        @ColorInt int colorAccent = ColorUtils.resolveAttributeColor(R.attr.colorAccent, getContext());

        playButton.setColorFilter(colorText);
        pauseButton.setColorFilter(colorText);
        liveButton.setColorFilter(colorText);
        backButton.setColorFilter(colorText);
        podcastButton.setColorFilter(colorText);
        nextButton.setColorFilter(colorText);
        trackLikeButton.setColorFilter(colorText);
        epgBadge.setColorFilter(colorText);
        DrawableCompat.setTint(currentProgrammSeekBar.getThumb(), colorAccent);
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.playBackDelegate = null;
        this.provider = null;

    }

    private void initTimeShiftList() {
        timeShiftRecyclerViewAdapter = new TimeShiftRecyclerViewAdapter(provider.getSkipContent(), provider::onSkipItemClicked, provider::onSkipItemLongClicked, true);//(programmes, provider::onSkipItemClicked);
        timeshiftList.setAdapter(timeShiftRecyclerViewAdapter);
        timeShiftRecyclerViewAdapter.setCurrentItem(provider.getCurrentSkipItem());
        scrollToCurrentSkipItem(false);
        if (provider.getSkipContent() == null || provider.getSkipContent().isEmpty()) {
            timeshiftList.setVisibility(GONE);
        } else {
            timeshiftList.setVisibility(View.VISIBLE);

        }

    }

    private void scrollToCurrentSkipItem(boolean smooth) {
        int index = this.timeShiftRecyclerViewAdapter.getCurrentIndex();
        if (index < 0) return;
        if (smooth) {
            RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(timeshiftList.getContext()) {
                @Override
                protected int getHorizontalSnapPreference() {
                    return LinearSmoothScroller.SNAP_TO_END;
                }
            };
            smoothScroller.setTargetPosition(index);
            Objects.requireNonNull(timeshiftList.getLayoutManager()).startSmoothScroll(smoothScroller);
        } else {
            timeshiftList.scrollToPosition(index);
        }
    }


    @OnClick(R.id.car_play_button)
    void onPlayClicked() {
        playBackDelegate.onPlayClicked();
    }

    @OnClick(R.id.car_pause_button)
    void onPauseClicked() {
        playBackDelegate.onPauseClicked();
    }

    @OnClick(R.id.car_live_button)
    void onJumpToLiveClicked() {
        playBackDelegate.onJumpToLive();
    }

    @OnClick(R.id.car_back_button)
    void onSkipBackClicked() {
        playBackDelegate.onSkipBack();
    }

    @OnClick(R.id.car_next_button)
    void onSkipNextClicked() {
        playBackDelegate.onSkipNext(getActivity());
    }

    @OnClick(R.id.car_podcast_button)
    void onSelectPodcastClicked(){
        playBackDelegate.onSubstitutePodcast(getActivity());
     }


    public void started() {
        isSubstituted = false;
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                playButton.setVisibility(View.INVISIBLE);
                pauseButton.setVisibility(View.VISIBLE);
                initTimeShiftList();
                createGradientBackground(currentLogo);
                //  setTunerTypeBadge(state.getTunerType());
                //  favoriteImageButton.setRadioService(playbackStateHolder.getState().getRunningService());
            });
        }
    }


    public void paused() {
        if (this.getActivity() != null)
            getActivity().runOnUiThread(() -> {
                playButton.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.INVISIBLE);
            });
    }

    public void visualContent(ImageData visual) {
        if (this.getActivity() != null && visual != null && visual.getImageData() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(visual.getImageData(), 0, visual.getImageData().length);

            getActivity().runOnUiThread(() -> {
                    if (bitmap != null) {
                        setCoverImage(bitmap);
                        return;
                    }
                coverImageView.setImageDrawable(ContextCompat.getDrawable(coverImageView.getContext(), R.drawable.outline_radio_white_48dp));
            });
        }
    }

    public void textualContent(TextData content) {
        if (this.getActivity() != null && content != null && artistTV != null && titleTV != null)
            getActivity().runOnUiThread(() -> {
                if(content.getTitle().isEmpty()){
                    titleTV.setText(content.getText());
                    artistTV.setVisibility(GONE);
                }else {
                    artistTV.setVisibility(View.VISIBLE);

                    artistTV.setText(content.getContent());
                    titleTV.setText(content.getTitle());
                }
                //    parseDLPlus(content);
            });
    }

    public void playProgress(long curPos, long totalDuration, boolean isRunning) {
        if (this.getActivity() != null)
            getActivity().runOnUiThread(() -> {
                currentProgrammSeekBar.setMax((int) totalDuration);
                currentProgrammSeekBar.setProgress((int) curPos);
                if (!isRunning)
                    progressTV.setText(DateUtils.getAbsolut(curPos * 1000, totalDuration * 1000));
            });
    }

    public void stopped() {
        if (this.getActivity() != null)
            getActivity().runOnUiThread(() -> {
                playButton.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.INVISIBLE);
                initTimeShiftList();
            });
    }

    public void started(SubstitutionItem substitution) {
        isSubstituted = true;
        if (this.getActivity() != null) {
            getActivity().runOnUiThread(() -> {

                playButton.setVisibility(View.INVISIBLE);
                pauseButton.setVisibility(View.VISIBLE);
                artistTV.setText(substitution.getArtist());
                titleTV.setText(substitution.getTitle());
                if (substitution.getCover() != null)
                    visualContent(substitution.getCover());
                else
                    substitution.setListener(() -> visualContent(substitution.getCover()));
                if (substitution.getCover()!=null)  createGradientBackground(substitution.getCover().decode());
     });
        }
    }

    public void stopped(SubstitutionItem substitution) {
        isSubstituted = false;

        if (this.getActivity() != null)
            getActivity().runOnUiThread(() -> {
                Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.outline_radio_white_48dp);
                coverImageView.setImageDrawable(drawable);
            });
    }


    public void playProgress(SubstitutionItem substitution, long current, long total) {
        if (this.getActivity() != null)
            getActivity().runOnUiThread(() -> {
                currentProgrammSeekBar.setMax((int) total / 1000);
                currentProgrammSeekBar.setProgress((int) current / 1000);
                progressTV.setText(String.format("%s/%s", DateUtils.formatMinAndSeconds(current), DateUtils.formatMinAndSeconds(total)));
            });
    }


    public void skipItemAdded(SkipItem skipItem) {
        if (this.getActivity() != null)
            getActivity().runOnUiThread(() -> {

                if (timeshiftList.getVisibility() == GONE)
                    timeshiftList.setVisibility(View.VISIBLE);

                if (timeShiftRecyclerViewAdapter != null) {
                    timeShiftRecyclerViewAdapter.addItem(skipItem);
                }

            });

    }

    public void itemStarted(SkipItem skipItem) {
        if (getActivity() != null)
            getActivity().runOnUiThread(() -> {
                if (timeShiftRecyclerViewAdapter != null) {
                    timeShiftRecyclerViewAdapter.setCurrentItem(skipItem);
                } else{
                    initTimeShiftList();
                }
                scrollToCurrentSkipItem(true);
            });
    }

    private void setCoverImage(Bitmap bitmap) {
        if(this.coverImageView != null)
            this.coverImageView.setImageBitmap(bitmap);
    }

    public void onEPGUpdate(ProgrammeInformationViewModel programmeInformation) {
        currentEPG = programmeInformation;
        if (getActivity() != null)
            //get todays schedule
            getActivity().runOnUiThread(this::loadEPG);
    }

    private void loadEPG() {
        View view = getView();
        if (view != null) {

            if (currentEPG == null || currentEPG.getCurrentRunningProgramme() == null) {
                epgList.setAdapter(null);
                programmePanel.setOnClickListener((v) -> {});
                bottomSheetBehavior.setPeekHeight(0);
                return;
            }

            float peakHeight = view.getResources().getDimension(R.dimen.car_peak_height);
            bottomSheetBehavior.setPeekHeight((int) peakHeight);

            //       currentProgrammeScheduleTextView.setOnClickListener(v -> onOpenProgrammeClicked());
            //       currentProgrammeTextView.setOnClickListener(v -> onOpenProgrammeClicked());

            //       searchEPGTextView.setVisibility(View.INVISIBLE);

            programmePanel.setOnClickListener((v) -> openEPG());

            ProgrammeViewModel programme = currentEPG.getCurrentRunningProgramme();

            String nameString = programme.getQualifiedName();
            for (LocationViewModel location : programme.getLocations()) {
                for (TimeViewModel t : location.getTimes()) {
                    String startTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(t.getStartTime());
                    String endTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(t.getEndTime());

                    String timeBuilder = String.format(getString(R.string.programme_name_label), startTime, endTime);
                    programmTitleTV.setVisibility(View.VISIBLE);
                    programmTimeTV.setVisibility(View.VISIBLE);
                    programmTitleTV.setText(nameString);
                    programmTimeTV.setText(timeBuilder);
                }
            }
            if (currentEPG.getSchedules().size() == 1) {
                initProgrameList(currentEPG.getSchedules().get(0));

            } else {
                for (ScheduleViewModel schedule : currentEPG.getSchedules()) {
                    if (android.text.format.DateUtils.isToday(schedule.getScope().getStartTime().getTime())) {
                        initProgrameList(schedule);
                        break;
                    }
                }
            }
        }
    }

    private void openEPG() {
        bottomSheetBehavior.setState(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED ? BottomSheetBehavior.STATE_EXPANDED : BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void initProgrameList(ScheduleViewModel schedule) {
        final EPGProgrammListAdapter adapter = new EPGProgrammListAdapter(schedule.getProgrammes(), podcasts ->{
        });
        epgList.setAdapter(adapter);

        RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(epgList.getContext()) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };
        smoothScroller.setTargetPosition(adapter.getIndexOfCurrent());
        Objects.requireNonNull(epgList.getLayoutManager()).startSmoothScroll(smoothScroller);
    }


    void setPlayBackDelegate(PlayBackDelegate delegate) {
        this.playBackDelegate = delegate;
    }

    void setProvider(RadioPlayerPlaybackFragment.SkipItemProvider siProvider) {
        this.provider = siProvider;
    }

    private void createGradientBackground(Bitmap bitmap) {
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

    public void clearSkipItems() {
        timeShiftRecyclerViewAdapter.clear();
    }

}
