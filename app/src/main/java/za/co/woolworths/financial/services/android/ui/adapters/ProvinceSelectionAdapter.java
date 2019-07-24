package za.co.woolworths.financial.services.android.ui.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.Province;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class ProvinceSelectionAdapter extends RecyclerView.Adapter<ProvinceSelectionAdapter.ProvinceViewHolder> {

	public interface OnItemClick {
		void onItemClick(Province province);
	}

	private OnItemClick onItemClick;
	private List<Province> provinceItems;

	public ProvinceSelectionAdapter(List<Province> provinceItems, OnItemClick onItemClick) {
		this.provinceItems = provinceItems;
		this.onItemClick = onItemClick;
	}

	@Override
	public ProvinceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ProvinceViewHolder(LayoutInflater.from(parent.getContext())
					.inflate(R.layout.province_selection_item, parent, false));
	}

	@Override
	public void onBindViewHolder(final ProvinceViewHolder holder, final int position) {
		final Province province = provinceItems.get(position);
		holder.tvProvinceName.setText(province.name);
		holder.provinceItemLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onItemClick.onItemClick(province);
			}
		});
	}

	@Override
	public int getItemCount() {
		return provinceItems.size();
	}

	public class ProvinceViewHolder extends RecyclerView.ViewHolder {
		private RelativeLayout provinceItemLayout;
		private WTextView tvProvinceName;

		public ProvinceViewHolder(View view) {
			super(view);
			provinceItemLayout = view.findViewById(R.id.provinceItemLayout);
			tvProvinceName = view.findViewById(R.id.tvProvinceName);
		}
	}
}
