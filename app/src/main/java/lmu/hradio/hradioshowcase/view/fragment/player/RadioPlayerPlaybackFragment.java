package lmu.hradio.hradioshowcase.view.fragment.player;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.hradio.timeshiftplayer.SkipItem;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.error.GeneralError;
import lmu.hradio.hradioshowcase.listener.PlayBackDelegate;
import lmu.hradio.hradioshowcase.listener.PlayBackListener;
import lmu.hradio.hradioshowcase.listener.State;
import lmu.hradio.hradioshowcase.model.state.PlayBackState;
import lmu.hradio.hradioshowcase.model.substitiution.SubstitutionItem;
import lmu.hradio.hradioshowcase.model.view.ImageData;
import lmu.hradio.hradioshowcase.model.view.TextData;
import lmu.hradio.hradioshowcase.util.ColorUtils;
import lmu.hradio.hradioshowcase.util.DateUtils;
import lmu.hradio.hradioshowcase.util.SharedPreferencesHelper;
import lmu.hradio.hradioshowcase.view.activity.MainActivity;
import lmu.hradio.hradioshowcase.view.adapter.TimeShiftRecyclerViewAdapter;
import lmu.hradio.hradioshowcase.view.component.TrackLikeButton;

import static android.view.View.GONE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link RadioPlayerPlaybackFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RadioPlayerPlaybackFragment extends Fragment implements PlayBackListener {

    private static final String TAG = RadioPlayerPlaybackFragment.class.getSimpleName();

    private PlayBackDelegate playBackDelegate;
    private State.PlayBackStateHolder playbackStateHolder;
    private SkipItemProvider provider;

    private boolean substitutionEnabled = false;

    @BindView(R.id.song_text_view)
    TextView nameTextView;
    @BindView(R.id.artist_text_view)
    TextView publisherTextView;
    @BindView(R.id.currently_playing_seek_bar)
    SeekBar currentProgrammSeekBar;

    @BindView(R.id.progress_text)
    TextView progressText;
    @BindView(R.id.program_timeshift_list)
    RecyclerView timeshiftList;

    @BindView(R.id.spotify_like_button)
    TrackLikeButton trackLikeButton;

    @BindView(R.id.live_button)
    ImageButton liveButton;
    @BindView(R.id.podcat_button)
    ImageButton podcastButton;
	@BindView(R.id.sharetoken_button)
	ImageButton sharetokenButton;
    @BindView(R.id.skip_back_button)
    ImageButton skipBackButton;
    @BindView(R.id.skip_next_button)
    ImageButton skipNextButton;
    @BindView(R.id.cover_image_view)
    ImageView coverImageView;
    @BindView(R.id.play_button)
    ImageButton playButton;
    @BindView(R.id.pause_button)
    ImageButton pauseButton;

    private TimeShiftRecyclerViewAdapter timeShiftRecyclerViewAdapter;

    public RadioPlayerPlaybackFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RadioPlayerFragment.
     */
    public static RadioPlayerPlaybackFragment newInstance() {
        return new RadioPlayerPlaybackFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            playBackDelegate = ((MainActivity) context).getPlayBackDelegate();
            playBackDelegate.registerPlayBackListener(this);
            playbackStateHolder = (MainActivity) context;
            provider = (MainActivity) context;
            substitutionEnabled = SharedPreferencesHelper.getInt(getActivity(), SharedPreferencesHelper.SUBSTITUTION_PROVIDER_TYPE, 0) != 0;
        } else {
            throw new RuntimeException(context.toString() + " must implement PlayBackDelegate");
        }

    }

    private boolean isSubstituted() {
        return playbackStateHolder.getState().getSubstitution() != PlayBackState.SubstituionState.INACTIVE;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_radio_playback, container, false);
        ButterKnife.bind(this, view);
        publisherTextView.setSelected(true);
        nameTextView.setSelected(true);
        publisherTextView.setVisibility(View.INVISIBLE);
        PlayBackState state = playbackStateHolder.getState();

        if(substitutionEnabled) {
            podcastButton.setVisibility(View.VISIBLE);
        }

        if (state != null) {

            playButton.setVisibility(state.isRunning() ? View.INVISIBLE : View.VISIBLE);
            pauseButton.setVisibility(state.isRunning() ? View.VISIBLE : View.INVISIBLE);
            visualContent(state.getCover());
            textualContent(state.getTextData());
        }

        currentProgrammSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (playbackStateHolder != null) {
                    if (!isSubstituted()) {
                        progressText.setText(DateUtils.getAbsolut(i * 1000, seekBar.getMax() * 1000));
                    } else {
                        progressText.setText(String.format("%s/%s", DateUtils.formatMinAndSeconds(seekBar.getProgress() * 1000), DateUtils.formatMinAndSeconds(seekBar.getMax() * 1000)));
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                playBackDelegate.seekTo(progress * 1000);
            }
        });

        initTimeShiftList();
        setupTooltips();

        trackLikeButton.setTrackLikeService(playBackDelegate.getTrackLikeService());

        setImageTints();
        return view;
    }

    private void setImageTints() {
        if (getContext() != null) {
            int color = ColorUtils.resolveAttributeColor(R.attr.colorPrimaryText, getContext());

            trackLikeButton.setColorFilter(color);
            pauseButton.setColorFilter(color);
            playButton.setColorFilter(color);

            liveButton.setColorFilter(color);
            podcastButton.setColorFilter(color);
            skipBackButton.setColorFilter(color);
            skipNextButton.setColorFilter(color);
        }
    }

    private void setupTooltips() {
        TooltipCompat.setTooltipText(liveButton, getResources().getString(R.string.tooltip_skip_live_button));
        TooltipCompat.setTooltipText(podcastButton, getResources().getString(R.string.tooltip_podcast_button));
    }

    private void setCoverImage(Bitmap bitmap) {
        this.coverImageView.setImageBitmap(bitmap);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        playBackDelegate.unregisterPlayBackListener(this);
        playBackDelegate = null;
        provider = null;
        playbackStateHolder = null;
    }

    @OnClick(R.id.skip_back_button)
    void onSkipBackClicked() {
        playBackDelegate.onSkipBack();
    }

    @OnClick(R.id.skip_next_button)
    void onSkipNextClicked() {
        playBackDelegate.onSkipNext(getActivity());
    }

    @OnClick(R.id.live_button)
    void onJumpToLiveClicked() {
        playBackDelegate.onJumpToLive();
    }

    @OnClick(R.id.play_button)
    void onPlayClicked() {
        playBackDelegate.onPlayClicked();
    }

    @OnClick(R.id.pause_button)
    void onPauseClicked() {
        playBackDelegate.onPauseClicked();
    }

    @OnClick(R.id.podcat_button)
    void onPodcastSubstitutionClicked() {
        playBackDelegate.onSubstitutePodcast(getActivity());
    }

    @OnClick(R.id.sharetoken_button)
    void shareSbtTokenClicked() {playBackDelegate.shareSbtToken(); }


    private void initTimeShiftList() {
        timeShiftRecyclerViewAdapter = new TimeShiftRecyclerViewAdapter(provider.getSkipContent(), provider::onSkipItemClicked, provider::onSkipItemLongClicked);//(programmes, provider::onSkipItemClicked);
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


    @Override
    public void started() {
        if (this.getActivity() != null)
            getActivity().runOnUiThread(() -> {
                playButton.setVisibility(View.INVISIBLE);
                pauseButton.setVisibility(View.VISIBLE);
                initTimeShiftList();
            });
    }

    @Override
    public void paused() {
        if (this.getActivity() != null)
            getActivity().runOnUiThread(() -> {
                playButton.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.INVISIBLE);
            });
    }

    @Override
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

    @Override
    public void playProgress(SubstitutionItem substitution, long current, long total) {
        if (this.getActivity() != null)
            getActivity().runOnUiThread(() -> {
                currentProgrammSeekBar.setMax((int) total / 1000);
                currentProgrammSeekBar.setProgress((int) current / 1000);
                progressText.setText(String.format("%s/%s", DateUtils.formatMinAndSeconds(current), DateUtils.formatMinAndSeconds(total)));
            });
    }

    private static SimpleDateFormat mDateFormat = new SimpleDateFormat("HH:mm:ss");
    @Override
    public void playProgressRealtime(long realTimePosix, long streamTimePosix, long curPos, long totalDuration) {
        if (this.getActivity() != null)
            getActivity().runOnUiThread(() -> {
                currentProgrammSeekBar.setMax((int) totalDuration);
                currentProgrammSeekBar.setProgress((int) curPos);
                progressText.setText(mDateFormat.format(new Date(streamTimePosix)));
            });
    }

    @Override
    public void skipItemAdded(SkipItem skipItem) {
        if (this.getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (timeshiftList.getVisibility() == GONE) {
                    timeshiftList.setVisibility(View.VISIBLE);
                }

                if (timeShiftRecyclerViewAdapter != null) {
                    timeShiftRecyclerViewAdapter.addItem(skipItem);
                }

            });
        }
    }

    @Override
    public void sbtSeeked() {

    }

    @Override
    public void skipItemRemoved(SkipItem skipItem) {
        if (this.getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (timeShiftRecyclerViewAdapter != null) {
                    timeShiftRecyclerViewAdapter.removeItem(skipItem);
                }

            });
        }
    }

    @Override
    public void itemStarted(SkipItem skipItem) {
        if (getActivity() != null)
            getActivity().runOnUiThread(() -> {
                if (timeShiftRecyclerViewAdapter != null) {
                    timeShiftRecyclerViewAdapter.setCurrentItem(skipItem);
                }
                scrollToCurrentSkipItem(true);
            });
    }

    @Override
    public void textualContent(TextData content) {
        if (this.getActivity() != null && content != null)
            getActivity().runOnUiThread(() -> {
                if (content.getContent().isEmpty()) {
                    publisherTextView.setVisibility(GONE);
                    nameTextView.setText(content.getText());
                } else {
                    publisherTextView.setVisibility(View.VISIBLE);
                    publisherTextView.setText(content.getContent());
                    nameTextView.setText(content.getTitle());
                    parseDLPlus(content);

                }
            });
    }

    private void parseDLPlus(TextData content) {
        Intent intent = null;
        if (content.getTextType() != null)
            switch (content.getTextType()) {
                case Track:
                    break;
                case None:
                    break;
                case News:
                    break;

                case SMS:
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("sms:" + content.getContent()));

                    break;
                case WEB:
                    String url = content.getContent();
                    if (!url.startsWith("http://") && !url.startsWith("https://"))
                        url = "http://" + url;
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    break;
                case EMAIL:
                    intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{content.getContent()}); // String[] addresses
                    intent.setData(Uri.parse("mailto:" + content.getContent()));
                    break;
                case Phone:
                    intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + content.getContent()));
                    intent = Intent.createChooser(intent, getString(R.string.choose_email_client));
                    break;
            }
        @ColorInt int textColor;
        if (getContext() != null) {
            if (intent != null) {
                final Intent finalIntent = intent;
                publisherTextView.setOnClickListener(v -> startActivity(finalIntent));
                textColor = ColorUtils.resolveAttributeColor(R.attr.colorAccent, Objects.requireNonNull(getContext()));
                publisherTextView.setPaintFlags(publisherTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

            } else {

                publisherTextView.setOnClickListener(v -> {
                });
                textColor = ColorUtils.resolveAttributeColor(R.attr.colorSecondaryText, getContext());
                publisherTextView.setPaintFlags(0);

            }
            publisherTextView.setTextColor(textColor);
        }
    }


    @Override
    public void stopped() {
        if (this.getActivity() != null)
            getActivity().runOnUiThread(() -> {
                playButton.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.INVISIBLE);
                initTimeShiftList();

            });
    }


    @Override
    public void playProgress(long curPos, long totalDuration) {
        if (this.getActivity() != null && !isSubstituted()) {
            getActivity().runOnUiThread(() -> {
                progressText.setText(DateUtils.getAbsolut(curPos * 1000, totalDuration * 1000));
                currentProgrammSeekBar.setMax((int) totalDuration);
                currentProgrammSeekBar.setProgress((int) curPos);
                if (playbackStateHolder != null && !playbackStateHolder.getState().isRunning()) {

                    progressText.setText(DateUtils.getAbsolut(curPos * 1000, totalDuration * 1000));
                }
            });
        }
    }


    @Override
    public void started(SubstitutionItem substitution) {
        if (this.getActivity() != null) {
            if (substitution.getCover() != null)
                visualContent(substitution.getCover());
            else
                substitution.setListener(() -> visualContent(substitution.getCover()));

            getActivity().runOnUiThread(() -> {

                playButton.setVisibility(View.INVISIBLE);
                pauseButton.setVisibility(View.VISIBLE);
                publisherTextView.setVisibility(View.VISIBLE);
                publisherTextView.setText(substitution.getArtist());
                nameTextView.setText(substitution.getTitle());
            });
        }
    }


    @Override
    public void stopped(SubstitutionItem substitution) {
        if (this.getActivity() != null) {

            getActivity().runOnUiThread(() -> {
                Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.outline_radio_white_48dp);
                coverImageView.setImageDrawable(drawable);
                textualContent(new TextData(playbackStateHolder.getState().getRunningService().getServiceLabel()));
            });
            visualContent(playbackStateHolder.getState().getRunningService().getLogo());
        }
    }

    @Override
    public void onError(@NonNull GeneralError error) {

    }


    public interface SkipItemProvider {

        List<SkipItem> getSkipContent();

        void onSkipItemClicked(SkipItem item);

        boolean onSkipItemLongClicked(SkipItem item);

        long getTotalDuration();

        SkipItem getCurrentSkipItem();

    }
}
