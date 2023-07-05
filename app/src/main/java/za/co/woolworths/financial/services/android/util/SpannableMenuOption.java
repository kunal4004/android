package za.co.woolworths.financial.services.android.util;

import android.content.Context;
import android.graphics.Typeface;
import androidx.core.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import com.awfs.coordination.R;

import static android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE;

/**
 * Created by dimitrij on 2016/12/14.
 */

public class SpannableMenuOption {

    private Context mContext;

    public SpannableMenuOption(Context context){
        this.mContext=context;
    }

    public SpannableString customSpannableSearch(String s){
        SpannableString mTextSearchStoreLoc = new SpannableString(s);
        int mStoreFontSize = mContext.getResources().getDimensionPixelSize(R.dimen.search_store);
        int length = mTextSearchStoreLoc.length();
        Typeface mTypfaceMyriadPro =  Typeface.createFromAsset(mContext.getAssets(), "OpenSans-Regular.ttf");
        mTextSearchStoreLoc.setSpan (new CustomTypefaceSpan("",mTypfaceMyriadPro), 0, length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        mTextSearchStoreLoc.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.indicator_unselected)), 0, length, 0);
        mTextSearchStoreLoc.setSpan(new AbsoluteSizeSpan(mStoreFontSize), 0, length, SPAN_INCLUSIVE_INCLUSIVE);
        return mTextSearchStoreLoc;
    }

    public CharSequence distanceKm(String distance){
        Typeface mTypfaceMyriadPro =  Typeface.createFromAsset(mContext.getAssets(), "fonts/WFutura-Medium.ttf");
        int mTextStoreSize = mContext.getResources().getDimensionPixelSize(R.dimen.search_store);
        int mKmDistance = mContext.getResources().getDimensionPixelSize(R.dimen.distance_km);
        SpannableString ssDistance = new SpannableString(distance);
        SpannableString mSpanKm = new SpannableString(mContext.getResources().getString(R.string.distance_in_km));
        int length = mSpanKm.length()-1;
        ssDistance.setSpan (new CustomTypefaceSpan("",mTypfaceMyriadPro), 0, length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        ssDistance.setSpan(new AbsoluteSizeSpan(mTextStoreSize), 0, length, SPAN_INCLUSIVE_INCLUSIVE);
        mSpanKm.setSpan(new AbsoluteSizeSpan(mKmDistance), 0, length, SPAN_INCLUSIVE_INCLUSIVE);
        mSpanKm.setSpan (new CustomTypefaceSpan("",mTypfaceMyriadPro), 0, length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        CharSequence mDistancekM = TextUtils.concat(ssDistance, " ", mSpanKm);
        return mDistancekM;
    }
}
