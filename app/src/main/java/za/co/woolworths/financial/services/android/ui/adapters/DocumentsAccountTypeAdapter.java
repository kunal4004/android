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
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.BankAccountType;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.DrawImage;

/**
 * Created by W7099877 on 2017/10/05.
 */

public class DocumentsAccountTypeAdapter extends RecyclerView.Adapter<DocumentsAccountTypeAdapter.MyViewHolder> {

	public interface OnAccountTypeClick {
		void onAccountTypeClick(View view, int position);
	}

	private OnAccountTypeClick onAccountTypeClick;
	private List<BankAccountType> bankAccountTypes;
	private int selectedPosition = -1;

	public DocumentsAccountTypeAdapter(List<BankAccountType> bankAccountTypes, OnAccountTypeClick onAccountTypeClick) {
		this.bankAccountTypes = bankAccountTypes;
		this.onAccountTypeClick = onAccountTypeClick;
	}

	public class MyViewHolder extends RecyclerView.ViewHolder {
		private ImageView imgSelectBank;
		public SimpleDraweeView imBankLogo;
		private TextView tvAccountType;
		private RelativeLayout relDeaBank;

		public MyViewHolder(View view) {
			super(view);
			tvAccountType = (TextView) view.findViewById(R.id.tvBankName);
			relDeaBank = (RelativeLayout) view.findViewById(R.id.relDeaBank);
			imBankLogo = (SimpleDraweeView) view.findViewById(R.id.imBankLogo);
			imgSelectBank = (ImageView) view.findViewById(R.id.imgSelectBank);
		}

		public void bindUI(int position, final MyViewHolder holder) {
			BankAccountType bankAccountType = bankAccountTypes.get(position);
			rowTypeBorder(holder, position);
			onItemClick(holder);
			setText(holder, bankAccountType);
			onItemSelected(holder, position);
			setAccountImage(holder, bankAccountType);
		}

		public void setAccountImage(MyViewHolder holder, BankAccountType bank) {
			SimpleDraweeView imBankLogo = holder.imBankLogo;
			DrawImage drawImage = new DrawImage(imBankLogo.getContext());
			drawImage.displaySmallImage(imBankLogo, bank.accountTypeImage);
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

		private void setText(MyViewHolder holder, BankAccountType accountType) {
			holder.tvAccountType.setAllCaps(false);
			holder.tvAccountType.setText(accountType.accountType);
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
						onAccountTypeClick.onAccountTypeClick(v, position);
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
		return bankAccountTypes.size();
	}

	public void clearSelection() {
		selectedPosition = -1;
		notifyDataSetChanged();
	}
}
