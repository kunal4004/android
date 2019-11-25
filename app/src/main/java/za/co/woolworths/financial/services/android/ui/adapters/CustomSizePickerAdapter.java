package za.co.woolworths.financial.services.android.ui.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.awfs.coordination.R;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.WFormatter;

import static za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment.INDEX_ADD_TO_CART;

public class CustomSizePickerAdapter extends RecyclerView.Adapter<CustomSizePickerAdapter.SimpleViewHolder> {

	private boolean mShowPrice;


	public interface RecyclerViewClickListener {
		void recyclerViewListClicked(View v, int position);

		void onOutOfStockItemClicked(OtherSkus otherSkus);
	}

	private RecyclerViewClickListener mRecyclerViewClickListener;
	private final ArrayList<OtherSkus> mOtherSKu;
	private WoolworthsApplication mWoolworthApplication;

	public CustomSizePickerAdapter(ArrayList<OtherSkus> otherSkus, RecyclerViewClickListener recyclerViewClickListener, boolean showPrice) {
		this.mOtherSKu = otherSkus;
		this.mRecyclerViewClickListener = recyclerViewClickListener;
		this.mShowPrice = showPrice;
		this.mWoolworthApplication = WoolworthsApplication.getInstance();
	}

	class SimpleViewHolder extends RecyclerView.ViewHolder {

		private WTextView tvName;
		private WTextView tvPrice;
		private RelativeLayout relRootSizeLayout;

		SimpleViewHolder(View view) {
			super(view);
			tvName = view.findViewById(R.id.name);
			tvPrice = view.findViewById(R.id.tvPrice);
			relRootSizeLayout = view.findViewById(R.id.ccRootSizeLayout);
		}

		public void quantityInStock(OtherSkus otherSkus) {
			Context context = relRootSizeLayout.getContext();
			if (context == null) return;
			if (otherSkus == null) return;
			if (mWoolworthApplication == null) return;
			if (addToCartButtonWasClicked()) {
				boolean quantityIsAvailable = otherSkus.quantity > 0;
				tvName.setAlpha(quantityIsAvailable ? 1f : 0.5f);
				tvPrice.setAlpha(quantityIsAvailable ? 0.6f : 1f);
				relRootSizeLayout.setBackgroundColor(quantityIsAvailable ? ContextCompat.getColor(context, R.color.white) : ContextCompat.getColor(context, R.color.unavailable_color));
				tvName.setPaintFlags(quantityIsAvailable ? Paint.ANTI_ALIAS_FLAG : tvName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				tvPrice.setText(quantityIsAvailable ? WFormatter.formatAmount(otherSkus.price) : context.getString(R.string.stock_finder_btn_label));
			}
		}
	}

	private boolean addToCartButtonWasClicked() {
		return mWoolworthApplication.getWGlobalState().getSaveButtonClick() == INDEX_ADD_TO_CART;
	}

	@Override
	public void onBindViewHolder(final SimpleViewHolder holder, final int position) {
		final OtherSkus otherSkus = mOtherSKu.get(position);
		if (otherSkus == null) return;
		String size = TextUtils.isEmpty(otherSkus.size) ? "" : otherSkus.size.trim();
		holder.tvName.setGravity((shouldShowPrice()) ? Gravity.START : Gravity.CENTER);
		//skipping the filling of the view
		holder.tvName.setText(size);
		holder.tvPrice.setText(WFormatter.formatAmount(otherSkus.price));
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int position = holder.getAdapterPosition();
				int quantityInStock = otherSkus.quantity;
				/*****
				 * addToCartButtonWasClicked determine whether add to cart button was pressed
				 * if add to cart button was pressed and quantity is stock is 0 when user tap
				 * find in store row, perform out of stock item process i.e. auto-tap find in store button
				 * on pdp page
				 */
				if (addToCartButtonWasClicked()) {
					if (quantityInStock > 0) {
						mRecyclerViewClickListener.recyclerViewListClicked(v, position);
					} else {
						mRecyclerViewClickListener.onOutOfStockItemClicked(otherSkus);
					}
					return;
				}
				mRecyclerViewClickListener.recyclerViewListClicked(v, position);
			}
		});

		holder.quantityInStock(otherSkus);
	}

	@Override
	public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.size_item, parent, false);
		return new SimpleViewHolder(v);
	}

	@Override
	public int getItemCount() {
		if (mOtherSKu != null)
			return mOtherSKu.size();
		else
			return 0;
	}

	/**
	 * @return
	 */
	private boolean shouldShowPrice() {
		return mShowPrice;
	}

	public ArrayList<OtherSkus> getOtherSKu() {
		return mOtherSKu;
	}

	public int getPositionBySkuId(String selectedSku) {
		int index = 0;
		for (OtherSkus otherSkus : mOtherSKu) {
			if (otherSkus.sku.equalsIgnoreCase(selectedSku)) {
				return index;
			}
			index++;
		}
		return index;
	}
}