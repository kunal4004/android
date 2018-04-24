package za.co.woolworths.financial.services.android.ui.adapters.sub_category;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.expand.ExpandableRecyclerView;

public class ExpandableTestAdapter extends ExpandableRecyclerView.Adapter<ExpandableTestAdapter.ChildViewHolder, ExpandableRecyclerView.SimpleGroupViewHolder, String, String> {

	public ExpandableTestAdapter() {
	}

	@Override
	public int getGroupItemCount() {
		return 6;
	}

	@Override
	public int getChildItemCount(int group) {
		return group + 1;
	}

	@Override
	public String getGroupItem(int position) {
		return "Category name :" + position;
	}

	@Override
	public String getChildItem(int group, int position) {
		return "Category item : " + group + " item" + position;
	}

	@Override
	protected ExpandableRecyclerView.SimpleGroupViewHolder onCreateGroupViewHolder(ViewGroup parent) {
		return new ExpandableRecyclerView.SimpleGroupViewHolder(parent.getContext());
	}

	@Override
	protected ChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View view = inflater.inflate(R.layout.list_item_sub_category, parent, false);
		return new ChildViewHolder(view);
	}

	@Override
	public int getChildItemViewType(int group, int position) {
		return 1;
	}

	@Override
	public void onBindGroupViewHolder(ExpandableRecyclerView.SimpleGroupViewHolder holder, int group) {
		super.onBindGroupViewHolder(holder, group);
		holder.setText(getGroupItem(group));
	}

	@Override
	public void onBindChildViewHolder(ChildViewHolder holder, int group, final int position) {
		super.onBindChildViewHolder(holder, group, position);
		holder.tv.setText(getChildItem(group, position));
	}

	public class ChildViewHolder extends RecyclerView.ViewHolder {
		private final TextView tv;

		public ChildViewHolder(View itemView) {
			super(itemView);
			tv = itemView.findViewById(R.id.item_name);
		}
	}
}
