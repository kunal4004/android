package za.co.woolworths.financial.services.android.ui.views;

/*
 * Copyright (C) 2016 Sutachad Wichai
 * Copyright (C) 2016 layer
 * source:https://github.com/layerlre/StepIndicator
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

        import android.annotation.SuppressLint;
        import android.annotation.TargetApi;
        import android.content.Context;
        import android.content.res.TypedArray;
        import android.graphics.Canvas;
        import android.graphics.Color;
        import android.graphics.Paint;
        import android.graphics.Rect;
        import android.graphics.Typeface;
        import android.os.Build;
        import android.os.Parcel;
        import android.os.Parcelable;
        import android.support.annotation.NonNull;
        import android.support.v4.content.ContextCompat;
        import android.support.v4.view.PagerAdapter;
        import android.support.v4.view.ViewPager;
        import android.util.AttributeSet;
        import android.util.DisplayMetrics;
        import android.view.MotionEvent;
        import android.view.View;
        import com.awfs.coordination.R;

public class StepIndicator extends View {

    private static final int DEFAULT_STEP_RADIUS = 11;   //DP
    private static final int DEFAULT_STOKE_WIDTH = 0;  //DP
    private static final int DEFAULT_STEP_COUNT = 4;  //DP
    private static final int DEFAULT_VIEW=1;
    private static final int DEFAULT_BACKGROUND_COLOR = R.color.background_default;
    private static final int DEFAULT_STEP_COLOR = R.color.step_default;
    private static final int DEFAULT_CURRENT_STEP_COLOR = R.color.text_default;
    private static final int DEFAULT_TEXT_COLOR = R.color.recent_search_bg;
    private static final int DEFAULT_SECONDARY_TEXT_COLOR = R.color.text_default;

    private int radius;
    private int strokeWidth;
    private int currentStepPosition;
    private int stepsCount = 1;
    private int backgroundColor;
    private int stepColor;
    private int currentColor;
    private int textColor;
    private int secondaryTextColor;

    private int centerY;
    private int startX;
    private int endX;
    private int stepDistance;
    private float offset;
    private int offsetPixel;
    private int pagerScrollState;

    private Paint paint;
    private Paint pStoke;
    private Paint pText;
    private final Rect textBounds = new Rect();
    private OnClickListener onClickListener;
    private float[] hsvCurrent = new float[3];
    private float[] hsvBG = new float[3];
    private float[] hsvProgress = new float[3];

    private boolean withViewpager;
    private int defaultView=1;

    public StepIndicator(Context context) {
        super(context);
        init(context, null);
    }

    public StepIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public StepIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public StepIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }


    public interface OnClickListener {
        void onClick(int position);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    private void init(Context context, AttributeSet attributeSet) {

        initAttributes(context, attributeSet);

        paint = new Paint();
        pStoke = new Paint();
        pText = new Paint();
        paint.setColor(stepColor);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(radius * 80 / 100);
        pStoke.setColor(stepColor);
        pStoke.setStrokeWidth(strokeWidth);
        pStoke.setStyle(Paint.Style.STROKE);
        pStoke.setFlags(Paint.ANTI_ALIAS_FLAG);
        pText.setColor(textColor);
        pText.setTextSize(radius);
        pText.setAlpha(1);
        pText.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        pText.setTextAlign(Paint.Align.CENTER);
        pText.setFlags(Paint.ANTI_ALIAS_FLAG);
        setMinimumHeight(radius * 3);
        Color.colorToHSV(currentColor, hsvCurrent);
        Color.colorToHSV(backgroundColor, hsvBG);
        Color.colorToHSV(stepColor, hsvProgress);
        invalidate();
    }

    private void initAttributes(Context context, AttributeSet attributeSet) {
        TypedArray attr = context.obtainStyledAttributes(attributeSet, R.styleable.StepIndicator, 0, 0);
        if (attr == null) {
            return;
        }

        try {
            radius = (int) attr.getDimension(R.styleable.StepIndicator_siRadius, dp2px(DEFAULT_STEP_RADIUS));
            strokeWidth = (int) attr.getDimension(R.styleable.StepIndicator_siStrokeWidth, dp2px(DEFAULT_STOKE_WIDTH));
            stepsCount = attr.getInt(R.styleable.StepIndicator_siStepCount, DEFAULT_STEP_COUNT);
            defaultView = attr.getInt(R.styleable.StepIndicator_siDefaultBackground, DEFAULT_VIEW);
            stepColor = attr.getColor(R.styleable.StepIndicator_siStepColor, ContextCompat.getColor(context, DEFAULT_STEP_COLOR));
            currentColor = attr.getColor(R.styleable.StepIndicator_siCurrentStepColor, ContextCompat.getColor(context, DEFAULT_CURRENT_STEP_COLOR));
            backgroundColor = attr.getColor(R.styleable.StepIndicator_siBackgroundColor, ContextCompat.getColor(context, DEFAULT_BACKGROUND_COLOR));
            textColor = attr.getColor(R.styleable.StepIndicator_siTextColor, ContextCompat.getColor(context, DEFAULT_TEXT_COLOR));
            secondaryTextColor = attr.getColor(R.styleable.StepIndicator_siSecondaryTextColor, ContextCompat.getColor(context, DEFAULT_SECONDARY_TEXT_COLOR));
        } finally {
            attr.recycle();
        }
    }

    @SuppressLint("NewApi")
    protected float dp2px(float dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public int getStepsCount() {
        return stepsCount;
    }

    public void setStepsCount(int stepsCount) {
        this.stepsCount = stepsCount;
        invalidate();
    }

    public int getCurrentStepPosition() {
        return currentStepPosition;
    }

    public void setCurrentStepPosition(int currentStepPosition) {
        this.currentStepPosition = currentStepPosition;
        invalidate();
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setupWithViewPager(@NonNull ViewPager viewPager) {
        final PagerAdapter adapter = viewPager.getAdapter();
        if (adapter == null) {
            throw new IllegalArgumentException("ViewPager does not have a PagerAdapter set");
        }
        withViewpager = true;
        // First we'll add Steps.
        setStepsCount(adapter.getCount());

        // Now we'll add our page change listener to the ViewPager
        viewPager.addOnPageChangeListener(new ViewPagerOnChangeListener(this));

        // Now we'll add a selected listener to set ViewPager's currentStepPosition item
        setOnClickListener(new ViewPagerOnSelectedListener(viewPager));

        // Make sure we reflect the currently set ViewPager item
        if (adapter.getCount() > 0) {
            final int curItem = viewPager.getCurrentItem();
            if (getCurrentStepPosition() != curItem) {
                setCurrentStepPosition(curItem);
                invalidate();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (stepsCount <= 1) {
            setVisibility(GONE);
            return;
        }
        super.onDraw(canvas);
        int pointX = startX;
        int pointOffset;

        /** draw Line */
        for (int i = 0; i < stepsCount - 1; i++) {
            if (i < currentStepPosition) {
                paint.setColor(stepColor);
                canvas.drawLine(pointX, centerY, pointX + stepDistance, centerY, paint);
            } else if (i == currentStepPosition) {
                paint.setColor(backgroundColor);
                canvas.drawLine(pointX, centerY, pointX + stepDistance, centerY, paint);
            } else {
                paint.setColor(backgroundColor);
                canvas.drawLine(pointX, centerY, pointX + stepDistance, centerY, paint);
            }
            paint.setStrokeWidth(10);
            pointX = pointX + stepDistance;
        }

        /**draw progress Line  */
        if (offsetPixel != 0 && pagerScrollState == 1) {
            pointOffset = startX + (currentStepPosition * stepDistance);
            int drawOffset = pointOffset + offsetPixel;
            if (drawOffset >= startX && drawOffset <= endX) {
                if (offsetPixel < 0) {
                    paint.setColor(backgroundColor);
                } else {
                    paint.setColor(stepColor);
                }
                canvas.drawLine(pointOffset, centerY, drawOffset, centerY, paint);
            }
        }

        /**draw Circle */
        pointX = startX;
        for (int i = 0; i < stepsCount; i++) {
            if (i < currentStepPosition) {
                //draw previous step
                paint.setColor(stepColor);
                canvas.drawCircle(pointX, centerY, radius, paint);

                //draw transition
                if (i == currentStepPosition - 1 && offsetPixel < 0 && pagerScrollState == 1) {
                    pStoke.setAlpha(255);
                    pStoke.setStrokeWidth(strokeWidth - Math.round(strokeWidth * offset));
                    canvas.drawCircle(pointX, centerY, radius, pStoke);
                }
                pText.setColor(Color.WHITE);
            } else if (i == currentStepPosition) {
                //draw current step
                if (offsetPixel == 0 || pagerScrollState == 0) {
                    //set stroke default
                    if(defaultView==1) {
                        paint.setColor(currentColor);
                        pStoke.setStrokeWidth(Math.round(strokeWidth));
                        pStoke.setAlpha(255);
                    }
                } else if (offsetPixel < 0) {
                    pStoke.setStrokeWidth(Math.round(strokeWidth * offset));
                    pStoke.setAlpha(Math.round(offset * 255f));
                    paint.setColor(getColorToBG(offset));
                } else {
                    //set stroke transition
                    paint.setColor(getColorToProgess(offset));
                    pStoke.setStrokeWidth(strokeWidth - Math.round(strokeWidth * offset));
                    pStoke.setAlpha(255 - Math.round(offset * 255f));
                }
                canvas.drawCircle(pointX, centerY, radius, paint);
                canvas.drawCircle(pointX, centerY, radius, pStoke);
                pText.setColor(Color.WHITE);
            } else {
                //draw next step
                paint.setColor(backgroundColor);
                canvas.drawCircle(pointX, centerY, radius, paint);
                pText.setColor(Color.TRANSPARENT);

                //draw transition
                if (i == currentStepPosition + 1 && offsetPixel > 0 && pagerScrollState == 1) {
                    pStoke.setStrokeWidth(Math.round(strokeWidth * offset));
                    pStoke.setAlpha(Math.round(offset * 255f));
                    canvas.drawCircle(pointX, centerY, radius, pStoke);
                }
            }

            if (defaultView==0){
                //all grey
                paint.setColor(backgroundColor);
                pStoke.setColor(Color.TRANSPARENT);
                pText.setColor(Color.TRANSPARENT);
                canvas.drawCircle(pointX, centerY, radius, paint);

            }
            drawTextCentred(canvas, pText, String.valueOf(i + 1), pointX, centerY);
            pointX = pointX + stepDistance;
        }

    }

    private void drawTextCentred(Canvas canvas, Paint paint, String text, float cx, float cy) {
        paint.getTextBounds(text, 0, text.length(), textBounds);
        Typeface tf =Typeface.createFromAsset(this.getResources().getAssets(),"fonts/WFutura-SemiBold.ttf");
        paint.setTypeface(tf);
        canvas.drawText(text, cx, cy - textBounds.exactCenterY(), paint);
    }

    private int getColorToBG(float offset) {
        offset = Math.abs(offset);
        float[] hsv = new float[3];
        hsv[0] = hsvBG[0] + (hsvCurrent[0] - hsvBG[0]) * offset;
        hsv[1] = hsvBG[1] + (hsvCurrent[1] - hsvBG[1]) * offset;
        hsv[2] = hsvBG[2] + (hsvCurrent[2] - hsvBG[2]) * offset;
        return Color.HSVToColor(hsv);
    }

    private int getColorToProgess(float offset) {
        offset = Math.abs(offset);
        float[] hsv = new float[3];
        hsv[0] = hsvCurrent[0] + (hsvProgress[0] - hsvCurrent[0]) * offset;
        hsv[1] = hsvCurrent[1] + (hsvProgress[1] - hsvCurrent[1]) * offset;
        hsv[2] = hsvCurrent[2] + (hsvProgress[2] - hsvCurrent[2]) * offset;
        return Color.HSVToColor(hsv);
    }

    private void setOffset(float offset, int position) {
        this.offset = offset;
        offsetPixel = Math.round(stepDistance * offset);
        if (currentStepPosition > position) {
            offsetPixel = offsetPixel - stepDistance;
        }else {
            currentStepPosition = position;
        }

        invalidate();
    }

    private void setPagerScrollState(int pagerScrollState) {
        this.pagerScrollState = pagerScrollState;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int pointX = startX;
        int xTouch;
        int yTouch;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                xTouch = (int) event.getX(0);
                yTouch = (int) event.getY(0);
                for (int i = 0; i < stepsCount; i++) {
                    if (Math.abs(xTouch - pointX) < radius + 5 && Math.abs(yTouch - centerY) < radius + 5) {
                        if (!withViewpager) {
                            setCurrentStepPosition(i);
                        }

                        if (onClickListener != null) {
                            onClickListener.onClick(i);
                        }
                    }
                    pointX = pointX + stepDistance;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, radius * 3);
        centerY = getHeight() / 2;
        startX = radius * 2;
        endX = getWidth() - (radius * 2);
        stepDistance = (endX - startX) / (stepsCount - 1);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerY = getHeight() / 2;
        startX = radius * 2;
        endX = getWidth() - (radius * 2);
        stepDistance = (endX - startX) / (stepsCount - 1);
        invalidate();
    }

    public static class ViewPagerOnChangeListener implements ViewPager.OnPageChangeListener {
        private final StepIndicator stepIndicator;

        public ViewPagerOnChangeListener(StepIndicator stepIndicator) {
            this.stepIndicator = stepIndicator;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            stepIndicator.setOffset(positionOffset, position);
        }

        @Override
        public void onPageSelected(int position) {
            stepIndicator.setCurrentStepPosition(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            stepIndicator.setPagerScrollState(state);
        }

    }

    public static class ViewPagerOnSelectedListener implements OnClickListener {
        private final ViewPager mViewPager;

        public ViewPagerOnSelectedListener(ViewPager viewPager) {
            mViewPager = viewPager;
        }

        @Override
        public void onClick(int position) {
            mViewPager.setCurrentItem(position);
        }
    }


    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.radius = this.radius;
        ss.strokeWidth = this.strokeWidth;
        ss.currentStepPosition = this.currentStepPosition;
        ss.stepsCount = this.stepsCount;
        ss.backgroundColor = this.backgroundColor;
        ss.stepColor = this.stepColor;
        ss.currentColor = this.currentColor;
        ss.textColor = this.textColor;
        ss.secondaryTextColor = this.secondaryTextColor;
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        this.radius = ss.radius;
        this.strokeWidth = ss.strokeWidth;
        this.currentStepPosition = ss.currentStepPosition;
        this.stepsCount = ss.stepsCount;
        this.backgroundColor = ss.backgroundColor;
        this.stepColor = ss.stepColor;
        this.currentColor = ss.currentColor;
        this.textColor = ss.textColor;
        this.secondaryTextColor = ss.secondaryTextColor;
    }

    static class SavedState extends BaseSavedState {
        int radius;
        int strokeWidth;
        int currentStepPosition;
        int stepsCount;
        int backgroundColor;
        int stepColor;
        int currentColor;
        int textColor;
        int secondaryTextColor;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            radius = in.readInt();
            strokeWidth = in.readInt();
            currentStepPosition = in.readInt();
            stepsCount = in.readInt();
            backgroundColor = in.readInt();
            stepColor = in.readInt();
            currentColor = in.readInt();
            textColor = in.readInt();
            secondaryTextColor = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(radius);
            dest.writeInt(strokeWidth);
            dest.writeInt(currentStepPosition);
            dest.writeInt(stepsCount);
            dest.writeInt(backgroundColor);
            dest.writeInt(stepColor);
            dest.writeInt(currentColor);
            dest.writeInt(textColor);
            dest.writeInt(secondaryTextColor);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

    }

    public int getDefaultView() {
        return defaultView;
    }

    public void setDefaultView(int defaultView) {
        this.defaultView = defaultView;
        invalidate();
    }

    public void setOtherState(){
        setCurrentStepPosition(0);
        invalidate();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev){
        return true;//consume
    }

}