package za.co.woolworths.financial.services.android.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.KeyEvent;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.util.FontHyperTextParser;

public class WLoanEditTextView extends WEditTextView {

    public interface OnKeyPreImeListener {
        void onBackPressed();
    }

    private int mFont = 1;
    private String mSpannableText;

    OnKeyPreImeListener onKeyPreImeListener;

    public void setOnKeyPreImeListener(OnKeyPreImeListener onKeyPreImeListener) {
        this.onKeyPreImeListener = onKeyPreImeListener;
    }


    public WLoanEditTextView(Context context) {
        super(context);
        init(null);
    }

    public WLoanEditTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public WLoanEditTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(21)
    public WLoanEditTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.WEditTextView,
                    0, 0);
            mFont = a.getInt(R.styleable.WEditTextView_WEditTextView_font, 1);
            mSpannableText = a.getString(R.styleable.WEditTextView_WEditTextView_spannable_text);
        }
        if (mSpannableText != null) {
            setText(FontHyperTextParser.getSpannable(mSpannableText, mFont), BufferType.SPANNABLE);
        } else {
            setTypeface(Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getStringArray(R.array.fonts)[mFont]));
        }
    }

    @Override
    public void setError(CharSequence error) {
        if (error == null){
            super.setError(error);
        } else {
            super.setError(FontHyperTextParser.getSpannable(error.toString(), mFont));
        }
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if(onKeyPreImeListener != null)
                onKeyPreImeListener.onBackPressed();
            return false;
        }
        return super.dispatchKeyEvent(event);
    }


}
