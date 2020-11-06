package za.co.woolworths.financial.services.android.ui.views;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.RadioButton;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.util.FontHyperTextParser;

public class WRadioButton extends RadioButton {
    private int mFont;
    private String mSpannableText;

    public WRadioButton(Context context) {
        super(context);
        init(null);
    }

    public WRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public WRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(21)
    public WRadioButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.WButton,
                    0, 0);
            mFont = a.getInt(R.styleable.WButton_WButton_font, 1);
            mSpannableText = a.getString(R.styleable.WButton_WButton_spannable_text);
        }
        if (mSpannableText != null){
            setText(FontHyperTextParser.getSpannable(mSpannableText, mFont), BufferType.SPANNABLE);
        } else {
            setTypeface(Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getStringArray(R.array.fonts)[mFont]));
        }
    }

}
