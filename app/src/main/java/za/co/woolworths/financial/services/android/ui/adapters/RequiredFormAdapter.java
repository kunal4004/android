package za.co.woolworths.financial.services.android.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WTextView;


public class RequiredFormAdapter extends RecyclerView.Adapter<RequiredFormAdapter.MyViewHolder> {

	private String[] item;
	private boolean imageIsVisible;

	public class MyViewHolder extends RecyclerView.ViewHolder {
		private WTextView tvTitle;
		private LinearLayout llRequiredForm;
		private ImageView imIcon;
		private View vTopLine, vBottomLine, vMiddleLine;

		public MyViewHolder(View view) {
			super(view);
			tvTitle = (WTextView) view.findViewById(R.id.tvTitle);
			llRequiredForm = (LinearLayout) view.findViewById(R.id.llRequiredForm);
			imIcon = (ImageView) view.findViewById(R.id.imIcon);
			vTopLine = view.findViewById(R.id.vTopLine);
			vBottomLine = view.findViewById(R.id.vBottomLine);
			vMiddleLine = view.findViewById(R.id.vMiddleLine);
		}
	}

	public RequiredFormAdapter(String[] item, boolean imageIsVisible) {
		this.item = item;
		this.imageIsVisible = imageIsVisible;
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new MyViewHolder(LayoutInflater.from(parent.getContext())
				.inflate(R.layout.required_form_item, parent, false));
	}

	@Override
	public void onBindViewHolder(final MyViewHolder holder, final int position) {
		if (imageIsVisible) {
			holder.imIcon.setVisibility(View.VISIBLE);
		} else {
			holder.imIcon.setVisibility(View.GONE);
		}

		switch (position) {
			case 0:
				holder.vTopLine.setVisibility(View.VISIBLE);
				holder.vMiddleLine.setVisibility(View.VISIBLE);
				break;
			default:
				holder.vMiddleLine.setVisibility(View.VISIBLE);
				break;
		}

		if (position == (getItemCount() - 1)) {
			holder.vBottomLine.setVisibility(View.VISIBLE);
		}

		holder.tvTitle.setText(item[position]);

	}

	@Override
	public int getItemCount() {
		return item.length;
	}
}

