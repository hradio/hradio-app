package lmu.hradio.hradioshowcase.view.fragment.player;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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
import lmu.hradio.hradioshowcase.model.view.RadioServiceViewModel;
import lmu.hradio.hradioshowcase.model.view.TextData;
import lmu.hradio.hradioshowcase.util.ColorUtils;
import lmu.hradio.hradioshowcase.view.activity.MainActivity;

public class MiniMusicPlayerFragment extends Fragment implements PlayBackListener {

    private final String TAG = MiniMusicPlayerFragment.this.toString();

    @BindView(R.id.song_text_view)
    TextView titleTextView;
    @BindView(R.id.play_button)
    ImageButton playButton;
    @BindView(R.id.pause_button)
    ImageButton pauseButton;
    @BindView(R.id.cover_image_view)
    ImageView coverImageView;

    @BindView(R.id.substitution_logo)
    ImageView substitutionImageView;
    @BindView(R.id.fullscreen_button)
    ImageButton fullScreenButton;

    private OnFullScreenClickedListener mListener;
    private PlayBackDelegate delegate;
    private State.PlayBackStateHolder stateHolder;

    public MiniMusicPlayerFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MiniMusicPlayerFragment.
     */
    public static MiniMusicPlayerFragment newInstance() {
        return new MiniMusicPlayerFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mini_music_player, container, false);
        ButterKnife.bind(this, view);

        titleTextView.setSelected(true);

        int color = ColorUtils.resolveAttributeColor(R.attr.colorPrimaryText,  view.getContext());


        substitutionImageView.setColorFilter(color);
        playButton.setColorFilter(color);
        pauseButton.setColorFilter(color);
        fullScreenButton.setColorFilter(color);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStop() {
        super.onStop();
        delegate.unregisterPlayBackListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        delegate.registerPlayBackListener(this);
        PlayBackState state = stateHolder.getState();
        if (state != null) {
            playButton.setVisibility(state.isRunning() ? View.GONE : View.VISIBLE);
            pauseButton.setVisibility(state.isRunning() ? View.VISIBLE : View.GONE);
            resolveSubstituionLogo(state.getSubstitution());
            visualContent(state.getCover());
            textualContent(state.getTextData());
        } else {
            substitutionImageView.setVisibility(View.GONE);

        }
    }

    private void resolveSubstituionLogo(PlayBackState.SubstituionState substituionState){

        switch (substituionState){
            case INACTIVE:
                substitutionImageView.setVisibility(View.GONE);
                break ;
            case NATIVE:
                substitutionImageView.setVisibility(View.GONE);
                break;
            case SPOTIFY:
                substitutionImageView.setVisibility(View.VISIBLE);
                substitutionImageView.setImageResource(R.drawable.spotify_icon_rgb_white);
        }
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mListener = (OnFullScreenClickedListener) context;
            delegate = ((MainActivity) context).getPlayBackDelegate();
            stateHolder = (State.PlayBackStateHolder) context;
        } else {
            throw new IllegalStateException("Activity mus be instance of MainActivity");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        delegate = null;
    }


    @OnClick(R.id.pause_button)
    void onPauseClicked() {
        delegate.onPauseClicked();
    }

    @OnClick(R.id.play_button)
    void onPlayClicked() {
        delegate.onPlayClicked();
    }

    @OnClick(R.id.fullscreen_button)
    void setFullscreenClicked() {
        mListener.onMiniPlayerFullscreenClicked();
    }


    @Override
    public void started() {
        if (this.getActivity() != null)
            getActivity().runOnUiThread(() -> {
                if (playButton == null || pauseButton == null)
                    return;
                playButton.setVisibility(View.INVISIBLE);
                pauseButton.setVisibility(View.VISIBLE);
            });
    }

    @Override
    public void paused() {
        if (this.getActivity() != null)
            getActivity().runOnUiThread(() -> {
                if (playButton == null || pauseButton == null)
                    return;
                playButton.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.INVISIBLE);
            });
    }


    @Override
    public void stopped() {
        if (this.getActivity() != null)
            getActivity().runOnUiThread(() -> {
                if (playButton == null || pauseButton == null)
                    return;
                playButton.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.INVISIBLE);
            });
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
    public void playProgress(long current, long total) {
    }

    @Override
    public void playProgressRealtime(long realTimePosix, long streamTimePosix, long curPos, long totalDuration) {

    }

	@Override
	public void skipItemRemoved(SkipItem skipItem) {

	}

    @Override
    public void sbtSeeked() {

    }

    @Override
    public void started(SubstitutionItem substitution) {
        if (this.getActivity() != null) {
            if (substitution.getCover() != null)
                visualContent(substitution.getCover());
            else
                substitution.setListener(() -> visualContent(substitution.getCover()));
            getActivity().runOnUiThread(() -> {
                String text = substitution.getTitle() + " - " + substitution.getArtist();
                titleTextView.setText(text);

                playButton.setVisibility(View.INVISIBLE);
                pauseButton.setVisibility(View.VISIBLE);
                substitutionImageView.setVisibility(View.VISIBLE);

                Bitmap bitmap;
                if (substitution.getSubstitutionType().equals(SubstitutionItem.NATIVE_TYPE)) {
                    ImageData logo = stateHolder.getState().getRunningService().getLogo();
                    if (logo != null) {
                        bitmap = BitmapFactory.decodeByteArray(logo.getImageData(), 0, logo.getImageData().length);
                    } else {
                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.outline_radio_white_48dp);
                    }
                } else {
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.spotify_icon_rgb_white);

                }
                substitutionImageView.setImageBitmap(bitmap);
            });
        }
    }



    @Override
    public void stopped(SubstitutionItem substitution) {
        if (this.getActivity() != null)
            getActivity().runOnUiThread(() -> {
                Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.outline_radio_white_48dp);
                coverImageView.setImageDrawable(drawable);
                substitutionImageView.setVisibility(View.GONE);
                RadioServiceViewModel running = stateHolder.getState().getRunningService();
                textualContent(new TextData(running.getServiceLabel()));
                visualContent(running.getLogo());
            });
    }

    @Override
    public void textualContent(TextData content) {
        if (this.getActivity() != null && content != null) {
            getActivity().runOnUiThread(() -> titleTextView.setText(content.getText()));
        }
    }

    @Override
    public void visualContent(ImageData visual) {
        if (this.getActivity() != null && coverImageView != null && visual != null && visual.getImageData() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(visual.getImageData(), 0, visual.getImageData().length);

            getActivity().runOnUiThread(() -> {
                    if (bitmap != null) {
                        coverImageView.setImageBitmap(bitmap);
                        return;
                    }
                coverImageView.setImageDrawable(ContextCompat.getDrawable(coverImageView.getContext(), R.drawable.outline_radio_white_48dp));
            });
        }
    }

    @Override
    public void onError(@NonNull GeneralError error) {

    }

    public interface OnFullScreenClickedListener {
        void onMiniPlayerFullscreenClicked();
    }
}
