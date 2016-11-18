package za.co.woolworths.financial.services.android.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.awfs.coordination.R;

import za.co.wigroup.androidutils.Util;

public class WBalanceView extends View {
    private int mHeight, mWidth, mOuterRadius,
            mOuterCircleColor = Color.GRAY, mOuterWidth = Util.dpToPx(15),
            mSpentEmptyColor = Color.RED, mSpentFullColor = Color.BLACK,
            mMiddleCircleWidth = Util.dpToPx(15), mCenterCircleColor = Color.WHITE,
            mTextColor = Color.BLACK, mLabelTextSize = Util.dpToPx(10), mAmountTextSize = Util.dpToPx(22);
    private float mPercentSpent = 37.5f;
    private long mAmountInCents = 0l;
    private String mCurrency = "R", mThousandSeparator = " ", mDecimalSeparator = ".", mLabel = "CURRENT BALANCE";
    private Typeface mTypeface = Typeface.DEFAULT;


    public WBalanceView(Context context) {
        super(context);
    }

    public WBalanceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public WBalanceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(21)
    public WBalanceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.WBalanceView,
                0, 0);

        try {
            mAmountTextSize = a.getDimensionPixelSize(R.styleable.WBalanceView_AmountTextSize, 65);
            mCenterCircleColor = a.getColor(R.styleable.WBalanceView_CenterCircleColor, Color.WHITE);
            mLabelTextSize = a.getDimensionPixelSize(R.styleable.WBalanceView_LabelTextSize, 30);
            mMiddleCircleWidth = a.getDimensionPixelSize(R.styleable.WBalanceView_MiddleCircleWidth, Util.dpToPx(10));
            mOuterWidth = a.getDimensionPixelSize(R.styleable.WBalanceView_OuterCircleWidth, Util.dpToPx(10));
            mOuterCircleColor = a.getColor(R.styleable.WBalanceView_OuterCircleColor, Color.GRAY);
            mSpentEmptyColor = a.getColor(R.styleable.WBalanceView_SpentEmptyColor, Color.RED);
            mSpentFullColor = a.getColor(R.styleable.WBalanceView_SpentFullColor, Color.BLACK);
            mTextColor = a.getColor(R.styleable.WBalanceView_TextColor, Color.BLACK);
            String string = a.getString(R.styleable.WBalanceView_Font);
            if (string != null) {
                mTypeface = Typeface.createFromAsset(getContext().getAssets(), string);
            }
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHeight = h;
        mWidth = w;
        int padding = (mWidth > mHeight) ? (getPaddingTop() + getPaddingBottom()) : (getPaddingLeft() + getPaddingRight());
        mOuterRadius = (h <= w) ? (h / 2) - padding : (w / 2) - padding;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(mOuterCircleColor);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        int cx = mWidth / 2;
        int cy = mHeight / 2;
        canvas.drawCircle(cx, cy, mOuterRadius, paint);
        paint.setColor(mSpentEmptyColor);
        canvas.drawCircle(cx, cy, mOuterRadius - mOuterWidth, paint);
        RectF rectF = new RectF((cx - mOuterRadius) + mOuterWidth, (cy - mOuterRadius) + mOuterWidth, (cx + mOuterRadius) - mOuterWidth, (cy + mOuterRadius) - mOuterWidth);
        paint.setColor(mSpentFullColor);
        canvas.drawArc(rectF, 270, (3.6f * mPercentSpent), true, paint);
        paint.setColor(mCenterCircleColor);
        canvas.drawCircle(cx, cy, mOuterRadius - (mOuterWidth + mMiddleCircleWidth), paint);
        paint.setColor(mTextColor);
        paint.setTypeface(mTypeface);
        paint.setTextSize(mAmountTextSize);
        paint.setTextAlign(Paint.Align.CENTER);
        Rect bounds = new Rect();
        String amountText = getAmountText();
        paint.getTextBounds(amountText, 0, amountText.length(), bounds);
        canvas.drawText(amountText, cx, cy + (bounds.height() / 2), paint);
        paint.setTextSize(mLabelTextSize);
        canvas.drawText(mLabel, cx, cy - ((bounds.height() / 2) * 3), paint);
    }

    private String getAmountText() {
        StringBuilder stringBuilder = new StringBuilder();
        String[] split = String.valueOf(((int)Math.sqrt(Math.pow(mAmountInCents, 2))) / 100).split("");
        int c = 0;
        for (int i = split.length - 1; i > 0; i--) {
            stringBuilder.append(split[i]);
            c++;
            if (c % 3 == 0) {
                stringBuilder.append(mThousandSeparator);
            }
        }
        return String.format("%s%s%s%02d %s", mCurrency, stringBuilder.reverse().toString().trim(), mDecimalSeparator, (int) Math.sqrt(Math.pow(mAmountInCents, 2)) % 100l, (mAmountInCents < 0) ? "CR" : "");
    }

    public void setPercentSpent(float percentSpent) {
        mPercentSpent = percentSpent;
        if (mPercentSpent < 0) {
            mPercentSpent = 0;
        } else if (mPercentSpent > 100) {
            mPercentSpent = 100;
        }

        invalidate();
    }

    public void setAmountInCents(long mAmountInCents) {
        this.mAmountInCents = mAmountInCents;
        invalidate();
    }
}
