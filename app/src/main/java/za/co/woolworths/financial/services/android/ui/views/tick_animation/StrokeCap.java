package za.co.woolworths.financial.services.android.ui.views.tick_animation;

import android.graphics.Paint;

public enum StrokeCap {
    BUTT(Paint.Cap.BUTT),
    ROUND(Paint.Cap.ROUND),
    SQUARE(Paint.Cap.SQUARE);
    final Paint.Cap paintCap;

    StrokeCap(Paint.Cap paintCap) {
        this.paintCap = paintCap;
    }
}
