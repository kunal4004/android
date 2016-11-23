package za.co.woolworths.financial.services.android.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import za.co.woolworths.financial.services.android.util.Utils;

/**
 * Created by W7099877 on 26/10/2016.
 */

public class CarouselView extends LinearLayout {
    private float scale = Utils.BIG_SCALE;

    public CarouselView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CarouselView(Context context) {
        super(context);
    }

    public void setScaleBoth(float scale) {
        this.scale = scale;
        this.invalidate();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = this.getWidth();
        int h = this.getHeight();
        canvas.scale(scale, scale, w/2, h/2);

        super.onDraw(canvas);
    }

}