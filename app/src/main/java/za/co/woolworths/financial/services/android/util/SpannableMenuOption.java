package za.co.woolworths.financial.services.android.util;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;

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
        Typeface mTypfaceMyriadPro =  Typeface.createFromAsset(mContext.getAssets(), "fonts/MyriadPro-Regular.otf");
        mTextSearchStoreLoc.setSpan (new CustomTypefaceSpan("",mTypfaceMyriadPro), 0, mTextSearchStoreLoc.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        mTextSearchStoreLoc.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.indicator_unselected)), 0, mTextSearchStoreLoc.length(), 0);
        mTextSearchStoreLoc.setSpan(new AbsoluteSizeSpan(mStoreFontSize), 0, mTextSearchStoreLoc.length(), SPAN_INCLUSIVE_INCLUSIVE);
        return mTextSearchStoreLoc;
    }

    public CharSequence distanceKm(String distance){
        Typeface mTypfaceMyriadPro =  Typeface.createFromAsset(mContext.getAssets(), "fonts/WFutura-Medium.ttf");
        int mTextStoreSize = mContext.getResources().getDimensionPixelSize(R.dimen.search_store);
        int mKmDistance = mContext.getResources().getDimensionPixelSize(R.dimen.distance_km);
        SpannableString ssDistance = new SpannableString(distance);
        SpannableString mSpanKm = new SpannableString(mContext.getResources().getString(R.string.distance_in_km));
        ssDistance.setSpan (new CustomTypefaceSpan("",mTypfaceMyriadPro), 0, mSpanKm.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        ssDistance.setSpan(new AbsoluteSizeSpan(mTextStoreSize), 0, mSpanKm.length(), SPAN_INCLUSIVE_INCLUSIVE);
        mSpanKm.setSpan(new AbsoluteSizeSpan(mKmDistance), 0, mSpanKm.length(), SPAN_INCLUSIVE_INCLUSIVE);
        mSpanKm.setSpan (new CustomTypefaceSpan("",mTypfaceMyriadPro), 0, mSpanKm.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        CharSequence mDistancekM = TextUtils.concat(ssDistance, " ", mSpanKm);
        return mDistancekM;
    }
}
