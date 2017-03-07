package za.co.woolworths.financial.services.android.util.zxing;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

public class QRCodeView extends RelativeLayout {

    private int maskColor;
    private int boxViewWidth;
    private int boxViewHeight;
    private int cornerColor;
    private int borderColor;
    private int cornerSize;
    private int cornerLength;
    private int cornerOffset;

    private FrameLayout boxView;
    private OnClickListener lightOnClickListener;

    public QRCodeView(Context context) {
        super(context);
        initialize(context, null, 0, 0);
    }

    public QRCodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0, 0);
    }

    public QRCodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr, 0);
    }

    @SuppressLint("NewApi")
    public QRCodeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, R.layout.layout_qr_code_view, this);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.QRCodeView, defStyleAttr, 0);
        Resources resources = getResources();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            maskColor = typedArray.getColor(R.styleable.QRCodeView_maskColor, ContextCompat.getColor(context, R.color.semi_per_black));
            cornerColor = typedArray.getColor(R.styleable.QRCodeView_boxViewCornerColor, ContextCompat.getColor(context, R.color.white));
            borderColor = typedArray.getColor(R.styleable.QRCodeView_boxViewBorderColor, ContextCompat.getColor(context, R.color.transparent));
        } else {
            maskColor = typedArray.getColor(R.styleable.QRCodeView_boxViewCornerColor, resources.getColor(R.color.semi_per_black, null));
            cornerColor = typedArray.getColor(R.styleable.QRCodeView_boxViewCornerColor, resources.getColor(R.color.white, null));
            borderColor = typedArray.getColor(R.styleable.QRCodeView_boxViewBorderColor, resources.getColor(R.color.transparent, null));
        }

        Display display = ((AppCompatActivity) context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        cornerOffset = typedArray.getInt(R.styleable.QRCodeView_boxViewCornerOffset, (int) resources.getDimension(R.dimen.size_qr_box_view_corner_offset));
        cornerLength = typedArray.getInt(R.styleable.QRCodeView_boxViewCornerLength, (int) resources.getDimension(R.dimen.length_qr_box_view_corner));
        cornerSize = typedArray.getInt(R.styleable.QRCodeView_boxViewCornerSize, (int) resources.getDimension(R.dimen.size_qr_box_view_corner));
        boxViewWidth = typedArray.getInt(R.styleable.QRCodeView_boxViewWidth, width * 7 / 10);
        boxViewHeight = typedArray.getInt(R.styleable.QRCodeView_boxViewHeight, width / 2);

        typedArray.recycle();
        boxView = (FrameLayout) findViewById(R.id.fl_box_view);
        LayoutParams params = (LayoutParams) boxView.getLayoutParams();
        params.width = boxViewWidth;
        params.height = boxViewHeight;
        boxView.setLayoutParams(params);
        setBackgroundResource(R.color.transparent);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        findViewById(R.id.btn_light).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox checkBox = (CheckBox) view;
                if (checkBox.isChecked()) {
                    checkBox.setText(R.string.app_label);
                } else {
                    checkBox.setText(R.string.app_name);
                }
                if (lightOnClickListener != null) {
                    lightOnClickListener.onClick(view);
                }
            }
        });
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.exlore_line_move);
        animation.setInterpolator(new LinearInterpolator());
        findViewById(R.id.img_scan_line).setAnimation(animation);
    }

    @Override
    public void onDraw(Canvas canvas) {
        /** Draw the exterior dark mask*/
        int width = getWidth();
        int height = getHeight();
        float boxViewX = boxView.getX();
        float boxViewY = boxView.getY();

        int padding = 20;
        int paddingTop = 30;
        int paddingBottom = 30;

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(maskColor);
        canvas.drawRect(0, boxViewY, boxViewX, boxViewY + boxViewHeight, paint);// left rect
        canvas.drawRect(boxViewX + boxViewWidth, boxViewY, width, boxViewY + boxViewHeight, paint);// right rect
        canvas.drawRect(0, 0, width, boxViewY, paint);// top rect
        canvas.drawRect(0, boxViewY + boxViewHeight, width, height, paint);// bottom rect

        /** Draw the border lines*/
        paint.setColor(borderColor);
        canvas.drawLine(boxViewX, boxViewY, boxViewX + boxViewWidth, boxViewY, paint);
        canvas.drawLine(boxViewX, boxViewY, boxViewX, boxViewY + boxViewHeight, paint);
        canvas.drawLine(boxViewX + boxViewWidth, boxViewY + boxViewHeight, boxViewX, boxViewY + boxViewHeight, paint);
        canvas.drawLine(boxViewX + boxViewWidth, boxViewY + boxViewHeight, boxViewX + boxViewWidth, boxViewY, paint);

        /** Draw the corners*/
        Rect rect = new Rect();
        rect.set((int) boxViewX, (int) boxViewY, (int) boxViewX + boxViewWidth, (int) boxViewY + boxViewHeight);
        paint.setColor(cornerColor);

        /** top the corners*/
        canvas.drawRect(rect.left - cornerSize + cornerOffset+padding, rect.top - cornerSize + cornerOffset+padding, rect.left + cornerLength - cornerSize + cornerOffset+padding, rect.top + cornerOffset+padding, paint);
        canvas.drawRect(rect.left - cornerSize + cornerOffset+padding, rect.top - cornerSize + cornerOffset+padding, rect.left + cornerOffset+padding, rect.top + cornerLength - cornerSize + cornerOffset+padding, paint);
        canvas.drawRect(rect.right - cornerLength + cornerSize - cornerOffset+padding, rect.top - cornerSize + cornerOffset+padding, rect.right + cornerSize - cornerOffset+padding, rect.top + cornerOffset+padding, paint);
        canvas.drawRect(rect.right - cornerOffset+padding, rect.top - cornerSize + cornerOffset+padding, rect.right + cornerSize - cornerOffset+padding, rect.top + cornerLength - cornerSize + cornerOffset+padding, paint);

        /** bottom the corners*/
        canvas.drawRect(rect.left - cornerSize + cornerOffset+padding, rect.bottom - cornerOffset+padding, rect.left + cornerLength - cornerSize + cornerOffset+padding, rect.bottom + cornerSize - cornerOffset+padding, paint);
        canvas.drawRect(rect.left - cornerSize + cornerOffset+padding, rect.bottom - cornerLength + cornerSize - cornerOffset+padding, rect.left + cornerOffset+padding, rect.bottom + cornerSize - cornerOffset+padding, paint);
        canvas.drawRect(rect.right - cornerLength + cornerSize - cornerOffset+padding, rect.bottom - cornerOffset+padding, rect.right + cornerSize - cornerOffset+padding, rect.bottom + cornerSize - cornerOffset+padding, paint);
        canvas.drawRect(rect.right - cornerOffset+padding, rect.bottom - cornerLength + cornerSize - cornerOffset+padding, rect.right + cornerSize - cornerOffset+padding, rect.bottom + cornerSize - cornerOffset+padding, paint);
    }


    public void setPickImageListener(OnClickListener onClickListener) {
        if (onClickListener != null) {
            findViewById(R.id.btn_photo).setOnClickListener(onClickListener);
        }
    }

    public void setProduceQRListener(OnClickListener onClickListener) {
        if (onClickListener != null) {
            findViewById(R.id.btn_produce).setOnClickListener(onClickListener);
        }
    }

    public void setLightOnClickListener(OnClickListener lightOnClickListener) {
        this.lightOnClickListener = lightOnClickListener;
    }
}