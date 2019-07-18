package za.co.woolworths.financial.services.android.ui.adapters;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.awfs.coordination.R;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.Bank;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.DrawImage;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.MyViewHolder> {

	public interface OnItemClick {
		void onItemClick(View view, int position);
	}

	private OnItemClick onItemClick;
	private List<Bank> deaBankList;
	private int selectedPosition = -1;

	public DocumentAdapter(List<Bank> deaBankList, OnItemClick onItemClick) {
		this.deaBankList = deaBankList;
		this.onItemClick = onItemClick;
	}

	public class MyViewHolder extends RecyclerView.ViewHolder {
		private ImageView imgSelectBank;
		private SimpleDraweeView imBankLogo;
		private WTextView tvBankName;

		public MyViewHolder(View view) {
			super(view);
			tvBankName = (WTextView) view.findViewById(R.id.tvBankName);
			imBankLogo = (SimpleDraweeView) view.findViewById(R.id.imBankLogo);
			imgSelectBank = (ImageView) view.findViewById(R.id.imgSelectBank);
		}

		public void bindUI(int position, final MyViewHolder holder) {
			Bank deaBank = deaBankList.get(position);
			rowTypeBorder(holder, position);
			onItemClick(holder);
			setText(holder, deaBank);
			onItemSelected(holder, position);
			setBankImage(holder, deaBank);
		}

		private void setBankImage(MyViewHolder holder, Bank bank) {
			SimpleDraweeView imBankLogo = holder.imBankLogo;
			if (bank.bankName.equalsIgnoreCase("Other")) {
				imBankLogo.setActualImageResource(R.drawable.bank);
			} else {
				DrawImage drawImage = new DrawImage(imBankLogo.getContext());
				drawImage.displaySmallImage(imBankLogo, bank.bankImage);
			}
		}

		private void onItemSelected(final MyViewHolder holder, int position) {
			AssetManager asset = holder.imgSelectBank.getContext().getAssets();
			if (selectedPosition == position) {
				holder.imgSelectBank.setBackgroundResource(R.drawable.tick_cli_active);
				holder.tvBankName.setTypeface(Typeface.createFromAsset(asset, "fonts/WFutura-SemiBold.ttf"));
				textViewColor(holder, R.color.black);
			} else {
				holder.imgSelectBank.setBackgroundResource(R.drawable.tick_cli_inactive);
				holder.tvBankName.setTypeface(Typeface.createFromAsset(asset, "fonts/WFutura-Medium.ttf"));
				textViewColor(holder, R.color.black_50);
			}
		}

		private void setText(MyViewHolder holder, Bank bank) {
			holder.tvBankName.setText(bank.bankName);
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
					if(selectedPosition == -1 || position!=selectedPosition)
					{
					selectedPosition = position;
					onItemClick.onItemClick(v, position);
					notifyDataSetChanged();
					}
				}
			});
		}

		private void rowBackground(final MyViewHolder holder, int id) {
			holder.itemView.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), id));
		}

		private void textViewColor(final MyViewHolder holder, int id) {
			holder.tvBankName.setTextColor(ContextCompat.getColor(holder.tvBankName.getContext(), id));
		}
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new MyViewHolder(LayoutInflater.from(parent.getContext())
				.inflate(R.layout.document_fragment_row, parent, false));
	}

	@Override
	public void onBindViewHolder(final MyViewHolder holder, final int position) {
		holder.bindUI(position, holder);
	}

	@Override
	public int getItemCount() {
		return deaBankList.size();
	}
}
