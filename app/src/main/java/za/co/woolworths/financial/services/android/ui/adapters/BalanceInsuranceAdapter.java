package za.co.woolworths.financial.services.android.ui.adapters;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.awfs.coordination.R;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.dto.BalanceInsurance;
import za.co.woolworths.financial.services.android.ui.views.WTextView;


public class BalanceInsuranceAdapter extends RecyclerView.Adapter<BalanceInsuranceAdapter.MyViewHolder> {

	private ArrayList<BalanceInsurance> balanceList;

	public class MyViewHolder extends RecyclerView.ViewHolder {
		public WTextView tvTitle, tvDescription;

		public MyViewHolder(View view) {
			super(view);
			tvTitle = (WTextView) view.findViewById(R.id.tvTitle);
			tvDescription = (WTextView) view.findViewById(R.id.tvDescription);
		}
	}


	public BalanceInsuranceAdapter(ArrayList<BalanceInsurance> balanceList) {
		this.balanceList = balanceList;
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new MyViewHolder(LayoutInflater.from(parent.getContext())
				.inflate(R.layout.balance_insurance_item, parent, false));
	}

	@Override
	public void onBindViewHolder(MyViewHolder holder, int position) {

		switch (position){
			case 0:
				holder.itemView.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(),R.drawable.top_divider_list));
				break;

			default:
				holder.itemView.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(),R.drawable.bottom_divider_list));
				break;
		}

		BalanceInsurance balanceInsurance = balanceList.get(position);
		holder.tvTitle.setText(balanceInsurance.title);
		holder.tvDescription.setText(balanceInsurance.description);
	}

	@Override
	public int getItemCount() {
		return balanceList.size();
	}
}
