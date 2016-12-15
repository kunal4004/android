package za.co.woolworths.financial.services.android.ui.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

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
        WTextView storeDistance;
        WTextView storeAddress;
        WTextView storeTimeing;


        public SearchViewHolder(View cView) {
            super(cView);
             storeName=(WTextView)cView.findViewById(R.id.storeName);
             storeOfferings=(WTextView)cView.findViewById(R.id.offerings);
             storeDistance=(WTextView)cView.findViewById(R.id.distance);
             storeAddress=(WTextView)cView.findViewById(R.id.storeAddress);
             storeTimeing=(WTextView)cView.findViewById(R.id.timeing);

        }
    }
    public StoreSearchListAdapter(Activity context, List<StoreDetails> storeDetailsList)
    {
          this.mContext=context;
          this.storeDetailsList=storeDetailsList;
    }

    @Override
    public void onBindViewHolder(SearchViewHolder holder, int position) {
        holder.storeName.setText(storeDetailsList.get(position).name);
        holder.storeAddress.setText(storeDetailsList.get(position).address);
        holder.storeDistance.setText(WFormatter.formatMeter(storeDetailsList.get(position).distance));
        if(storeDetailsList.get(position).offerings!=null)
             holder.storeOfferings.setText(WFormatter.formatOfferingString(storeDetailsList.get(position).offerings));
        if(storeDetailsList.get(position).times!=null  )
            holder.storeTimeing.setText("Open until "+WFormatter.formatOpenUntilTime(storeDetailsList.get(position).times.get(0).hours));
    }
    @Override
    public int getItemCount() {
        return storeDetailsList.size();
    }

    @Override
    public SearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= mContext.getLayoutInflater()
                .inflate(R.layout.search_store_nearby_item, parent, false);
        return new SearchViewHolder(v);
    }
}
