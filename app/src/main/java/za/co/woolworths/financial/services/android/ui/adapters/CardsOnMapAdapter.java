package za.co.woolworths.financial.services.android.ui.adapters;

import android.app.Activity;
import android.os.Parcelable;
import androidx.viewpager.widget.PagerAdapter;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.StoreDetails;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.WFormatter;

import static android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE;

/**
 * Created by W7099877 on 10/10/2016.
 */

public class CardsOnMapAdapter extends PagerAdapter {
    public Activity mContext;
    public List<StoreDetails> storeDetailsList;

    public CardsOnMapAdapter(Activity context, List<StoreDetails> storeDetailsList) {
        this.mContext = context;
        this.storeDetailsList = storeDetailsList;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        container.removeView((View) object);
    }


    @Override
    public Object instantiateItem(ViewGroup container, final int position) {

        View cView = mContext.getLayoutInflater().inflate(R.layout.store_nearby_item, container, false);
        WTextView storeName = (WTextView) cView.findViewById(R.id.storeName);
        WTextView storeOfferings = (WTextView) cView.findViewById(R.id.offerings);
        WTextView storeDistance = (WTextView) cView.findViewById(R.id.distance);
        WTextView storeAddress = (WTextView) cView.findViewById(R.id.storeAddress);
        WTextView storeTimeing = (WTextView) cView.findViewById(R.id.timeing);
        storeName.setText(storeDetailsList.get(position).name);
        if (!TextUtils.isEmpty(storeDetailsList.get(position).address))
            storeAddress.setText(storeDetailsList.get(position).address);

        int mKmDistance = mContext.getResources().getDimensionPixelSize(R.dimen.distance_km);
        SpannableString ssDistance = new SpannableString(WFormatter.formatMeter(storeDetailsList.get(position).distance));
        SpannableString mSpanKm = new SpannableString(mContext.getResources().getString(R.string.distance_in_km));
        mSpanKm.setSpan(new AbsoluteSizeSpan(mKmDistance), 0, mSpanKm.length(), SPAN_INCLUSIVE_INCLUSIVE);
        CharSequence mDistancekM = TextUtils.concat(ssDistance, "\n", mSpanKm);
        storeDistance.setText(mDistancekM);

        if (storeDetailsList.get(position).offerings != null)
            storeOfferings.setText(WFormatter.formatOfferingString(storeDetailsList.get(position).offerings));
        if (storeDetailsList.get(position).times != null) {
            try {
                String mHour = WFormatter.formatOpenUntilTime(storeDetailsList.get(position).times.get(0).hours);
                storeTimeing.setText("Open until " + mHour);
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }
        }
        container.addView(cView);
        return cView;
    }

    @Override
    public int getCount() {
        return storeDetailsList.size();
    }

    @Override
    public float getPageWidth(int position) {
        return 0.92f;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        super.restoreState(state, loader);
    }

    @Override
    public Parcelable saveState() {
        return null;
    }
}