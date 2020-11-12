package za.co.woolworths.financial.services.android.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.util.FontHyperTextParser;


public class WTextView extends TextView {
    private int mFont = 1;
    private String mSpannableText;

    public WTextView(Context context) {
        super(context);
        init(null);
    }

    public WTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public WTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(21)
    public WTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.WTextView,
                    0, 0);
            mFont = a.getInt(R.styleable.WTextView_WTextView_font, 1);
            mSpannableText = a.getString(R.styleable.WTextView_WTextView_spannable_text);
        }
        if (mSpannableText != null) {
            setText(FontHyperTextParser.getSpannable(mSpannableText, mFont), BufferType.SPANNABLE);
        } else {
            setTypeface(Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getStringArray(R.array.fonts)[mFont]));
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        String s = text.toString();
        if (s.split("\\|![lmsb\\?]\\|").length == 1) {
            super.setText(text, type);
        } else {
            super.setText(FontHyperTextParser.getSpannable(s, mFont), BufferType.SPANNABLE);
        }
    }

    public void setText(String s) {
        if (s.split("\\|![lmsb\\?]\\|").length == 1) {
            super.setText(FontHyperTextParser.getSpannable(s, mFont), BufferType.SPANNABLE);
        } else {
            super.setText(s);
        }
    }

}
