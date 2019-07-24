package za.co.woolworths.financial.services.android.ui.adapters;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.dto.BalanceInsurance;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class BalanceInsuranceAdapter extends RecyclerView.Adapter<BalanceInsuranceAdapter.MyViewHolder> {

	public interface OnItemClick {
		void onItemClick(View view, int position);
	}

	private OnItemClick onItemClick;
	private ArrayList<BalanceInsurance> balanceList;


	public class MyViewHolder extends RecyclerView.ViewHolder {
		private View vEmptySpace;
		private WTextView tvTitle, tvDescription;
		private RelativeLayout rlBalanceInsurance;
		private View vBottomLine;


		public MyViewHolder(View view) {
			super(view);
			tvTitle = (WTextView) view.findViewById(R.id.tvTitle);
			tvDescription = (WTextView) view.findViewById(R.id.tvDescription);
			rlBalanceInsurance = (RelativeLayout) view.findViewById(R.id.rlBalanceInsurance);
			vBottomLine = view.findViewById(R.id.vBottomLine);
			vEmptySpace = view.findViewById(R.id.vEmptySpace);
		}
	}


	public BalanceInsuranceAdapter(ArrayList<BalanceInsurance> balanceList, OnItemClick onItemClick) {
		this.balanceList = balanceList;
		this.onItemClick = onItemClick;
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new MyViewHolder(LayoutInflater.from(parent.getContext())
				.inflate(R.layout.balance_insurance_item, parent, false));
	}

	@Override
	public void onBindViewHolder(final MyViewHolder holder, final int position) {
		switch (position) {
			case 0:
				holder.vEmptySpace.setVisibility(View.VISIBLE);
				holder.rlBalanceInsurance.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.top_divider_list));
				break;

			default:
				holder.vEmptySpace.setVisibility(View.GONE);
				holder.rlBalanceInsurance.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.bottom_divider_list));
				break;
		}

		if (position == (getItemCount() - 1)) {
			holder.vBottomLine.setVisibility(View.VISIBLE);
		}

		BalanceInsurance balanceInsurance = balanceList.get(position);
		holder.tvTitle.setText(balanceInsurance.title);
		holder.tvDescription.setText(balanceInsurance.description);

		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onItemClick.onItemClick(v, holder.getAdapterPosition());
			}
		});
	}

	@Override
	public int getItemCount() {
		return balanceList.size();
	}
}
