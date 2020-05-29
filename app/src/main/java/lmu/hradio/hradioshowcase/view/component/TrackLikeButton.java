package lmu.hradio.hradioshowcase.view.component;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.ColorInt;
import androidx.annotation.UiThread;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.content.ContextCompat;

import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.listener.TrackLikeService;
import lmu.hradio.hradioshowcase.util.ColorUtils;

public class TrackLikeButton extends AppCompatImageButton implements TrackLikeService.TrackChangeListener {

    private TrackLikeService.Track track;
    private TrackLikeService trackLikeService;

    public TrackLikeButton(Context context) {
        super(context);
    }

    public TrackLikeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TrackLikeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void setDrawable(boolean selected){
        int unselectedDrawableId = R.drawable.baseline_favorite_border_white_36;
        int selectedDrawableId = R.drawable.baseline_favorite_white_36;
        String toolTipText = (selected)? String.format(getResources().getString(R.string.tooltip_remove_favorites_button), track.getName()) : String.format(getResources().getString(R.string.tooltip_add_favorites_button), track.getName());
        this.post(() -> {
            @ColorInt int color = ColorUtils.resolveAttributeColor(R.attr.colorPrimaryText, getContext());
            TooltipCompat.setTooltipText(this, toolTipText);
            this.setImageResource(selected ? selectedDrawableId : unselectedDrawableId);
            this.setColorFilter(color);
        });
    }

    public void setTrackLikeService(TrackLikeService service) {
        this.trackLikeService = service;
        if(trackLikeService != null) {
            trackLikeService.registerTrackChangeListener(this);
            if (track != null)
                setTrack(track);
        }
    }

    public void setTrack(TrackLikeService.Track track){

        this.track = track;
        if(track == null)
            this.post(() -> super.setImageDrawable(new ColorDrawable(ContextCompat.getColor(getContext(), android.R.color.transparent))));
        else {
            if (trackLikeService != null) {
                trackLikeService.containsTrack(track, this::setDrawable);
                this.setOnClickListener(view -> trackLikeService.toggleTrack(track , this::setDrawable));
                }
            }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(trackLikeService != null){
            trackLikeService.unregisterTrackChangeListener(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(track != null && trackLikeService != null){
            trackLikeService.registerTrackChangeListener(this);
            trackLikeService.containsTrack(track , this::setDrawable);
        } else if(trackLikeService != null){
            trackLikeService.registerTrackChangeListener(this);
            setTrack(trackLikeService.getCurrent());
        }
    }


    @Override
    public void onTrackChange(TrackLikeService.Track newTrack) {
        setTrack(newTrack);
    }
}
