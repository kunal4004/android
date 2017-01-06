package za.co.woolworths.financial.services.android.ui.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WTextView;

/**
 * Created by W7099877 on 06/01/2017.
 */

public class WRewardsSavingsHorizontalScrollAdapter extends RecyclerView.Adapter<WRewardsSavingsHorizontalScrollAdapter.ViewHolder> {
    Activity context;
    public int selectedPosition = 1;
    public final int  HEADER_POSITION = 0;

    public WRewardsSavingsHorizontalScrollAdapter(Activity context) {
        this.context = context;
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
            holder.listItem.setAlpha(0.3f);
            holder.monthText.setText(context.getString(R.string.year));
            holder.yearText.setText(context.getString(R.string.to_date));
        } else {
            if (position == selectedPosition)
                holder.listItem.setAlpha(1f);
            else
                holder.listItem.setAlpha(0.3f);
        }
    }

    @Override
    public int getItemCount() {
        //return size()+1 for header view
        return 5;
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
