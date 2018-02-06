package za.co.woolworths.financial.services.android.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.Province;
import za.co.woolworths.financial.services.android.models.dto.Suburb;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class SuburbSelectionAdapter extends RecyclerView.Adapter<SuburbSelectionAdapter.SuburbViewHolder> implements Filterable {

	public interface OnItemClick {
		void onItemClick(Suburb province);
	}

	private OnItemClick onItemClick;
	private ArrayList<Suburb> suburbItems;
	private ArrayList<Suburb> suburbItemsFiltered;
	private ArrayList<HeaderPosition> headerItems = new ArrayList<>();
	private boolean isFiltering = false;

	public SuburbSelectionAdapter(ArrayList<Suburb> suburbItems, OnItemClick onItemClick) {
		this.suburbItems = this.suburbItemsFiltered = configureHeaders(suburbItems);
		this.onItemClick = onItemClick;
	}

	private ArrayList<Suburb> configureHeaders(ArrayList<Suburb> items) {
		String lastFirstChar = null;
		int idxSuburb = 0;
		for (Suburb suburb : items) {
			String currentFirstChar = Character.toString(suburb.title.charAt(0));
			if(lastFirstChar == null || (lastFirstChar != null && !lastFirstChar.equalsIgnoreCase(currentFirstChar))) {
				suburb.hasHeader = true;
				headerItems.add(new HeaderPosition(currentFirstChar, idxSuburb));
			}
			lastFirstChar = currentFirstChar;
			idxSuburb++;
		}
		return items;
	}

	@Override
	public SuburbViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new SuburbViewHolder(LayoutInflater.from(parent.getContext())
					.inflate(R.layout.suburb_selection_item, parent, false));
	}

	@Override
	public void onBindViewHolder(final SuburbViewHolder holder, final int position) {
		final Suburb suburb = suburbItemsFiltered.get(position);
		holder.tvSuburbName.setText(suburb.title);

		if(suburb.hasHeader && !isFiltering) {
			holder.tvSuburbHeader.setText(Character.toString(suburb.title.charAt(0)));
			holder.headerLayout.setVisibility(View.VISIBLE);
		} else {
			holder.headerLayout.setVisibility(View.GONE);
		}

		holder.suburbItemLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onItemClick.onItemClick(suburb);
			}
		});
	}

	@Override
	public int getItemCount() {
		return suburbItemsFiltered.size();
	}

	@Override
	public Filter getFilter() {
		return new Filter() {

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				String charString = constraint.toString();
				ArrayList<Suburb> filteredList = new ArrayList<>();

				if (charString.isEmpty()) {
					filteredList = suburbItems;
					isFiltering = false;
				} else {
					String lowercaseConstraint = charString.toLowerCase();
					for (Suburb suburbItem : suburbItems) {
						if (suburbItem.title.toLowerCase().contains(lowercaseConstraint)) {
							filteredList.add(suburbItem);
						}
					}
					isFiltering = true;
				}

				FilterResults filterResults = new FilterResults();
				filterResults.values = filteredList;
				return filterResults;
			}

			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				suburbItemsFiltered = (ArrayList<Suburb>) results.values;

				// refresh the list with filtered data
				notifyDataSetChanged();
			}
		};
	}

	public ArrayList<HeaderPosition> getHeaderItems() {
		return headerItems;
	}

	public class SuburbViewHolder extends RecyclerView.ViewHolder {
		private RelativeLayout suburbItemLayout;
		private LinearLayout headerLayout;
		private WTextView tvSuburbName, tvSuburbHeader;

		public SuburbViewHolder(View view) {
			super(view);
			suburbItemLayout = view.findViewById(R.id.suburbItemLayout);
			tvSuburbName = view.findViewById(R.id.tvSuburbName);

			headerLayout = view.findViewById(R.id.headerLayout);
			tvSuburbHeader = view.findViewById(R.id.tvSuburbHeader);
		}
	}

	public class HeaderPosition {
		public String title;
		public int position;

		public HeaderPosition(String title, int position) {
			this.title = title;
			this.position = position;
		}
	}
}
