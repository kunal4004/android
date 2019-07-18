package za.co.woolworths.financial.services.android.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

import com.awfs.coordination.R;

public class WTabIndicator extends View {

    private int mIndicatorCount;
    private int mIndicatorUnselectedColor;
    private int mIndicatorSelectedColor;
    private int mIndicatorBorderColor;
    private int mCurrentPosition;
    private float mOffset;
    private int mIndicatorBorderSize;
    private Animation mAnimation = Animation.FADE;


    public WTabIndicator(Context context) {
        this(context, null);
    }

    public WTabIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WTabIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs, defStyleAttr);
    }

    @TargetApi(21)
    public WTabIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(attrs, defStyleAttr);
    }

    private void initView(AttributeSet attrs, int defStyleAttr) {
        TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.TabIndicator, 0, 0);
        mIndicatorCount = a.getInteger(R.styleable.TabIndicator_count, 3);
        mCurrentPosition = a.getInteger(R.styleable.TabIndicator_currentPosition, 1);
        mIndicatorUnselectedColor = a.getColor(R.styleable.TabIndicator_unselectedColor, Color.parseColor("#CCCCCC"));
        mIndicatorSelectedColor = a.getColor(R.styleable.TabIndicator_selectedColor, Color.parseColor("#686663"));
        mIndicatorBorderColor = a.getColor(R.styleable.TabIndicator_borderColor, Color.parseColor("#00000000"));
        mIndicatorBorderSize = a.getDimensionPixelSize(R.styleable.TabIndicator_borderSize, 2);
        mOffset = a.getFloat(R.styleable.TabIndicator_offset, 0f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = getHeight();
        int width = getWidth();
        int singleCircleWidth = width / mIndicatorCount;
        float radius = (((float) Math.min(singleCircleWidth, height)) / 2f) - 2;
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(mIndicatorBorderColor);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(mIndicatorBorderSize);
        Paint selectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectedPaint.setColor(mIndicatorSelectedColor);
        selectedPaint.setStyle(Paint.Style.FILL);
        Paint selectedOverRidePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectedOverRidePaint.setColor(mIndicatorSelectedColor);
        selectedOverRidePaint.setStyle(Paint.Style.FILL);
        selectedOverRidePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        Paint unSelectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        unSelectedPaint.setColor(mIndicatorUnselectedColor);
        unSelectedPaint.setStyle(Paint.Style.FILL);
        Paint unSelectedOverRidePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        unSelectedOverRidePaint.setColor(mIndicatorUnselectedColor);
        unSelectedOverRidePaint.setStyle(Paint.Style.FILL);
        unSelectedOverRidePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        for (int i = 0; i < mIndicatorCount; i++) {
            int cx = i * singleCircleWidth + (singleCircleWidth / 2);
            int cy = height / 2;
            canvas.drawCircle(cx, cy, radius, borderPaint);
            switch (mAnimation) {
                case SLIDE:
                    if (i == mCurrentPosition) {
                        int sc = canvas.saveLayer(0, 0, width, height, null, Canvas.ALL_SAVE_FLAG);
                        canvas.translate(0, 0);
                        canvas.drawCircle(cx, cy, radius, selectedPaint);
                        if (mOffset != 0) {
                            float leftOffset = 0;
                            float rightOffset = 0;
                            if (mOffset < 0) {
                                leftOffset = (radius * 2) - ((radius * 2) * (mOffset * -1));
                            } else {
                                rightOffset = (radius * 2) - ((radius * 2) * mOffset);
                            }
                            canvas.drawRect((cx - radius) + leftOffset, 0, (cx + radius) - rightOffset, height, unSelectedOverRidePaint);
                        }
                        canvas.restoreToCount(sc);

                    } else {
                        int sc = canvas.saveLayer(0, 0, width, height, null, Canvas.ALL_SAVE_FLAG);
                        canvas.translate(0, 0);
                        canvas.drawCircle(cx, cy, radius, unSelectedPaint);
                        if (mOffset > 0 && i + 1 <= mIndicatorCount && i == mCurrentPosition + 1) {
                            float leftOffset = 0;
                            float rightOffset;
                            rightOffset = (radius * 2) - ((radius * 2) * mOffset);
                            canvas.drawRect((cx - radius) + leftOffset, 0, (cx + radius) - rightOffset, height, selectedOverRidePaint);
                        } else if (mOffset < 0 && i == mCurrentPosition - 1) {
                            float leftOffset;
                            float rightOffset = 0;
                            leftOffset = (radius * 2) - ((radius * 2) * (mOffset * -1));
                            canvas.drawRect((cx - radius) + leftOffset, 0, (cx + radius) - rightOffset, height, selectedOverRidePaint);
                        }
                        canvas.restoreToCount(sc);
                    }
                    break;
                case FADE: {
                    if (mIndicatorUnselectedColor == Color.TRANSPARENT) {
                        if (i == mCurrentPosition) {
                            if (mOffset != 0) {
                                int alpha = selectedPaint.getAlpha();
                                selectedPaint.setAlpha((int) (255 * (1 - Math.sqrt(Math.pow(mOffset, 2)))));
                                canvas.drawCircle(cx, cy, radius, selectedPaint);
                                selectedPaint.setAlpha(alpha);
                            } else {
                                selectedPaint.setAlpha(255);
                                canvas.drawCircle(cx, cy, radius, selectedPaint);
                            }
                        } else {
                            canvas.drawCircle(cx, cy, radius, unSelectedPaint);
                            int alpha = selectedPaint.getAlpha();
                            selectedPaint.setAlpha((int) (255 * (Math.sqrt(Math.pow(mOffset, 2)))));
                            if (mOffset > 0 && i + 1 <= mIndicatorCount && i == mCurrentPosition + 1) {
                                canvas.drawCircle(cx, cy, radius, selectedPaint);
                            } else if (mOffset < 0 && i == mCurrentPosition - 1) {
                                canvas.drawCircle(cx, cy, radius, selectedPaint);
                            } else {
                                canvas.drawCircle(cx, cy, radius, unSelectedPaint);
                                break;
                            }
                            selectedPaint.setAlpha(alpha);

                        }
                    } else {
                        if (i == mCurrentPosition) {
                            canvas.drawCircle(cx, cy, radius, selectedPaint);
                            if (mOffset != 0) {
                                int alpha = unSelectedPaint.getAlpha();
                                unSelectedPaint.setAlpha((int) (255 * Math.sqrt(Math.pow(mOffset, 2))));
                                canvas.drawCircle(cx, cy, radius, unSelectedPaint);
                                unSelectedPaint.setAlpha(alpha);
                            }
                        } else {
                            canvas.drawCircle(cx, cy, radius, unSelectedPaint);
                            int alpha = selectedPaint.getAlpha();
                            if (mOffset > 0 && i + 1 <= mIndicatorCount && i == mCurrentPosition + 1) {
                                selectedPaint.setAlpha((int) (255 * (Math.sqrt(Math.pow(mOffset, 2)))));
                            } else if (mOffset < 0 && i == mCurrentPosition - 1) {
                                selectedPaint.setAlpha((int) (255 * (Math.sqrt(Math.pow(mOffset, 2)))));
                            } else {
                                break;
                            }
                            canvas.drawCircle(cx, cy, radius, selectedPaint);
                            selectedPaint.setAlpha(alpha);
                        }
                    }
                    break;
                }
            }
        }
    }

    public void setIndicatorCount(int count) {
        mIndicatorCount = count;
        invalidate();
    }

    public void setCurrentIndicatorPosition(int i) {
        mCurrentPosition = i;
        invalidate();
    }

    public void setOffset(float v) {
        if (v != Float.NaN) {
            if (v == 0) {
                mOffset = v;
            } else if (v > 0) {
                mOffset = Math.min(1, v);
            } else if (v < 0) {
                mOffset = Math.max(-1, v);
            }
            invalidate();
        }
    }

    public enum Animation {
        SLIDE,
        FADE,
        GROW
    }
}