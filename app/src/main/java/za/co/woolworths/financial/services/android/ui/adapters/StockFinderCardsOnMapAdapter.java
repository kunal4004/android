package za.co.woolworths.financial.services.android.ui.adapters;

import android.app.Activity;
import android.os.Parcelable;
import androidx.viewpager.widget.PagerAdapter;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.StoreDetails;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;

import static android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE;

public class StockFinderCardsOnMapAdapter extends PagerAdapter {
	private Activity mContext;
	private List<StoreDetails> storeDetailsList;

	public StockFinderCardsOnMapAdapter(Activity context, List<StoreDetails> storeDetailsList) {
		this.mContext = context;
		this.storeDetailsList = storeDetailsList;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

	@Override
	public Object instantiateItem(ViewGroup container, final int position) {

		View cView = mContext.getLayoutInflater().inflate(R.layout.stock_finder_item, container, false);
		WTextView storeName = (WTextView) cView.findViewById(R.id.storeName);
		WTextView storeDistance = (WTextView) cView.findViewById(R.id.distance);
		TextView storeAddress = (TextView) cView.findViewById(R.id.storeAddress);
		TextView storeTimeing = (TextView) cView.findViewById(R.id.timeing);
		WTextView offerings = (WTextView) cView.findViewById(R.id.offerings);

		StoreDetails storeDetails = storeDetailsList.get(position);
		storeName.setText(storeDetails.name);
		if (!TextUtils.isEmpty(storeDetails.address))
			storeAddress.setText(storeDetails.address);
		String status = storeDetails.status;
		Utils.setRagRating(storeDistance.getContext(), offerings, storeDetails.status);
		int mKmDistance = mContext.getResources().getDimensionPixelSize(R.dimen.distance_km);
		SpannableString ssDistance = new SpannableString(WFormatter.formatMeter(storeDetailsList.get(position).distance));
		SpannableString mSpanKm = new SpannableString(mContext.getResources().getString(R.string.distance_in_km));
		mSpanKm.setSpan(new AbsoluteSizeSpan(mKmDistance), 0, mSpanKm.length(), SPAN_INCLUSIVE_INCLUSIVE);
		CharSequence mDistancekM = TextUtils.concat(ssDistance, "\n", mSpanKm);
		storeDistance.setText(mDistancekM);

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