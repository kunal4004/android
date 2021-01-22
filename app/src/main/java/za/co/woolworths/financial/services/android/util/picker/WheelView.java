package za.co.woolworths.financial.services.android.util.picker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.FloatRange;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.awfs.coordination.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author zyyoona7
 * @version v1.0.1
 * @since 2018/8/7.
 */
public class WheelView<T> extends View implements Runnable {

    private static final String TAG = "WheelView";

    private static final float DEFAULT_LINE_SPACING = dp2px(2f);
    private static final float DEFAULT_TEXT_SIZE = sp2px(15f);
    private static final float DEFAULT_TEXT_BOUNDARY_MARGIN = dp2px(2);
    private static final float DEFAULT_DIVIDER_HEIGHT = dp2px(1);
    private static final int DEFAULT_NORMAL_TEXT_COLOR = Color.DKGRAY;
    private static final int DEFAULT_SELECTED_TEXT_COLOR = Color.BLACK;
    private static final int DEFAULT_VISIBLE_ITEM = 5;
    private static final int DEFAULT_SCROLL_DURATION = 250;
    private static final long DEFAULT_CLICK_CONFIRM = 120;
    private static final String DEFAULT_INTEGER_FORMAT = "%02d";
    //默认折射比值，通过字体大小来实现折射视觉差
    private static final float DEFAULT_REFRACT_RATIO = 1f;

    //文字对齐方式
    public static final int TEXT_ALIGN_LEFT = 0;
    public static final int TEXT_ALIGN_CENTER = 1;
    public static final int TEXT_ALIGN_RIGHT = 2;

    //滚动状态
    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_DRAGGING = 1;
    public static final int SCROLL_STATE_SCROLLING = 2;

    //弯曲效果对齐方式
    public static final int CURVED_ARC_DIRECTION_LEFT = 0;
    public static final int CURVED_ARC_DIRECTION_CENTER = 1;
    public static final int CURVED_ARC_DIRECTION_RIGHT = 2;

    public static final float DEFAULT_CURVED_FACTOR = 0.75f;

    //分割线填充类型
    public static final int DIVIDER_TYPE_FILL = 0;
    public static final int DIVIDER_TYPE_WRAP = 1;

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    //字体大小
    private float mTextSize;
    //是否自动调整字体大小以显示完全
    private boolean isAutoFitTextSize;
    private Paint.FontMetrics mFontMetrics;
    //每个item的高度
    private int mItemHeight;
    //文字的最大宽度
    private int mMaxTextWidth;
    //文字中心距离baseline的距离
    private int mCenterToBaselineY;
    //可见的item条数
    private int mVisibleItems;
    //每个item之间的空间，行间距
    private float mLineSpacing;
    //是否循环滚动
    private boolean isCyclic;
    //文字对齐方式
    @TextAlign
    private int mTextAlign;
    //文字颜色
    private int mTextColor;
    //选中item文字颜色
    private int mSelectedItemTextColor;

    //是否显示分割线
    private boolean isShowDivider;
    //分割线的颜色
    private int mDividerColor;
    //分割线高度
    private float mDividerSize;
    //分割线填充类型
    @DividerType
    private int mDividerType;
    //分割线类型为DIVIDER_TYPE_WRAP时 分割线左右两端距离文字的间距
    private float mDividerPaddingForWrap;
    //分割线两端形状，默认圆头
    private Paint.Cap mDividerCap = Paint.Cap.ROUND;
    //分割线和选中区域偏移，实现扩大选中区域
    private float mDividerOffset;

    //是否绘制选中区域
    private boolean isDrawSelectedRect;
    //选中区域颜色
    private int mSelectedRectColor;

    //文字起始X
    private int mStartX;
    //X轴中心点
    private int mCenterX;
    //Y轴中心点
    private int mCenterY;
    //选中边界的上下限制
    private int mSelectedItemTopLimit;
    private int mSelectedItemBottomLimit;
    //裁剪边界
    private int mClipLeft;
    private int mClipTop;
    private int mClipRight;
    private int mClipBottom;
    //绘制区域
    private Rect mDrawRect;
    //字体外边距，目的是留有边距
    private float mTextBoundaryMargin;
    //数据为Integer类型时，是否需要格式转换
    private boolean isIntegerNeedFormat;
    //数据为Integer类型时，转换格式，默认转换为两位数
    private String mIntegerFormat;

    //3D效果
    private Camera mCamera;
    private Matrix mMatrix;
    //是否是弯曲（3D）效果
    private boolean isCurved;
    //弯曲（3D）效果左右圆弧偏移效果方向 center 不偏移
    @CurvedArcDirection
    private int mCurvedArcDirection;
    //弯曲（3D）效果左右圆弧偏移效果系数 0-1之间 越大越明显
    private float mCurvedArcDirectionFactor;
    //选中后折射的偏移 与字体大小的比值，1为不偏移 越小偏移越明显
    //(普通效果和3d效果都适用)
    private float mRefractRatio;

    //数据列表
    @NonNull
    private List<T> mDataList = new ArrayList<>(1);
    //数据变化时，是否重置选中下标到第一个位置
    private boolean isResetSelectedPosition = false;

    private VelocityTracker mVelocityTracker;
    private int mMaxFlingVelocity;
    private int mMinFlingVelocity;
    private Scroller mScroller;

    //最小滚动距离，上边界
    private int mMinScrollY;
    //最大滚动距离，下边界
    private int mMaxScrollY;

    //Y轴滚动偏移
    private int mScrollOffsetY;
    //Y轴已滚动偏移，控制重绘次数
    private int mScrolledY = 0;
    //手指最后触摸的位置
    private float mLastTouchY;
    //手指按下时间，根据按下抬起时间差处理点击滚动
    private long mDownStartTime;
    //是否强制停止滚动
    private boolean isForceFinishScroll = false;
    //是否是快速滚动，快速滚动结束后跳转位置
    private boolean isFlingScroll;
    //当前选中的下标
    private int mSelectedItemPosition;
    //当前滚动经过的下标
    private int mCurrentScrollPosition;

    //字体
    private boolean mIsBoldForSelectedItem = true;
    //如果 mIsBoldForSelectedItem==true 则这个字体为未选中条目的字体
    private Typeface mNormalTypeface = null;
    //如果 mIsBoldForSelectedItem==true 则这个字体为选中条目的字体
    private Typeface mBoldTypeface = null;

    //监听器
    private OnItemSelectedListener<T> mOnItemSelectedListener;
    private OnWheelChangedListener mOnWheelChangedListener;

    //音频
    private SoundHelper mSoundHelper;
    //是否开启音频效果
    private boolean isSoundEffect = false;

    private Typeface mTextFontStyle;

    private Typeface mSelectedTextFontStyle;

    public WheelView(Context context) {
        this(context, null);
    }

    public WheelView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WheelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrsAndDefault(context, attrs);
        initValue(context);
    }

    /**
     * 初始化自定义属性及默认值
     *
     * @param context 上下文
     * @param attrs   attrs
     */
    private void initAttrsAndDefault(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.WheelView);
        mTextSize = typedArray.getDimension(R.styleable.WheelView_wv_textSize, DEFAULT_TEXT_SIZE);
        isAutoFitTextSize = typedArray.getBoolean(R.styleable.WheelView_wv_autoFitTextSize, false);
        mTextAlign = typedArray.getInt(R.styleable.WheelView_wv_textAlign, TEXT_ALIGN_CENTER);
        mTextBoundaryMargin = typedArray.getDimension(R.styleable.WheelView_wv_textBoundaryMargin,
                DEFAULT_TEXT_BOUNDARY_MARGIN);
        mTextColor = typedArray.getColor(R.styleable.WheelView_wv_normalItemTextColor, DEFAULT_NORMAL_TEXT_COLOR);
        mSelectedItemTextColor = typedArray.getColor(R.styleable.WheelView_wv_selectedItemTextColor, DEFAULT_SELECTED_TEXT_COLOR);
        mLineSpacing = typedArray.getDimension(R.styleable.WheelView_wv_lineSpacing, DEFAULT_LINE_SPACING);
        isIntegerNeedFormat = typedArray.getBoolean(R.styleable.WheelView_wv_integerNeedFormat, false);
        mIntegerFormat = typedArray.getString(R.styleable.WheelView_wv_integerFormat);
        if (TextUtils.isEmpty(mIntegerFormat)) {
            mIntegerFormat = DEFAULT_INTEGER_FORMAT;
        }

        mVisibleItems = typedArray.getInt(R.styleable.WheelView_wv_visibleItems, DEFAULT_VISIBLE_ITEM);
        mVisibleItems = adjustVisibleItems(mVisibleItems);
        mSelectedItemPosition = typedArray.getInt(R.styleable.WheelView_wv_selectedItemPosition, 0);

        mCurrentScrollPosition = mSelectedItemPosition;
        isCyclic = typedArray.getBoolean(R.styleable.WheelView_wv_cyclic, false);

        isShowDivider = typedArray.getBoolean(R.styleable.WheelView_wv_showDivider, false);
        mDividerType = typedArray.getInt(R.styleable.WheelView_wv_dividerType, DIVIDER_TYPE_FILL);
        mDividerSize = typedArray.getDimension(R.styleable.WheelView_wv_dividerHeight, DEFAULT_DIVIDER_HEIGHT);
        mDividerColor = typedArray.getColor(R.styleable.WheelView_wv_dividerColor, DEFAULT_SELECTED_TEXT_COLOR);
        mDividerPaddingForWrap = typedArray.getDimension(R.styleable.WheelView_wv_dividerPaddingForWrap, DEFAULT_TEXT_BOUNDARY_MARGIN);

        mDividerOffset = typedArray.getDimensionPixelOffset(R.styleable.WheelView_wv_dividerOffset, 0);

        isDrawSelectedRect = typedArray.getBoolean(R.styleable.WheelView_wv_drawSelectedRect, false);
        mSelectedRectColor = typedArray.getColor(R.styleable.WheelView_wv_selectedRectColor, Color.TRANSPARENT);

        isCurved = typedArray.getBoolean(R.styleable.WheelView_wv_curved, true);
        mCurvedArcDirection = typedArray.getInt(R.styleable.WheelView_wv_curvedArcDirection, CURVED_ARC_DIRECTION_CENTER);
        mCurvedArcDirectionFactor = typedArray.getFloat(R.styleable.WheelView_wv_curvedArcDirectionFactor, DEFAULT_CURVED_FACTOR);

        float curvedRefractRatio = typedArray.getFloat(R.styleable.WheelView_wv_curvedRefractRatio, 0.9f);
        mRefractRatio = typedArray.getFloat(R.styleable.WheelView_wv_refractRatio, DEFAULT_REFRACT_RATIO);
        mRefractRatio = isCurved ? Math.min(curvedRefractRatio, mRefractRatio) : mRefractRatio;
        if (mRefractRatio > 1f) {
            mRefractRatio = 1.0f;
        } else if (mRefractRatio < 0f) {
            mRefractRatio = DEFAULT_REFRACT_RATIO;
        }

        mTextFontStyle =Typeface.createFromAsset(context.getAssets(), context.getResources().getStringArray(R.array.fonts)[typedArray.getInt(R.styleable.WheelView_wv_textFont, 1)]);
        mSelectedTextFontStyle =Typeface.createFromAsset(context.getAssets(), context.getResources().getStringArray(R.array.fonts)[typedArray.getInt(R.styleable.WheelView_wv_selectedTextFont, 1)]);

        typedArray.recycle();
    }


    private void initValue(Context context) {
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        mMaxFlingVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        mMinFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        mScroller = new Scroller(context);
        mDrawRect = new Rect();
        mCamera = new Camera();
        mMatrix = new Matrix();
        if (!isInEditMode()) {
            mSoundHelper = SoundHelper.obtain();
            initDefaultVolume(context);
        }
        calculateTextSize();
        updateTextAlign();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mSoundHelper != null) {
            mSoundHelper.release();
        }
    }


    private void initDefaultVolume(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            mSoundHelper.setPlayVolume(currentVolume * 1.0f / maxVolume);
        } else {
            mSoundHelper.setPlayVolume(0.3f);
        }
    }


    private void calculateTextSize() {
        mPaint.setTextSize(mTextSize);
        for (int i = 0; i < mDataList.size(); i++) {
            int textWidth = (int) mPaint.measureText(getDataText(mDataList.get(i)));
            mMaxTextWidth = Math.max(textWidth, mMaxTextWidth);
        }

        mFontMetrics = mPaint.getFontMetrics();
        mItemHeight = (int) (mFontMetrics.bottom - mFontMetrics.top + mLineSpacing);
    }

    private void updateTextAlign() {
        switch (mTextAlign) {
            case TEXT_ALIGN_LEFT:
                mPaint.setTextAlign(Paint.Align.LEFT);
                break;
            case TEXT_ALIGN_RIGHT:
                mPaint.setTextAlign(Paint.Align.RIGHT);
                break;
            case TEXT_ALIGN_CENTER:
            default:
                mPaint.setTextAlign(Paint.Align.CENTER);
                break;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height;
        if (isCurved) {
            height = (int) ((mItemHeight * mVisibleItems * 2 / Math.PI) + getPaddingTop() + getPaddingBottom());
        } else {
            height = mItemHeight * mVisibleItems + getPaddingTop() + getPaddingBottom();
        }
        int width = (int) (mMaxTextWidth + getPaddingLeft() + getPaddingRight() + mTextBoundaryMargin * 2);
        if (isCurved) {
            int towardRange = (int) (Math.sin(Math.PI / 48) * height);
            width += towardRange;
        }
        setMeasuredDimension(resolveSizeAndState(width, widthMeasureSpec, 0),
                resolveSizeAndState(height, heightMeasureSpec, 0));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mDrawRect.set(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
        mCenterX = mDrawRect.centerX();
        mCenterY = mDrawRect.centerY();
        mSelectedItemTopLimit = (int) (mCenterY - mItemHeight / 2 - mDividerOffset);
        mSelectedItemBottomLimit = (int) (mCenterY + mItemHeight / 2 + mDividerOffset);
        mClipLeft = getPaddingLeft();
        mClipTop = getPaddingTop();
        mClipRight = getWidth() - getPaddingRight();
        mClipBottom = getHeight() - getPaddingBottom();

        calculateDrawStart();
        calculateLimitY();

        int itemDistance = calculateItemDistance(mSelectedItemPosition);
        if (itemDistance > 0) {
            doScroll(itemDistance);
        }
    }

    private void calculateDrawStart() {
        switch (mTextAlign) {
            case TEXT_ALIGN_LEFT:
                mStartX = (int) (getPaddingLeft() + mTextBoundaryMargin);
                break;
            case TEXT_ALIGN_RIGHT:
                mStartX = (int) (getWidth() - getPaddingRight() - mTextBoundaryMargin);
                break;
            case TEXT_ALIGN_CENTER:
            default:
                mStartX = getWidth() / 2;
                break;
        }

        mCenterToBaselineY = (int) (mFontMetrics.ascent + (mFontMetrics.descent - mFontMetrics.ascent) / 2);
    }


    private void calculateLimitY() {
        mMinScrollY = isCyclic ? Integer.MIN_VALUE : 0;
        mMaxScrollY = isCyclic ? Integer.MAX_VALUE : (mDataList.size() - 1) * mItemHeight;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawSelectedRect(canvas);
        drawDivider(canvas);

        int scrolledItem = mScrollOffsetY / dividedItemHeight();
        int scrolledOffset = mScrollOffsetY % dividedItemHeight();
        int halfItem = (mVisibleItems + 1) / 2;
        int minIndex;
        int maxIndex;
        if (scrolledOffset < 0) {
            minIndex = scrolledItem - halfItem - 1;
            maxIndex = scrolledItem + halfItem;
        } else if (scrolledOffset > 0) {
            minIndex = scrolledItem - halfItem;
            maxIndex = scrolledItem + halfItem + 1;
        } else {
            minIndex = scrolledItem - halfItem;
            maxIndex = scrolledItem + halfItem;
        }

        for (int i = minIndex; i < maxIndex; i++) {
            if (isCurved) {
                draw3DItem(canvas, i, scrolledOffset);
            } else {
                drawItem(canvas, i, scrolledOffset);
            }
        }

    }


    private void drawSelectedRect(Canvas canvas) {
        if (isDrawSelectedRect) {
            mPaint.setColor(mSelectedRectColor);
            canvas.drawRect(mClipLeft, mSelectedItemTopLimit, mClipRight, mSelectedItemBottomLimit, mPaint);
        }
    }


    private void drawDivider(Canvas canvas) {
        if (isShowDivider) {
            mPaint.setColor(mDividerColor);
            float originStrokeWidth = mPaint.getStrokeWidth();
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(mDividerSize);
            if (mDividerType == DIVIDER_TYPE_FILL) {
                canvas.drawLine(mClipLeft, mSelectedItemTopLimit, mClipRight, mSelectedItemTopLimit, mPaint);
                canvas.drawLine(mClipLeft, mSelectedItemBottomLimit, mClipRight, mSelectedItemBottomLimit, mPaint);
            } else {
                int startX = (int) (mCenterX - mMaxTextWidth / 2 - mDividerPaddingForWrap);
                int stopX = (int) (mCenterX + mMaxTextWidth / 2 + mDividerPaddingForWrap);

                int wrapStartX = startX < mClipLeft ? mClipLeft : startX;
                int wrapStopX = stopX > mClipRight ? mClipRight : stopX;
                canvas.drawLine(wrapStartX, mSelectedItemTopLimit, wrapStopX, mSelectedItemTopLimit, mPaint);
                canvas.drawLine(wrapStartX, mSelectedItemBottomLimit, wrapStopX, mSelectedItemBottomLimit, mPaint);
            }
            mPaint.setStrokeWidth(originStrokeWidth);
        }
    }


    private void drawItem(Canvas canvas, int index, int scrolledOffset) {
        String text = getDataByIndex(index);
        if (text == null) {
            return;
        }

        int item2CenterOffsetY = (index - mScrollOffsetY / dividedItemHeight()) * mItemHeight - scrolledOffset;
        int startX = mStartX;
        int centerToBaselineY = isAutoFitTextSize ? remeasureTextSize(text) : mCenterToBaselineY;

        if (Math.abs(item2CenterOffsetY) <= 0) {
            mPaint.setColor(mSelectedItemTextColor);
            clipAndDraw2DText(canvas, text, mSelectedItemTopLimit, mSelectedItemBottomLimit, item2CenterOffsetY, centerToBaselineY);
        } else if (item2CenterOffsetY > 0 && item2CenterOffsetY < mItemHeight) {
            mPaint.setColor(mSelectedItemTextColor);
            clipAndDraw2DText(canvas, text, mSelectedItemTopLimit, mSelectedItemBottomLimit, item2CenterOffsetY, centerToBaselineY);

            mPaint.setColor(mTextColor);
            float textSize = mPaint.getTextSize();
            mPaint.setTextSize(textSize * mRefractRatio);
            changeTypefaceIfBoldForSelectedItem();
            clipAndDraw2DText(canvas, text, mSelectedItemBottomLimit, mClipBottom, item2CenterOffsetY, centerToBaselineY);
            mPaint.setTextSize(textSize);
            resetTypefaceIfBoldForSelectedItem();
        } else if (item2CenterOffsetY < 0 && item2CenterOffsetY > -mItemHeight) {
            mPaint.setColor(mSelectedItemTextColor);
            clipAndDraw2DText(canvas, text, mSelectedItemTopLimit, mSelectedItemBottomLimit, item2CenterOffsetY, centerToBaselineY);

            mPaint.setColor(mTextColor);
            float textSize = mPaint.getTextSize();
            mPaint.setTextSize(textSize * mRefractRatio);
            changeTypefaceIfBoldForSelectedItem();
            clipAndDraw2DText(canvas, text, mClipTop, mSelectedItemTopLimit, item2CenterOffsetY, centerToBaselineY);
            mPaint.setTextSize(textSize);
            resetTypefaceIfBoldForSelectedItem();
        } else {
            mPaint.setColor(mTextColor);
            float textSize = mPaint.getTextSize();
            mPaint.setTextSize(textSize * mRefractRatio);
            changeTypefaceIfBoldForSelectedItem();
            clipAndDraw2DText(canvas, text, mClipTop, mClipBottom, item2CenterOffsetY, centerToBaselineY);
            mPaint.setTextSize(textSize);
            resetTypefaceIfBoldForSelectedItem();
        }

        if (isAutoFitTextSize) {
            mPaint.setTextSize(mTextSize);
            mStartX = startX;
        }
    }


    private void clipAndDraw2DText(Canvas canvas, String text, int clipTop, int clipBottom,
                                   int item2CenterOffsetY, int centerToBaselineY) {
        canvas.save();
        canvas.clipRect(mClipLeft, clipTop, mClipRight, clipBottom);
        canvas.drawText(text, 0, text.length(), mStartX, mCenterY + item2CenterOffsetY - centerToBaselineY, mPaint);
        canvas.restore();
    }


    private int remeasureTextSize(String contentText) {
        float textWidth = mPaint.measureText(contentText);
        float drawWidth = getWidth();
        float textMargin = mTextBoundaryMargin * 2;
        if (textMargin > (drawWidth / 10f)) {
            drawWidth = drawWidth * 9f / 10f;
            textMargin = drawWidth / 10f;
        } else {
            drawWidth = drawWidth - textMargin;
        }
        if (drawWidth <= 0) {
            return mCenterToBaselineY;
        }
        float textSize = mTextSize;
        while (textWidth > drawWidth) {
            textSize--;
            if (textSize <= 0) {
                break;
            }
            mPaint.setTextSize(textSize);
            textWidth = mPaint.measureText(contentText);
        }
        recalculateStartX(textMargin / 2.0f);
        return recalculateCenterToBaselineY();
    }


    private void recalculateStartX(float textMargin) {
        switch (mTextAlign) {
            case TEXT_ALIGN_LEFT:
                mStartX = (int) textMargin;
                break;
            case TEXT_ALIGN_RIGHT:
                mStartX = (int) (getWidth() - textMargin);
                break;
            case TEXT_ALIGN_CENTER:
            default:
                mStartX = getWidth() / 2;
                break;
        }
    }


    private int recalculateCenterToBaselineY() {
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        return (int) (fontMetrics.ascent + (fontMetrics.descent - fontMetrics.ascent) / 2);
    }


    private void draw3DItem(Canvas canvas, int index, int scrolledOffset) {
        String text = getDataByIndex(index);
        if (text == null) {
            return;
        }
        final int radius = (getHeight() - getPaddingTop() - getPaddingBottom()) / 2;
        int item2CenterOffsetY = (index - mScrollOffsetY / dividedItemHeight()) * mItemHeight - scrolledOffset;

        if (Math.abs(item2CenterOffsetY) > radius * Math.PI / 2) return;

        final double angle = (double) item2CenterOffsetY / radius;
        float rotateX = (float) Math.toDegrees(-angle);

        float translateY = (float) (Math.sin(angle) * radius);

        float translateZ = (float) ((1 - Math.cos(angle)) * radius);

        int alpha = (int) (Math.cos(angle) * 255);


        int startX = mStartX;

        int centerToBaselineY = isAutoFitTextSize ? remeasureTextSize(text) : mCenterToBaselineY;
        if (Math.abs(item2CenterOffsetY) <= 0) {

            mPaint.setColor(mSelectedItemTextColor);
            mPaint.setAlpha(255);
            clipAndDraw3DText(canvas, text, mSelectedItemTopLimit, mSelectedItemBottomLimit,
                    rotateX, translateY, translateZ, centerToBaselineY);
        } else if (item2CenterOffsetY > 0 && item2CenterOffsetY < mItemHeight) {

            mPaint.setColor(mSelectedItemTextColor);
            mPaint.setAlpha(255);
            clipAndDraw3DText(canvas, text, mSelectedItemTopLimit, mSelectedItemBottomLimit,
                    rotateX, translateY, translateZ, centerToBaselineY);

            mPaint.setColor(mTextColor);
            mPaint.setAlpha(alpha);

            float textSize = mPaint.getTextSize();
            mPaint.setTextSize(textSize * mRefractRatio);

            changeTypefaceIfBoldForSelectedItem();

            int reCenterToBaselineY = recalculateCenterToBaselineY();
            clipAndDraw3DText(canvas, text, mSelectedItemBottomLimit, mClipBottom,
                    rotateX, translateY, translateZ, reCenterToBaselineY);
            mPaint.setTextSize(textSize);

            resetTypefaceIfBoldForSelectedItem();
        } else if (item2CenterOffsetY < 0 && item2CenterOffsetY > -mItemHeight) {

            mPaint.setColor(mSelectedItemTextColor);
            mPaint.setAlpha(255);
            clipAndDraw3DText(canvas, text, mSelectedItemTopLimit, mSelectedItemBottomLimit,
                    rotateX, translateY, translateZ, centerToBaselineY);

            mPaint.setColor(mTextColor);
            mPaint.setAlpha(alpha);


            float textSize = mPaint.getTextSize();
            mPaint.setTextSize(textSize * mRefractRatio);

            changeTypefaceIfBoldForSelectedItem();

            int reCenterToBaselineY = recalculateCenterToBaselineY();
            clipAndDraw3DText(canvas, text, mClipTop, mSelectedItemTopLimit,
                    rotateX, translateY, translateZ, reCenterToBaselineY);
            mPaint.setTextSize(textSize);

            resetTypefaceIfBoldForSelectedItem();
        } else {

            mPaint.setColor(mTextColor);
            mPaint.setAlpha(alpha);


            float textSize = mPaint.getTextSize();
            mPaint.setTextSize(textSize * mRefractRatio);

            changeTypefaceIfBoldForSelectedItem();

            int reCenterToBaselineY = recalculateCenterToBaselineY();
            clipAndDraw3DText(canvas, text, mClipTop, mClipBottom,
                    rotateX, translateY, translateZ, reCenterToBaselineY);
            mPaint.setTextSize(textSize);

            resetTypefaceIfBoldForSelectedItem();
        }

        if (isAutoFitTextSize) {

            mPaint.setTextSize(mTextSize);
            mStartX = startX;
        }
    }


    private void clipAndDraw3DText(Canvas canvas, String text, int clipTop, int clipBottom,
                                   float rotateX, float offsetY, float offsetZ, int centerToBaselineY) {

        canvas.save();
        canvas.clipRect(mClipLeft, clipTop, mClipRight, clipBottom);
        draw3DText(canvas, text, rotateX, offsetY, offsetZ, centerToBaselineY);
        canvas.restore();
    }


    private void draw3DText(Canvas canvas, String text, float rotateX, float offsetY,
                            float offsetZ, int centerToBaselineY) {
        mCamera.save();
        mCamera.translate(0, 0, offsetZ);
        mCamera.rotateX(rotateX);
        mCamera.getMatrix(mMatrix);
        mCamera.restore();


        float centerX = mCenterX;

        if (mCurvedArcDirection == CURVED_ARC_DIRECTION_LEFT) {
            centerX = mCenterX * (1 + mCurvedArcDirectionFactor);
        } else if (mCurvedArcDirection == CURVED_ARC_DIRECTION_RIGHT) {
            centerX = mCenterX * (1 - mCurvedArcDirectionFactor);
        }

        float centerY = mCenterY + offsetY;
        mMatrix.preTranslate(-centerX, -centerY);
        mMatrix.postTranslate(centerX, centerY);

        canvas.concat(mMatrix);
        canvas.drawText(text, 0, text.length(), mStartX, centerY - centerToBaselineY, mPaint);

    }

    private void changeTypefaceIfBoldForSelectedItem() {
        if (mIsBoldForSelectedItem) {
            mPaint.setTypeface(mTextFontStyle);
        }
    }

    private void resetTypefaceIfBoldForSelectedItem() {
        if (mIsBoldForSelectedItem) {
            mPaint.setTypeface(mSelectedTextFontStyle);
        }
    }


    private String getDataByIndex(int index) {
        int dataSize = mDataList.size();
        if (dataSize == 0) {
            return null;
        }

        String itemText = null;
        if (isCyclic) {
            int i = index % dataSize;
            if (i < 0) {
                i += dataSize;
            }
            itemText = getDataText(mDataList.get(i));
        } else {
            if (index >= 0 && index < dataSize) {
                itemText = getDataText(mDataList.get(index));
            }
        }
        return itemText;
    }


    protected String getDataText(T item) {
        if (item == null) {
            return "";
        } else if (item instanceof IWheelEntity) {
            return ((IWheelEntity) item).getWheelText();
        } else if (item instanceof Integer) {

            return isIntegerNeedFormat ? String.format(Locale.getDefault(), mIntegerFormat, item)
                    : String.valueOf(item);
        } else if (item instanceof String) {
            return (String) item;
        }
        return item.toString();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (!isEnabled() || mDataList.isEmpty()) {
            return super.onTouchEvent(event);
        }
        initVelocityTracker();
        mVelocityTracker.addMovement(event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:

                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }

                if (!mScroller.isFinished()) {

                    mScroller.forceFinished(true);
                    isForceFinishScroll = true;
                }
                mLastTouchY = event.getY();

                mDownStartTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:

                float moveY = event.getY();
                float deltaY = moveY - mLastTouchY;

                if (mOnWheelChangedListener != null) {
                    mOnWheelChangedListener.onWheelScrollStateChanged(SCROLL_STATE_DRAGGING);
                }
                onWheelScrollStateChanged(SCROLL_STATE_DRAGGING);
                if (Math.abs(deltaY) < 1) {
                    break;
                }

                doScroll((int) -deltaY);
                mLastTouchY = moveY;
                invalidateIfYChanged();

                break;
            case MotionEvent.ACTION_UP:

                isForceFinishScroll = false;
                mVelocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity);
                float velocityY = mVelocityTracker.getYVelocity();
                if (Math.abs(velocityY) > mMinFlingVelocity) {

                    mScroller.forceFinished(true);
                    isFlingScroll = true;
                    mScroller.fling(0, mScrollOffsetY, 0, (int) -velocityY, 0, 0,
                            mMinScrollY, mMaxScrollY);
                } else {
                    int clickToCenterDistance = 0;
                    if (System.currentTimeMillis() - mDownStartTime <= DEFAULT_CLICK_CONFIRM) {

                        clickToCenterDistance = (int) (event.getY() - mCenterY);
                    }
                    int scrollRange = clickToCenterDistance +
                            calculateDistanceToEndPoint((mScrollOffsetY + clickToCenterDistance) % dividedItemHeight());

                    boolean isInMinRange = scrollRange < 0 && mScrollOffsetY + scrollRange >= mMinScrollY;

                    boolean isInMaxRange = scrollRange > 0 && mScrollOffsetY + scrollRange <= mMaxScrollY;
                    if (isInMinRange || isInMaxRange) {

                        mScroller.startScroll(0, mScrollOffsetY, 0, scrollRange);
                    }
                }

                invalidateIfYChanged();
                ViewCompat.postOnAnimation(this, this);

                recycleVelocityTracker();
                break;
            case MotionEvent.ACTION_CANCEL:

                recycleVelocityTracker();
                break;
        }
        return true;
    }


    private void initVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }


    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }


    private void doScroll(int distance) {
        mScrollOffsetY += distance;
        if (!isCyclic) {

            if (mScrollOffsetY < mMinScrollY) {
                mScrollOffsetY = mMinScrollY;
            } else if (mScrollOffsetY > mMaxScrollY) {
                mScrollOffsetY = mMaxScrollY;
            }
        }
    }


    private void invalidateIfYChanged() {
        if (mScrollOffsetY != mScrolledY) {
            mScrolledY = mScrollOffsetY;

            if (mOnWheelChangedListener != null) {
                mOnWheelChangedListener.onWheelScroll(mScrollOffsetY);
            }
            onWheelScroll(mScrollOffsetY);

            observeItemChanged();
            invalidate();
        }
    }


    private void observeItemChanged() {

        int oldPosition = mCurrentScrollPosition;
        int newPosition = getCurrentPosition();
        if (oldPosition != newPosition) {

            if (mOnWheelChangedListener != null) {
                mOnWheelChangedListener.onWheelItemChanged(oldPosition, newPosition);
            }
            onWheelItemChanged(oldPosition, newPosition);

            playSoundEffect();

            mCurrentScrollPosition = newPosition;
        }
    }


    public void playSoundEffect() {
        if (mSoundHelper != null && isSoundEffect) {
            mSoundHelper.playSoundEffect();
        }
    }


    public void forceFinishScroll() {
        if (!mScroller.isFinished()) {
            mScroller.forceFinished(true);
        }
    }


    public void abortFinishScroll() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
    }


    private int calculateDistanceToEndPoint(int remainder) {
        if (Math.abs(remainder) > mItemHeight / 2) {
            if (mScrollOffsetY < 0) {
                return -mItemHeight - remainder;
            } else {
                return mItemHeight - remainder;
            }
        } else {
            return -remainder;
        }
    }


    @Override
    public void run() {

        if (mScroller.isFinished() && !isForceFinishScroll && !isFlingScroll) {
            if (mItemHeight == 0) return;

            if (mOnWheelChangedListener != null) {
                mOnWheelChangedListener.onWheelScrollStateChanged(SCROLL_STATE_IDLE);
            }
            onWheelScrollStateChanged(SCROLL_STATE_IDLE);
            int currentItemPosition = getCurrentPosition();

            if (currentItemPosition == mSelectedItemPosition) {
                return;
            }
            mSelectedItemPosition = currentItemPosition;

            mCurrentScrollPosition = mSelectedItemPosition;


            if (mOnItemSelectedListener != null) {
                mOnItemSelectedListener.onItemSelected(this, mDataList.get(mSelectedItemPosition), mSelectedItemPosition);
            }
            onItemSelected(mDataList.get(mSelectedItemPosition), mSelectedItemPosition);

            if (mOnWheelChangedListener != null) {
                mOnWheelChangedListener.onWheelSelected(mSelectedItemPosition);
            }
            onWheelSelected(mSelectedItemPosition);
        }

        if (mScroller.computeScrollOffset()) {
            int oldY = mScrollOffsetY;
            mScrollOffsetY = mScroller.getCurrY();

            if (oldY != mScrollOffsetY) {
                if (mOnWheelChangedListener != null) {
                    mOnWheelChangedListener.onWheelScrollStateChanged(SCROLL_STATE_SCROLLING);
                }
                onWheelScrollStateChanged(SCROLL_STATE_SCROLLING);
            }
            invalidateIfYChanged();
            ViewCompat.postOnAnimation(this, this);
        } else if (isFlingScroll) {

            isFlingScroll = false;

            mScroller.startScroll(0, mScrollOffsetY, 0, calculateDistanceToEndPoint(mScrollOffsetY % dividedItemHeight()));
            invalidateIfYChanged();
            ViewCompat.postOnAnimation(this, this);
        }
    }


    private int getCurrentPosition() {
        if (mDataList.isEmpty()) {
            return -1;
        }
        int itemPosition;
        if (mScrollOffsetY < 0) {
            itemPosition = (mScrollOffsetY - mItemHeight / 2) / dividedItemHeight();
        } else {
            itemPosition = (mScrollOffsetY + mItemHeight / 2) / dividedItemHeight();
        }
        int currentPosition = itemPosition % mDataList.size();
        if (currentPosition < 0) {
            currentPosition += mDataList.size();
        }

        return currentPosition;
    }


    private int dividedItemHeight() {
        return mItemHeight > 0 ? mItemHeight : 1;
    }


    public boolean isSoundEffect() {
        return isSoundEffect;
    }


    public void setSoundEffect(boolean isSoundEffect) {
        this.isSoundEffect = isSoundEffect;
    }


    public void setSoundEffectResource(@RawRes int rawResId) {
        if (mSoundHelper != null) {
            mSoundHelper.load(getContext(), rawResId);
        }
    }


    public float getPlayVolume() {
        return mSoundHelper == null ? 0 : mSoundHelper.getPlayVolume();
    }


    public void setPlayVolume(@FloatRange(from = 0.0, to = 1.0) float playVolume) {
        if (mSoundHelper != null) {
            mSoundHelper.setPlayVolume(playVolume);
        }
    }


    @Nullable
    public T getItemData(int position) {
        if (isPositionInRange(position)) {
            return mDataList.get(position);
        } else if (mDataList.size() > 0 && position >= mDataList.size()) {
            return mDataList.get(mDataList.size() - 1);
        } else if (mDataList.size() > 0 && position < 0) {
            return mDataList.get(0);
        }
        return null;
    }


    public T getSelectedItemData() {
        return getItemData(mSelectedItemPosition);
    }


    public List<T> getData() {
        return mDataList;
    }


    public void setData(List<T> dataList) {
        if (dataList == null) {
            return;
        }
        mDataList = dataList;
        if (!isResetSelectedPosition && mDataList.size() > 0) {
            //不重置选中下标
            if (mSelectedItemPosition >= mDataList.size()) {
                mSelectedItemPosition = mDataList.size() - 1;
                //重置滚动下标
                mCurrentScrollPosition = mSelectedItemPosition;
            }
        } else {
            //重置选中下标和滚动下标
            mCurrentScrollPosition = mSelectedItemPosition = 0;
        }
        //强制滚动完成
        forceFinishScroll();
        calculateTextSize();
        calculateLimitY();
        //重置滚动偏移
        mScrollOffsetY = mSelectedItemPosition * mItemHeight;
        requestLayout();
        invalidate();
    }

    /**
     * 当数据变化时，是否重置选中下标到第一个
     *
     * @return 是否重置选中下标到第一个
     */
    public boolean isResetSelectedPosition() {
        return isResetSelectedPosition;
    }

    /**
     * 设置当数据变化时，是否重置选中下标到第一个
     *
     * @param isResetSelectedPosition 当数据变化时,是否重置选中下标到第一个
     */
    public void setResetSelectedPosition(boolean isResetSelectedPosition) {
        this.isResetSelectedPosition = isResetSelectedPosition;
    }

    /**
     * 获取字体大小
     *
     * @return 字体大小
     */
    public float getTextSize() {
        return mTextSize;
    }

    /**
     * 设置字体大小
     *
     * @param textSize 字体大小
     */
    public void setTextSize(float textSize) {
        setTextSize(textSize, false);
    }

    /**
     * 设置字体大小
     *
     * @param textSize 字体大小
     * @param isSp     单位是否是 sp
     */
    public void setTextSize(float textSize, boolean isSp) {
        float tempTextSize = mTextSize;
        mTextSize = isSp ? sp2px(textSize) : textSize;
        if (tempTextSize == mTextSize) {
            return;
        }
        //强制滚动完成
        forceFinishScroll();
        calculateTextSize();
        calculateDrawStart();
        calculateLimitY();
        //字体大小变化，偏移距离也变化了
        mScrollOffsetY = mSelectedItemPosition * mItemHeight;
        requestLayout();
        invalidate();
    }

    /**
     * 获取是否自动调整字体大小，以显示完全
     *
     * @return 是否自动调整字体大小
     */
    public boolean isAutoFitTextSize() {
        return isAutoFitTextSize;
    }

    /**
     * 设置是否自动调整字体大小，以显示完全
     *
     * @param isAutoFitTextSize 是否自动调整字体大小
     */
    public void setAutoFitTextSize(boolean isAutoFitTextSize) {
        this.isAutoFitTextSize = isAutoFitTextSize;
        invalidate();
    }

    /**
     * 获取当前字体
     *
     * @return 字体
     */
    public Typeface getTypeface() {
        return mPaint.getTypeface();
    }

    /**
     * 设置当前字体
     *
     * @param typeface 字体
     */
    public void setTypeface(Typeface typeface) {
        setTypeface(typeface, false);
    }

    /**
     * 设置当前字体
     *
     * @param typeface              字体
     * @param isBoldForSelectedItem 是否设置选中条目字体加粗，其他条目不会加粗
     */
    public void setTypeface(Typeface typeface, boolean isBoldForSelectedItem) {
        if (typeface == null || mPaint.getTypeface() == typeface) {
            return;
        }
        //强制滚动完成
        forceFinishScroll();
        mIsBoldForSelectedItem = isBoldForSelectedItem;
        if (mIsBoldForSelectedItem) {
            //如果设置了选中条目字体加粗，其他条目不会加粗，则拆分两份字体
            if (typeface.isBold()) {
                mNormalTypeface = Typeface.create(typeface, Typeface.NORMAL);
                mBoldTypeface = typeface;
            } else {
                mNormalTypeface = typeface;
                mBoldTypeface = Typeface.create(typeface, Typeface.BOLD);
            }
            //测量时 使用加粗字体测量，因为加粗字体比普通字体宽，以大的为准进行测量
            mPaint.setTypeface(mBoldTypeface);
        } else {
            mPaint.setTypeface(typeface);
        }
        calculateTextSize();
        calculateDrawStart();
        //字体大小变化，偏移距离也变化了
        mScrollOffsetY = mSelectedItemPosition * mItemHeight;
        calculateLimitY();
        requestLayout();
        invalidate();
    }

    /**
     * 获取文字对齐方式
     *
     * @return 文字对齐
     * {@link #TEXT_ALIGN_LEFT}
     * {@link #TEXT_ALIGN_CENTER}
     * {@link #TEXT_ALIGN_RIGHT}
     */
    public int getTextAlign() {
        return mTextAlign;
    }

    /**
     * 设置文字对齐方式
     *
     * @param textAlign 文字对齐方式
     *                  {@link #TEXT_ALIGN_LEFT}
     *                  {@link #TEXT_ALIGN_CENTER}
     *                  {@link #TEXT_ALIGN_RIGHT}
     */
    public void setTextAlign(@TextAlign int textAlign) {
        if (mTextAlign == textAlign) {
            return;
        }
        mTextAlign = textAlign;
        updateTextAlign();
        calculateDrawStart();
        invalidate();
    }

    /**
     * 获取未选中条目颜色
     *
     * @return 未选中条目颜色 ColorInt
     */
    public int getNormalItemTextColor() {
        return mTextColor;
    }

    /**
     * 设置未选中条目颜色
     *
     * @param textColorRes 未选中条目颜色 {@link ColorRes}
     */
    public void setNormalItemTextColorRes(@ColorRes int textColorRes) {
        setNormalItemTextColor(ContextCompat.getColor(getContext(), textColorRes));
    }

    /**
     * 设置未选中条目颜色
     *
     * @param textColor 未选中条目颜色 {@link ColorInt}
     */
    public void setNormalItemTextColor(@ColorInt int textColor) {
        if (mTextColor == textColor) {
            return;
        }
        mTextColor = textColor;
        invalidate();
    }

    /**
     * 获取选中条目颜色
     *
     * @return 选中条目颜色 ColorInt
     */
    public int getSelectedItemTextColor() {
        return mSelectedItemTextColor;
    }

    /**
     * 设置选中条目颜色
     *
     * @param selectedItemColorRes 选中条目颜色 {@link ColorRes}
     */
    public void setSelectedItemTextColorRes(@ColorRes int selectedItemColorRes) {
        setSelectedItemTextColor(ContextCompat.getColor(getContext(), selectedItemColorRes));
    }

    /**
     * 设置选中条目颜色
     *
     * @param selectedItemTextColor 选中条目颜色 {@link ColorInt}
     */
    public void setSelectedItemTextColor(@ColorInt int selectedItemTextColor) {
        if (mSelectedItemTextColor == selectedItemTextColor) {
            return;
        }
        mSelectedItemTextColor = selectedItemTextColor;
        invalidate();
    }

    /**
     * 获取文字距离边界的外边距
     *
     * @return 外边距值
     */
    public float getTextBoundaryMargin() {
        return mTextBoundaryMargin;
    }

    /**
     * 设置文字距离边界的外边距
     *
     * @param textBoundaryMargin 外边距值
     */
    public void setTextBoundaryMargin(float textBoundaryMargin) {
        setTextBoundaryMargin(textBoundaryMargin, false);
    }

    /**
     * 设置文字距离边界的外边距
     *
     * @param textBoundaryMargin 外边距值
     * @param isDp               单位是否为 dp
     */
    public void setTextBoundaryMargin(float textBoundaryMargin, boolean isDp) {
        float tempTextBoundaryMargin = mTextBoundaryMargin;
        mTextBoundaryMargin = isDp ? dp2px(textBoundaryMargin) : textBoundaryMargin;
        if (tempTextBoundaryMargin == mTextBoundaryMargin) {
            return;
        }
        requestLayout();
        invalidate();
    }

    /**
     * 获取item间距
     *
     * @return 行间距值
     */
    public float getLineSpacing() {
        return mLineSpacing;
    }

    /**
     * 设置item间距
     *
     * @param lineSpacing 行间距值
     */
    public void setLineSpacing(float lineSpacing) {
        setLineSpacing(lineSpacing, false);
    }

    /**
     * 设置item间距
     *
     * @param lineSpacing 行间距值
     * @param isDp        lineSpacing 单位是否为 dp
     */
    public void setLineSpacing(float lineSpacing, boolean isDp) {
        float tempLineSpace = mLineSpacing;
        mLineSpacing = isDp ? dp2px(lineSpacing) : lineSpacing;
        if (tempLineSpace == mLineSpacing) {
            return;
        }
        mScrollOffsetY = 0;
        calculateTextSize();
        requestLayout();
        invalidate();
    }

    /**
     * 获取数据为Integer类型时是否需要转换
     *
     * @return isIntegerNeedFormat
     */
    public boolean isIntegerNeedFormat() {
        return isIntegerNeedFormat;
    }

    /**
     * 设置数据为Integer类型时是否需要转换
     *
     * @param isIntegerNeedFormat 数据为Integer类型时是否需要转换
     */
    public void setIntegerNeedFormat(boolean isIntegerNeedFormat) {
        if (this.isIntegerNeedFormat == isIntegerNeedFormat) {
            return;
        }
        this.isIntegerNeedFormat = isIntegerNeedFormat;
        calculateTextSize();
        requestLayout();
        invalidate();
    }

    /**
     * 同时设置 isIntegerNeedFormat=true 和 mIntegerFormat=integerFormat
     *
     * @param integerFormat 注意：integerFormat 中必须包含并且只能包含一个格式说明符（format specifier）
     *                      格式说明符请参照 http://java2s.com/Tutorials/Java/Data_Format/Java_Format_Specifier.htm
     *                      <p>
     *                      如果有多个格式说明符会抛出 java.util.MissingFormatArgumentException: Format specifier '%s'(多出来的说明符)
     */
    public void setIntegerNeedFormat(String integerFormat) {
        isIntegerNeedFormat = true;
        mIntegerFormat = integerFormat;
        calculateTextSize();
        requestLayout();
        invalidate();
    }

    /**
     * 获取Integer类型转换格式
     *
     * @return integerFormat
     */
    public String getIntegerFormat() {
        return mIntegerFormat;
    }

    /**
     * 设置Integer类型转换格式
     *
     * @param integerFormat 注意：integerFormat 中必须包含并且只能包含一个格式说明符（format specifier）
     *                      格式说明符请参照 http://java2s.com/Tutorials/Java/Data_Format/Java_Format_Specifier.htm
     *                      <p>
     *                      如果有多个格式说明符会抛出 java.util.MissingFormatArgumentException: Format specifier '%s'(多出来的说明符)
     */
    public void setIntegerFormat(String integerFormat) {
        if (TextUtils.isEmpty(integerFormat) || integerFormat.equals(mIntegerFormat)) {
            return;
        }
        mIntegerFormat = integerFormat;
        calculateTextSize();
        requestLayout();
        invalidate();
    }

    /**
     * 获取可见条目数
     *
     * @return 可见条目数
     */
    public int getVisibleItems() {
        return mVisibleItems;
    }

    /**
     * 设置可见的条目数
     *
     * @param visibleItems 可见条目数
     */
    public void setVisibleItems(int visibleItems) {
        if (mVisibleItems == visibleItems) {
            return;
        }
        mVisibleItems = adjustVisibleItems(visibleItems);
        mScrollOffsetY = 0;
        requestLayout();
        invalidate();
    }

    /**
     * 跳转可见条目数为奇数
     *
     * @param visibleItems 可见条目数
     * @return 调整后的可见条目数
     */
    private int adjustVisibleItems(int visibleItems) {
        return Math.abs(visibleItems / 2 * 2 + 1); // 当传入的值为偶数时,换算成奇数;
    }

    /**
     * 是否是循环滚动
     *
     * @return 是否是循环滚动
     */
    public boolean isCyclic() {
        return isCyclic;
    }

    /**
     * 设置是否循环滚动
     *
     * @param isCyclic 是否是循环滚动
     */
    public void setCyclic(boolean isCyclic) {
        if (this.isCyclic == isCyclic) {
            return;
        }
        this.isCyclic = isCyclic;

        forceFinishScroll();
        calculateLimitY();
        //设置当前选中的偏移值
        mScrollOffsetY = mSelectedItemPosition * mItemHeight;
        invalidate();
    }

    /**
     * 获取当前选中下标
     *
     * @return 当前选中的下标
     */
    public int getSelectedItemPosition() {
        return mSelectedItemPosition;
    }

    /**
     * 设置当前选中下标
     *
     * @param position 下标
     */
    public void setSelectedItemPosition(int position) {
        setSelectedItemPosition(position, false);
    }

    /**
     * 设置当前选中下标
     *
     * @param position       下标
     * @param isSmoothScroll 是否平滑滚动
     */
    public void setSelectedItemPosition(int position, boolean isSmoothScroll) {
        setSelectedItemPosition(position, isSmoothScroll, 0);
    }

    /**
     * 设置当前选中下标
     * <p>
     * bug 修复记录：调用这个方法时大多数情况在初始化时，如果没有执行 onSizeChanged() 方法时，调用这个方法会导致失效
     * 因为 onSizeChanged() 方法执行结束才确定边界等信息，
     * 所以在 onSizeChanged() 方法增加了兼容，如果 mSelectedItemPosition >0 的情况重新计算一下滚动值。
     *
     * @param position       下标
     * @param isSmoothScroll 是否平滑滚动
     * @param smoothDuration 平滑滚动时间
     */
    public void setSelectedItemPosition(int position, boolean isSmoothScroll, int smoothDuration) {
        if (!isPositionInRange(position)) {
            return;
        }

        //item之间差值
        int itemDistance = calculateItemDistance(position);
        if (itemDistance == 0) {
            return;
        }
        //如果Scroller滑动未停止，强制结束动画
        abortFinishScroll();

        if (isSmoothScroll) {
            //如果是平滑滚动并且之前的Scroll滚动完成
            mScroller.startScroll(0, mScrollOffsetY, 0, itemDistance,
                    smoothDuration > 0 ? smoothDuration : DEFAULT_SCROLL_DURATION);
            invalidateIfYChanged();
            ViewCompat.postOnAnimation(this, this);

        } else {
            doScroll(itemDistance);
            mSelectedItemPosition = position;
            //选中条目回调
            if (mOnItemSelectedListener != null) {
                mOnItemSelectedListener.onItemSelected(this, mDataList.get(mSelectedItemPosition), mSelectedItemPosition);
            }
            onItemSelected(mDataList.get(mSelectedItemPosition), mSelectedItemPosition);
            if (mOnWheelChangedListener != null) {
                mOnWheelChangedListener.onWheelSelected(mSelectedItemPosition);
            }
            onWheelSelected(mSelectedItemPosition);
            invalidateIfYChanged();
        }

    }

    private int calculateItemDistance(int position) {
        return position * mItemHeight - mScrollOffsetY;
    }

    /**
     * 判断下标是否在数据列表范围内
     *
     * @param position 下标
     * @return 是否在数据列表范围内
     */
    public boolean isPositionInRange(int position) {
        return position >= 0 && position < mDataList.size();
    }

    /**
     * 获取是否显示分割线
     *
     * @return 是否显示分割线
     */
    public boolean isShowDivider() {
        return isShowDivider;
    }

    /**
     * 设置是否显示分割线
     *
     * @param isShowDivider 是否显示分割线
     */
    public void setShowDivider(boolean isShowDivider) {
        if (this.isShowDivider == isShowDivider) {
            return;
        }
        this.isShowDivider = isShowDivider;
        invalidate();
    }

    /**
     * 获取分割线颜色
     *
     * @return 分割线颜色 ColorInt
     */
    public int getDividerColor() {
        return mDividerColor;
    }

    /**
     * 设置分割线颜色
     *
     * @param dividerColorRes 分割线颜色 {@link ColorRes}
     */
    public void setDividerColorRes(@ColorRes int dividerColorRes) {
        setDividerColor(ContextCompat.getColor(getContext(), dividerColorRes));
    }

    /**
     * 设置分割线颜色
     *
     * @param dividerColor 分割线颜色 {@link ColorInt}
     */
    public void setDividerColor(@ColorInt int dividerColor) {
        if (mDividerColor == dividerColor) {
            return;
        }
        mDividerColor = dividerColor;
        invalidate();
    }

    /**
     * 获取分割线高度
     *
     * @return 分割线高度
     */
    public float getDividerHeight() {
        return mDividerSize;
    }

    /**
     * 设置分割线高度
     *
     * @param dividerHeight 分割线高度
     */
    public void setDividerHeight(float dividerHeight) {
        setDividerHeight(dividerHeight, false);
    }

    /**
     * 设置分割线高度
     *
     * @param dividerHeight 分割线高度
     * @param isDp          单位是否是 dp
     */
    public void setDividerHeight(float dividerHeight, boolean isDp) {
        float tempDividerHeight = mDividerSize;
        mDividerSize = isDp ? dp2px(dividerHeight) : dividerHeight;
        if (tempDividerHeight == mDividerSize) {
            return;
        }
        invalidate();
    }

    /**
     * 获取分割线填充类型
     *
     * @return 分割线填充类型
     * {@link #DIVIDER_TYPE_FILL}
     * {@link #DIVIDER_TYPE_WRAP}
     */
    public int getDividerType() {
        return mDividerType;
    }

    /**
     * 设置分割线填充类型
     *
     * @param dividerType 分割线填充类型
     *                    {@link #DIVIDER_TYPE_FILL}
     *                    {@link #DIVIDER_TYPE_WRAP}
     */
    public void setDividerType(@DividerType int dividerType) {
        if (mDividerType == dividerType) {
            return;
        }
        mDividerType = dividerType;
        invalidate();
    }

    /**
     * 获取自适应分割线类型时的分割线内边距
     *
     * @return 分割线内边距
     */
    public float getDividerPaddingForWrap() {
        return mDividerPaddingForWrap;
    }

    /**
     * 设置自适应分割线类型时的分割线内边距
     *
     * @param dividerPaddingForWrap 分割线内边距
     */
    public void setDividerPaddingForWrap(float dividerPaddingForWrap) {
        setDividerPaddingForWrap(dividerPaddingForWrap, false);
    }

    /**
     * 设置自适应分割线类型时的分割线内边距
     *
     * @param dividerPaddingForWrap 分割线内边距
     * @param isDp                  单位是否是 dp
     */
    public void setDividerPaddingForWrap(float dividerPaddingForWrap, boolean isDp) {
        float tempDividerPadding = mDividerPaddingForWrap;
        mDividerPaddingForWrap = isDp ? dp2px(dividerPaddingForWrap) : dividerPaddingForWrap;
        if (tempDividerPadding == mDividerPaddingForWrap) {
            return;
        }
        invalidate();
    }

    /**
     * 获取分割线两端形状
     *
     * @return 分割线两端形状
     * {@link Paint.Cap#BUTT}
     * {@link Paint.Cap#ROUND}
     * {@link Paint.Cap#SQUARE}
     */
    public Paint.Cap getDividerCap() {
        return mDividerCap;
    }

    /**
     * 设置分割线两端形状
     *
     * @param dividerCap 分割线两端形状
     *                   {@link Paint.Cap#BUTT}
     *                   {@link Paint.Cap#ROUND}
     *                   {@link Paint.Cap#SQUARE}
     */
    public void setDividerCap(Paint.Cap dividerCap) {
        if (mDividerCap == dividerCap) {
            return;
        }
        mDividerCap = dividerCap;
        invalidate();
    }

    /**
     * 获取是否绘制选中区域
     *
     * @return 是否绘制选中区域
     */
    public boolean isDrawSelectedRect() {
        return isDrawSelectedRect;
    }

    /**
     * 设置是否绘制选中区域
     *
     * @param isDrawSelectedRect 是否绘制选中区域
     */
    public void setDrawSelectedRect(boolean isDrawSelectedRect) {
        this.isDrawSelectedRect = isDrawSelectedRect;
        invalidate();
    }

    /**
     * 获取选中区域颜色
     *
     * @return 选中区域颜色 ColorInt
     */
    public int getSelectedRectColor() {
        return mSelectedRectColor;
    }

    /**
     * 设置选中区域颜色
     *
     * @param selectedRectColorRes 选中区域颜色 {@link ColorRes}
     */
    public void setSelectedRectColorRes(@ColorRes int selectedRectColorRes) {
        setSelectedRectColor(ContextCompat.getColor(getContext(), selectedRectColorRes));
    }

    /**
     * 设置选中区域颜色
     *
     * @param selectedRectColor 选中区域颜色 {@link ColorInt}
     */
    public void setSelectedRectColor(@ColorInt int selectedRectColor) {
        mSelectedRectColor = selectedRectColor;
        invalidate();
    }

    /**
     * 获取是否是弯曲（3D）效果
     *
     * @return 是否是弯曲（3D）效果
     */
    public boolean isCurved() {
        return isCurved;
    }

    /**
     * 设置是否是弯曲（3D）效果
     *
     * @param isCurved 是否是弯曲（3D）效果
     */
    public void setCurved(boolean isCurved) {
        if (this.isCurved == isCurved) {
            return;
        }
        this.isCurved = isCurved;
        calculateTextSize();
        requestLayout();
        invalidate();
    }

    /**
     * 获取弯曲（3D）效果左右圆弧效果方向
     *
     * @return 左右圆弧效果方向
     * {@link #CURVED_ARC_DIRECTION_LEFT}
     * {@link #CURVED_ARC_DIRECTION_CENTER}
     * {@link #CURVED_ARC_DIRECTION_RIGHT}
     */
    public int getCurvedArcDirection() {
        return mCurvedArcDirection;
    }

    /**
     * 设置弯曲（3D）效果左右圆弧效果方向
     *
     * @param curvedArcDirection 左右圆弧效果方向
     *                           {@link #CURVED_ARC_DIRECTION_LEFT}
     *                           {@link #CURVED_ARC_DIRECTION_CENTER}
     *                           {@link #CURVED_ARC_DIRECTION_RIGHT}
     */
    public void setCurvedArcDirection(@CurvedArcDirection int curvedArcDirection) {
        if (mCurvedArcDirection == curvedArcDirection) {
            return;
        }
        mCurvedArcDirection = curvedArcDirection;
        invalidate();
    }

    /**
     * 获取弯曲（3D）效果左右圆弧偏移效果方向系数
     *
     * @return 左右圆弧偏移效果方向系数
     */
    public float getCurvedArcDirectionFactor() {
        return mCurvedArcDirectionFactor;
    }

    /**
     * 设置弯曲（3D）效果左右圆弧偏移效果方向系数
     *
     * @param curvedArcDirectionFactor 左右圆弧偏移效果方向系数
     *                                 range 0.0-1.0 越大越明显
     */
    public void setCurvedArcDirectionFactor(@FloatRange(from = 0.0f, to = 1.0f) float curvedArcDirectionFactor) {
        if (mCurvedArcDirectionFactor == curvedArcDirectionFactor) {
            return;
        }
        if (curvedArcDirectionFactor < 0) {
            curvedArcDirectionFactor = 0f;
        } else if (curvedArcDirectionFactor > 1) {
            curvedArcDirectionFactor = 1f;
        }
        mCurvedArcDirectionFactor = curvedArcDirectionFactor;
        invalidate();
    }

    /**
     * 获取折射偏移比例
     *
     * @return 折射偏移比例
     */
    public float getRefractRatio() {
        return mRefractRatio;
    }

    /**
     * 设置选中条目折射偏移比例
     *
     * @param refractRatio 折射偏移比例 range 0.0-1.0
     */
    public void setRefractRatio(@FloatRange(from = 0.0f, to = 1.0f) float refractRatio) {
        float tempRefractRatio = mRefractRatio;
        mRefractRatio = refractRatio;
        if (mRefractRatio > 1f) {
            mRefractRatio = 1.0f;
        } else if (mRefractRatio < 0f) {
            mRefractRatio = DEFAULT_REFRACT_RATIO;
        }
        if (tempRefractRatio == mRefractRatio) {
            return;
        }
        invalidate();
    }

    @Deprecated
    public float getCurvedRefractRatio() {
        return mRefractRatio;
    }

    @Deprecated
    public void setCurvedRefractRatio(@FloatRange(from = 0.0f, to = 1.0f) float refractRatio) {
        setRefractRatio(refractRatio);
    }

    /**
     * 获取选中监听
     *
     * @return 选中监听器
     */
    public OnItemSelectedListener<T> getOnItemSelectedListener() {
        return mOnItemSelectedListener;
    }

    /**
     * 设置选中监听
     *
     * @param onItemSelectedListener 选中监听器
     */
    public void setOnItemSelectedListener(OnItemSelectedListener<T> onItemSelectedListener) {
        mOnItemSelectedListener = onItemSelectedListener;
    }

    /**
     * 获取滚动变化监听
     *
     * @return 滚动变化监听器
     */
    public OnWheelChangedListener getOnWheelChangedListener() {
        return mOnWheelChangedListener;
    }

    /**
     * 设置滚动变化监听
     *
     * @param onWheelChangedListener 滚动变化监听器
     */
    public void setOnWheelChangedListener(OnWheelChangedListener onWheelChangedListener) {
        mOnWheelChangedListener = onWheelChangedListener;
    }

    /*
      --------- 滚动变化方法同监听器方法（适用于子类） ------
     */

    /**
     * WheelView 滚动
     *
     * @param scrollOffsetY 滚动偏移
     */
    protected void onWheelScroll(int scrollOffsetY) {

    }

    /**
     * WheelView 条目变化
     *
     * @param oldPosition 旧的下标
     * @param newPosition 新下标
     */
    protected void onWheelItemChanged(int oldPosition, int newPosition) {

    }

    /**
     * WheelView 选中
     *
     * @param position 选中的下标
     */
    protected void onWheelSelected(int position) {

    }

    /**
     * WheelView 滚动状态
     *
     * @param state 滚动状态
     *              {@link WheelView#SCROLL_STATE_IDLE}
     *              {@link WheelView#SCROLL_STATE_DRAGGING}
     *              {@link WheelView#SCROLL_STATE_SCROLLING}
     */
    protected void onWheelScrollStateChanged(int state) {

    }

    /**
     * 条目选中回调
     *
     * @param data     选中的数据
     * @param position 选中的下标
     */
    protected void onItemSelected(T data, int position) {

    }

    /*
      --------- 滚动变化方法同监听器方法（适用于子类） ------
     */

    /**
     * dp转换px
     *
     * @param dp dp值
     * @return 转换后的px值
     */
    protected static float dp2px(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }

    /**
     * sp转换px
     *
     * @param sp sp值
     * @return 转换后的px值
     */
    protected static float sp2px(float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, Resources.getSystem().getDisplayMetrics());
    }

    /**
     * 自定义文字对齐方式注解
     * <p>
     * {@link #mTextAlign}
     * {@link #setTextAlign(int)}
     */
    @IntDef({TEXT_ALIGN_LEFT, TEXT_ALIGN_CENTER, TEXT_ALIGN_RIGHT})
    @Retention(value = RetentionPolicy.SOURCE)
    public @interface TextAlign {
    }

    /**
     * 自定义左右圆弧效果方向注解
     * <p>
     * {@link #mCurvedArcDirection}
     * {@link #setCurvedArcDirection(int)}
     */
    @IntDef({CURVED_ARC_DIRECTION_LEFT, CURVED_ARC_DIRECTION_CENTER, CURVED_ARC_DIRECTION_RIGHT})
    @Retention(value = RetentionPolicy.SOURCE)
    public @interface CurvedArcDirection {
    }

    /**
     * 自定义分割线类型注解
     * <p>
     * {@link #mDividerType}
     * {@link #setDividerType(int)}
     */
    @IntDef({DIVIDER_TYPE_FILL, DIVIDER_TYPE_WRAP})
    @Retention(value = RetentionPolicy.SOURCE)
    public @interface DividerType {
    }

    /**
     * 条目选中监听器
     *
     * @param <T>
     */
    public interface OnItemSelectedListener<T> {

        /**
         * 条目选中回调
         *
         * @param wheelView wheelView
         * @param data      选中的数据
         * @param position  选中的下标
         */
        void onItemSelected(WheelView<T> wheelView, T data, int position);
    }

    /**
     * WheelView滚动状态改变监听器
     */
    public interface OnWheelChangedListener {

        /**
         * WheelView 滚动
         *
         * @param scrollOffsetY 滚动偏移
         */
        void onWheelScroll(int scrollOffsetY);

        /**
         * WheelView 条目变化
         *
         * @param oldPosition 旧的下标
         * @param newPosition 新下标
         */
        void onWheelItemChanged(int oldPosition, int newPosition);

        /**
         * WheelView 选中
         *
         * @param position 选中的下标
         */
        void onWheelSelected(int position);

        /**
         * WheelView 滚动状态
         *
         * @param state 滚动状态
         *              {@link WheelView#SCROLL_STATE_IDLE}
         *              {@link WheelView#SCROLL_STATE_DRAGGING}
         *              {@link WheelView#SCROLL_STATE_SCROLLING}
         */
        void onWheelScrollStateChanged(int state);
    }

    /**
     * SoundPool 辅助类
     */
    private static class SoundHelper {

        private SoundPool mSoundPool;
        private int mSoundId;
        private float mPlayVolume;

        @SuppressWarnings("deprecation")
        private SoundHelper() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mSoundPool = new SoundPool.Builder().build();
            } else {
                mSoundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 1);
            }
        }

        /**
         * 初始化 SoundHelper
         *
         * @return SoundHelper 对象
         */
        static SoundHelper obtain() {
            return new SoundHelper();
        }

        /**
         * 加载音频资源
         *
         * @param context 上下文
         * @param resId   音频资源 {@link RawRes}
         */
        void load(Context context, @RawRes int resId) {
            if (mSoundPool != null) {
                mSoundId = mSoundPool.load(context, resId, 1);
            }
        }

        /**
         * 设置音量
         *
         * @param playVolume 音频播放音量 range 0.0-1.0
         */
        void setPlayVolume(@FloatRange(from = 0.0, to = 1.0) float playVolume) {
            this.mPlayVolume = playVolume;
        }

        /**
         * 获取音量
         *
         * @return 音频播放音量 range 0.0-1.0
         */
        float getPlayVolume() {
            return mPlayVolume;
        }

        /**
         * 播放声音效果
         */
        void playSoundEffect() {
            if (mSoundPool != null && mSoundId != 0) {
                mSoundPool.play(mSoundId, mPlayVolume, mPlayVolume, 1, 0, 1);
            }
        }

        /**
         * 释放SoundPool
         */
        void release() {
            if (mSoundPool != null) {
                mSoundPool.release();
                mSoundPool = null;
            }
        }
    }
}