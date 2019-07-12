package za.co.woolworths.financial.services.android.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import androidx.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.SeekBar;

public class WSeekBar extends SeekBar {
    public WSeekBar(Context context) {
        super(context);
        init();
    }

    private void init() {

    }

    public WSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public WSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public WSeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected synchronized void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
    }
}
