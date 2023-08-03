package za.co.woolworths.financial.services.android.ui.adapters;

import android.app.Activity;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.StoreDetails;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.WFormatter;

/**
 * Created by W7099877 on 11/10/2016.
 */

public class StoreSearchListAdapter extends RecyclerView.Adapter<StoreSearchListAdapter.SearchViewHolder> {

	Activity mContext;
	List<StoreDetails> storeDetailsList;


	public class SearchViewHolder extends RecyclerView.ViewHolder {
		WTextView storeName;
		WTextView storeOfferings;
		TextView storeDistance;
		TextView storeAddress;
		TextView storeTimeing;

		public SearchViewHolder(View cView) {
			super(cView);
			storeName = (WTextView) cView.findViewById(R.id.storeName);
			storeOfferings = (WTextView) cView.findViewById(R.id.offerings);
			storeDistance =  cView.findViewById(R.id.distance);
			storeAddress = (TextView) cView.findViewById(R.id.storeAddress);
			storeTimeing = (TextView) cView.findViewById(R.id.timeing);
		}
	}

	public StoreSearchListAdapter(Activity context, List<StoreDetails> storeDetailsList) {
		this.mContext = context;
		this.storeDetailsList = storeDetailsList;
	}

	@Override
	public void onBindViewHolder(SearchViewHolder holder, int position) {
		holder.storeName.setText(storeDetailsList.get(position).name);
		if (!TextUtils.isEmpty(storeDetailsList.get(position).address))
			holder.storeAddress.setText(storeDetailsList.get(position).address);
		holder.storeDistance.setText(WFormatter.formatMeter(storeDetailsList.get(position).distance));
		if (getItemViewType(position) == 0) {
			// Inflate padded layout
			holder.itemView.setPadding(0, 45, 0, 0);
		} else {
			// Inflate standard layout
			holder.itemView.setPadding(0, 0, 0, 0);
		}
		if (storeDetailsList.get(position).offerings != null)
			holder.storeOfferings.setText(WFormatter.formatOfferingString(storeDetailsList.get(position).offerings));
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
		View v = mContext.getLayoutInflater().inflate(R.layout.search_store_nearby_item, parent, false);
		return new SearchViewHolder(v);
	}
}
