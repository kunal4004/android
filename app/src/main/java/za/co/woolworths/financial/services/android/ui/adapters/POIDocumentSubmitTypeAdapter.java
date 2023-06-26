package za.co.woolworths.financial.services.android.ui.adapters;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WTextView;

/**
 * Created by W7099877 on 2017/10/05.
 */

public class POIDocumentSubmitTypeAdapter extends RecyclerView.Adapter<POIDocumentSubmitTypeAdapter.MyViewHolder> {

	public interface OnSubmitType {
		void onSubmitTypeSelected(View view, int position);
	}

	private OnSubmitType onSubmitType;
	private int selectedPosition = -1;
	private String[] submitTypes = {"Submit USDocuments Now", "Submit at a Later Date"};
	private int[] icons = {R.drawable.listview, R.drawable.ic_time24};

	public POIDocumentSubmitTypeAdapter(OnSubmitType onSubmitType) {
		this.onSubmitType = onSubmitType;
	}

	public class MyViewHolder extends RecyclerView.ViewHolder {
		private ImageView imBankLogo, imgSelectBank;
		private TextView tvAccountType;
		private RelativeLayout relDeaBank;

		public MyViewHolder(View view) {
			super(view);
			tvAccountType = (TextView) view.findViewById(R.id.tvBankName);
			relDeaBank = (RelativeLayout) view.findViewById(R.id.relDeaBank);
			imBankLogo = (ImageView) view.findViewById(R.id.imBankLogo);
			imgSelectBank = (ImageView) view.findViewById(R.id.imgSelectBank);
		}

		public void bindUI(int position, final MyViewHolder holder) {
			holder.imBankLogo.setBackgroundResource(icons[position]);
			rowTypeBorder(holder, position);
			onItemClick(holder);
			setText(holder, submitTypes[position]);
			onItemSelected(holder, position);
		}

		private void onItemSelected(final MyViewHolder holder, int position) {
			AssetManager asset = holder.imgSelectBank.getContext().getAssets();
			if (selectedPosition == position) {
				holder.imgSelectBank.setBackgroundResource(R.drawable.tick_cli_active);
				holder.tvAccountType.setTypeface(Typeface.createFromAsset(asset, "fonts/WFutura-SemiBold.ttf"));
				textViewColor(holder, R.color.black);
			} else {
				holder.imgSelectBank.setBackgroundResource(R.drawable.tick_cli_inactive);
				holder.tvAccountType.setTypeface(Typeface.createFromAsset(asset, "fonts/WFutura-Medium.ttf"));
				textViewColor(holder, R.color.black_50);
			}
		}

		private void setText(MyViewHolder holder, String submitType) {
			holder.tvAccountType.setAllCaps(false);
			holder.tvAccountType.setText(submitType);
		}

		private void rowTypeBorder(MyViewHolder holder, int position) {
			switch (position) {
				case 0:
					rowBackground(holder, R.drawable.top_divider_list);
					break;
				default:
					rowBackground(holder, R.drawable.bottom_divider_list);
					break;
			}
			if (position == (getItemCount() - 1)) {
				holder.itemView.setVisibility(View.VISIBLE);
			}
		}

		private void onItemClick(final MyViewHolder holder) {
			holder.itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int position = holder.getAdapterPosition();
					if(selectedPosition == -1 || position!=selectedPosition) {
						selectedPosition = position;
						onSubmitType.onSubmitTypeSelected(v, position);
						notifyDataSetChanged();
					}
				}
			});
		}

		private void rowBackground(final MyViewHolder holder, int id) {
			holder.itemView.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), id));
		}

		private void textViewColor(final MyViewHolder holder, int id) {
			holder.tvAccountType.setTextColor(ContextCompat.getColor(holder.tvAccountType.getContext(), id));
		}
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new MyViewHolder(LayoutInflater.from(parent.getContext())
				.inflate(R.layout.cli_documents_account_type_item, parent, false));
	}

	@Override
	public void onBindViewHolder(MyViewHolder holder, int position) {
		holder.bindUI(position, holder);
	}


	@Override
	public int getItemCount() {
		return submitTypes.length;
	}

	public void clearSelection() {
		selectedPosition = -1;
		notifyDataSetChanged();
	}
}
