package lmu.hradio.hradioshowcase.view.component;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.TooltipCompat;

import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.database.Database;
import lmu.hradio.hradioshowcase.model.view.RadioServiceViewModel;
import lmu.hradio.hradioshowcase.util.ColorUtils;

public class FavoriteImageButton extends AppCompatImageButton implements Database.OnFavoritesChangeListener {

    private RadioServiceViewModel service;

    public FavoriteImageButton(Context context) {
        super(context);
    }

    public FavoriteImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FavoriteImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void setDrawable(boolean selected){
        if(service != null) {
            int unselectedDrawableId = R.drawable.outline_star_border_white_36;
            int selectedDrawableId = R.drawable.outline_star_white_36;
            String toolTipText = (selected) ? String.format(getResources().getString(R.string.tooltip_remove_favorites_button), service.getServiceLabel()) : String.format(getResources().getString(R.string.tooltip_add_favorites_button), service.getServiceLabel());
            this.post(() -> {
                @ColorInt int color = ColorUtils.resolveAttributeColor(R.attr.colorPrimaryText, getContext());
                TooltipCompat.setTooltipText(this, toolTipText);
                this.setImageResource(selected ? selectedDrawableId : unselectedDrawableId);
                this.setColorFilter(color);
            });
        }
    }

    public void setRadioService(RadioServiceViewModel service){
        this.service = service;

        Database.getInstance().contains(service,  this::setDrawable);
        this.setOnClickListener(view -> Database.getInstance().toggleFavorite(service,  this::setDrawable, view.getContext()));
    }

    @Override
    public void onAdded(RadioServiceViewModel service) {
        if(service.equals(this.service))
            setDrawable(true);
    }

    @Override
    public void onRemoved(RadioServiceViewModel service) {
        if(service.equals(this.service))
            setDrawable(false);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Database.getInstance().unregisterFavoritesChangeListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Database.getInstance().registerFavoritesChangeListener(this);
        if(service != null)
            Database.getInstance().contains(service, this::setDrawable);
    }


}
