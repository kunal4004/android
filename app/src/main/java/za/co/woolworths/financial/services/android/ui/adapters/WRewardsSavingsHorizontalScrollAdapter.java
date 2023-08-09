package za.co.woolworths.financial.services.android.ui.adapters;

import android.app.Activity;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.TierHistory;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.unique_locators.WRewardUniqueLocatorsHelper;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

/**
 * Created by W7099877 on 06/01/2017.
 */

public class WRewardsSavingsHorizontalScrollAdapter extends RecyclerView.Adapter<WRewardsSavingsHorizontalScrollAdapter.ViewHolder> {
    Activity context;
    public int selectedPosition = 0;
    public final int  HEADER_POSITION = 0;
    List<TierHistory> tierHistoryList;

    public WRewardsSavingsHorizontalScrollAdapter(Activity context, List<TierHistory> tierHistoryList) {
        this.context = context;
        this.tierHistoryList=tierHistoryList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.wrewards_savings_horizontal_scroll_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if (position == HEADER_POSITION) {
          //  holder.listItem.setAlpha(0.3f);
            holder.monthText.setText(context.getString(R.string.year));
            holder.yearText.setText(context.getString(R.string.to_date));
        } else {
            String[] monthAndYear=tierHistoryList.get(position-1).finMonthDescription.trim().split(" ");
            holder.monthText.setText(monthAndYear[0].substring(0,3));
            holder.yearText.setText(monthAndYear[1]);
        }
        WRewardUniqueLocatorsHelper.Companion.setSavingsYearMonthsLocators(holder.itemView,position == HEADER_POSITION,position);

        if (position == selectedPosition) {
            holder.listItem.setBackgroundResource(R.drawable.black_circle);
            holder.yearText.setAlpha(0.6f);
        }
        else {
            holder.listItem.setBackgroundResource(R.drawable.gray_circle);
            holder.yearText.setAlpha(1f);
        }
    }

    @Override
    public int getItemCount() {
        //return size()+1 for header view
        return tierHistoryList.size()+1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public WTextView monthText;
        public WTextView yearText;
        public RelativeLayout listItem;

        public ViewHolder(View v) {
            super(v);
            monthText = (WTextView) v.findViewById(R.id.month);
            yearText = (WTextView) v.findViewById(R.id.year);
            listItem = (RelativeLayout) v.findViewById(R.id.listItem);
        }
    }

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;

    }
}
