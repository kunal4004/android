package za.co.woolworths.financial.services.android.util;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;

import com.awfs.coordination.R;

import uk.co.chrisjenx.calligraphy.CalligraphyTypefaceSpan;

public class FontHyperTextParser {

    public static SpannableString getSpannable(String mSpannableText, int defaultFont, Context context) {
        if (context == null) return SpannableString.valueOf(mSpannableText);
        String[] split = mSpannableText.split("\\|");
        SpannableString spannableString = new SpannableString(mSpannableText.replaceAll("\\|![lmsb\\?]\\|", ""));
        int start = 0;
        Typeface typeface = Typeface.createFromAsset(context.getAssets(), context.getResources().getStringArray(R.array.fonts)[defaultFont]);
        for (String s : split) {
            if (s.matches("![lmsb]")) {
                if (s.contains("l")) {
                    typeface = Typeface.createFromAsset(context.getAssets(), context.getResources().getStringArray(R.array.fonts)[0]);
                } else if (s.contains("m")) {
                    typeface = Typeface.createFromAsset(context.getAssets(), context.getResources().getStringArray(R.array.fonts)[1]);
                } else if (s.contains("s")) {
                    typeface = Typeface.createFromAsset(context.getAssets(), context.getResources().getStringArray(R.array.fonts)[2]);
                } else if (s.contains("b")) {
                    typeface = Typeface.createFromAsset(context.getAssets(), context.getResources().getStringArray(R.array.fonts)[3]);
                }
            } else if (s.matches("!\\?")) {
                typeface = Typeface.createFromAsset(context.getAssets(), context.getResources().getStringArray(R.array.fonts)[defaultFont]);
            } else {
                spannableString.setSpan(new CalligraphyTypefaceSpan(typeface), start, start + s.length(),
                        0);
                start += s.length();
            }
        }
        return spannableString;
    }
}
