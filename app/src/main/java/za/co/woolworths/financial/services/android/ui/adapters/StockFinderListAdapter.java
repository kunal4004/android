package za.co.woolworths.financial.services.android.ui.adapters;

import android.app.Activity;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.StoreDetails;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;

/**
 * Created by W7099877 on 11/10/2016.
 */

public class StockFinderListAdapter extends RecyclerView.Adapter<StockFinderListAdapter.SearchViewHolder> {

	private Activity mContext;
	private List<StoreDetails> storeDetailsList;

	public class SearchViewHolder extends RecyclerView.ViewHolder {

		WTextView storeName;
		WTextView storeDistance;
		TextView storeAddress;
		TextView storeTimeing;
		WTextView offerings;
		LinearLayout llKilometerContainer;

		public SearchViewHolder(View cView) {
			super(cView);
			storeName = (WTextView) cView.findViewById(R.id.storeName);
			storeDistance = (WTextView) cView.findViewById(R.id.distance);
			storeAddress = (TextView) cView.findViewById(R.id.storeAddress);
			storeTimeing = (TextView) cView.findViewById(R.id.timeing);
			offerings = (WTextView) cView.findViewById(R.id.offerings);
			llKilometerContainer = (LinearLayout) cView.findViewById(R.id.llKilometerContainer);
		}
	}

	public StockFinderListAdapter(Activity context, List<StoreDetails> storeDetailsList) {
		this.mContext = context;
		this.storeDetailsList = storeDetailsList;
	}

	@Override
	public void onBindViewHolder(SearchViewHolder holder, int position) {
		StoreDetails storeDetails = storeDetailsList.get(position);
		holder.storeName.setText(TextUtils.isEmpty(storeDetails.name) ? "" : storeDetails.name);
		holder.storeAddress.setText(TextUtils.isEmpty(storeDetails.address) ? "" : storeDetails.address);
		holder.storeDistance.setText(WFormatter.formatMeter(storeDetails.distance));

		Utils.setRagRating(holder.storeDistance.getContext(), holder.offerings, storeDetails.status);

		if (getItemViewType(position) == 0) {
			// Inflate padded layout
			holder.itemView.setPadding(0, 45, 0, 0);
		} else {
			// Inflate standard layout
			holder.itemView.setPadding(0, 0, 0, 0);
		}
		if (storeDetailsList.get(position).times != null) {
			try {
				String mHour = WFormatter.formatOpenUntilTime(storeDetailsList.get(position).times.get(0).hours);
				holder.storeTimeing.setText("Open until " + mHour);
			} catch (ArrayIndexOutOfBoundsException ignored) {
			}
		}
	}

	@Override
	public int getItemCount() {
		return storeDetailsList.size();
	}

	@Override
	public int getItemViewType(int position) {
		return (position == 0) ? 0 : position;
	}

	@Override
	public SearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = mContext.getLayoutInflater().inflate(R.layout.stock_finder_nearby_store_item, parent, false);
		return new SearchViewHolder(v);
	}
}
