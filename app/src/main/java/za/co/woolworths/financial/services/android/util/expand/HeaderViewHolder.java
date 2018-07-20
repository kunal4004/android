package za.co.woolworths.financial.services.android.util.expand;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.awfs.coordination.R;
import com.squareup.picasso.Picasso;

import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class HeaderViewHolder extends RecyclerView.ViewHolder {

	private WTextView tvCategoryName;
	private ImageView imProductCategory;
	private ImageView imClose;

	public HeaderViewHolder(View view) {
		super(view);
		tvCategoryName = view.findViewById(R.id.tvCategoryName);
		imProductCategory = view.findViewById(R.id.imProductCategory);
		imClose = view.findViewById(R.id.imClose);
	}

	public void bind(SubCategoryModel subCategoryModel) {
		Picasso.get().load(subCategoryModel.getImageUrl()).fit().into(imProductCategory);
		tvCategoryName.setText(subCategoryModel.getName());
	}

	public ImageView getImClose() {
		return imClose;
	}
}