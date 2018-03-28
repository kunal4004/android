package za.co.woolworths.financial.services.android.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.ShoppingList;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.dialog.AddToListInterface;

public class AddToListAdapter extends RecyclerView.Adapter<AddToListAdapter.ViewHolder> {

	private AddToListInterface addToListInterface;
	private List<ShoppingList> lists;

	public AddToListAdapter(List<ShoppingList> lists, AddToListInterface addToListInterface) {
		this.addToListInterface = addToListInterface;
		this.lists = lists;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext())
				.inflate(R.layout.add_to_list_row, parent, false));
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, final int position) {
		ShoppingList shoppingList = lists.get(position);
		holder.tvName.setText(TextUtils.isEmpty(shoppingList.listName) ? "" : shoppingList.listName);
		holder.chxAddToList.setChecked(shoppingList.viewIsSelected ? true : false);
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				int position = holder.getAdapterPosition();
				boolean viewIsSelected = lists.get(position).viewIsSelected;
				lists.get(position).viewIsSelected = !viewIsSelected;
				ShoppingList shoppingList = lists.get(position);
				notifyItemChanged(position);
				boolean active = false;
				for (ShoppingList list : lists) {
					if (list.viewIsSelected) {
						active = true;
						break;
					}
				}
				addToListInterface.onItemClick(shoppingList, active);
			}
		});
	}

	@Override
	public int getItemCount() {
		return lists.size();
	}

	public List<ShoppingList> getList() {
		return lists;
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		public WTextView tvName;
		public CheckBox chxAddToList;

		public ViewHolder(View itemView) {
			super(itemView);
			tvName = itemView.findViewById(R.id.tvName);
			chxAddToList = itemView.findViewById(R.id.chxAddToList);
		}
	}
}
