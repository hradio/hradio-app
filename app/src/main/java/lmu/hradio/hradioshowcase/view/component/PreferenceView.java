package lmu.hradio.hradioshowcase.view.component;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import lmu.hradio.hradioshowcase.R;

public class PreferenceView extends ConstraintLayout {

    @BindView(R.id.title_text)
    TextView titleTextView;

    @BindView(R.id.preference_description)
    TextView summaryTextView;


    public PreferenceView(Context context) {
        super(context);
        init(null, 0);
    }

    public PreferenceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public PreferenceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        inflate(getContext(), R.layout.preference_layout, this);
        ButterKnife.bind(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public void setTitle(String title){
        this.titleTextView.setText(title);
    }


    public void setContentText(String text){
        this.summaryTextView.setText(text);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }



}
