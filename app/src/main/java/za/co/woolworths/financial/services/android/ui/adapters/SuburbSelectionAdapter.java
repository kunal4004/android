package za.co.woolworths.financial.services.android.ui.adapters;

import android.graphics.Paint;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import za.co.woolworths.financial.services.android.models.dto.Suburb;
import za.co.woolworths.financial.services.android.ui.views.WEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class SuburbSelectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable, TextWatcher {

	private static int ROW_SEARCH = 0, ROW_ITEM = 1;

	public interface SuburbSelectionCallback {
		void onItemClick(Suburb province);

		void setScrollbarVisibility(boolean visible);
	}

	private SuburbSelectionCallback suburbSelectionCallback;
	private List<Suburb> suburbItems;
	private List<Suburb> suburbItemsFiltered;
	private List<HeaderPosition> headerItems = new ArrayList<>();
	private boolean isFiltering = false;

	private SearchBarViewHolder searchbarViewHolder;

	public SuburbSelectionAdapter(List<Suburb> suburbItems, SuburbSelectionCallback suburbSelectionCallback) {
		List<Suburb> suburbItemsWithHeader = configureHeaders(suburbItems);
		this.suburbItems = this.suburbItemsFiltered = suburbItemsWithHeader;
		this.suburbSelectionCallback = suburbSelectionCallback;
	}

	private List<Suburb> configureHeaders(List<Suburb> items) {
		String lastFirstChar = null;
		int idxSuburb = 0;
		for (Suburb suburb : items) {
			String currentFirstChar = Character.toString(suburb.name.charAt(0)).toUpperCase();
			if (lastFirstChar == null || (lastFirstChar != null && !lastFirstChar.equals(currentFirstChar))) {
				suburb.hasHeader = true;
				headerItems.add(new HeaderPosition(currentFirstChar.toUpperCase(), idxSuburb + 1)); // index + 1 because of search bar
			}
			lastFirstChar = currentFirstChar;
			idxSuburb++;
		}
		return items;
	}

	@Override
	public int getItemViewType(int position) {
		if (position == 0) {
			return ROW_SEARCH;
		}
		return ROW_ITEM;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == ROW_SEARCH) {
			return new SearchBarViewHolder(LayoutInflater.from(parent.getContext())
					.inflate(R.layout.suburb_selection_searchbar_item, parent, false));
		}
		return new SuburbViewHolder(LayoutInflater.from(parent.getContext())
				.inflate(R.layout.suburb_selection_item, parent, false));
	}

	@Override
	public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
		if (position == 0) {
			// search bar
			searchbarViewHolder = (SearchBarViewHolder) holder;
			searchbarViewHolder.etvSuburbFilter.addTextChangedListener(this);
		} else {
			SuburbViewHolder suburbViewHolder = (SuburbViewHolder) holder;
			final Suburb suburb = suburbItemsFiltered.get(position - 1);
			suburbViewHolder.tvSuburbName.setText(suburb.name);

			if (suburb.hasHeader && !isFiltering) {
				suburbViewHolder.tvSuburbHeader.setText(Character.toString(suburb.name.charAt(0)).toUpperCase());
				suburbViewHolder.headerLayout.setVisibility(View.VISIBLE);
			} else {
				suburbViewHolder.headerLayout.setVisibility(View.GONE);
			}

			suburbViewHolder.suburbItemLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (suburb.suburbDeliverable)
						suburbSelectionCallback.onItemClick(suburb);
				}
			});
			suburbViewHolder.suburbItemLayout.setAlpha(suburb.suburbDeliverable ? 1f : 0.5f);
			suburbViewHolder.tvSuburbName.setPaintFlags(Paint.ANTI_ALIAS_FLAG);
			if (!suburb.suburbDeliverable)
				suburbViewHolder.tvSuburbName.setPaintFlags(suburbViewHolder.tvSuburbHeader.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		}
	}

	@Override
	public int getItemCount() {
		// + 1 for search bar
		return suburbItemsFiltered.size() + 1;
	}

	@Override
	public Filter getFilter() {
		return new Filter() {

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				String charString = constraint.toString();
				List<Suburb> filteredList = new ArrayList<>();

				if (charString.isEmpty()) {
					filteredList = suburbItems;
				} else {
					String lowercaseConstraint = charString.toLowerCase();
					for (Suburb suburbItem : suburbItems) {
						if (suburbItem.name.toLowerCase().contains(lowercaseConstraint)) {
							filteredList.add(suburbItem);
						}
					}
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

	@Override
	public void afterTextChanged(Editable s) {
		// filter list
		isFiltering = s.length() > 0;
		suburbSelectionCallback.setScrollbarVisibility(!isFiltering);
		getFilter().filter(s.toString());
		if (searchbarViewHolder != null) {
			searchbarViewHolder.searchSeparator.setVisibility(!isFiltering ? View.GONE : View.VISIBLE);
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	public List<HeaderPosition> getHeaderItems() {
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

	private class SearchBarViewHolder extends RecyclerView.ViewHolder {
		public WEditTextView etvSuburbFilter;
		public View searchSeparator;

		public SearchBarViewHolder(View view) {
			super(view);
			etvSuburbFilter = view.findViewById(R.id.etvSuburbFilter);
			searchSeparator = view.findViewById(R.id.searchSeparator);
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
