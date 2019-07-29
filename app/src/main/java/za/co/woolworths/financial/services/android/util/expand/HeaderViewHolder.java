package za.co.woolworths.financial.services.android.util.expand;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ImageLoader;

public class HeaderViewHolder extends RecyclerView.ViewHolder {

	private Context mContext;
	private WTextView tvCategoryName;
	private ImageView imProductCategory;
	private ImageView imClose;

	HeaderViewHolder(View view, Context mContext) {
		super(view);
		this.mContext = mContext;
		tvCategoryName = view.findViewById(R.id.tvCategoryName);
		imProductCategory = view.findViewById(R.id.imProductCategory);
		imClose = view.findViewById(R.id.imClose);
	}

	public void bind(final SubCategoryModel subCategoryModel) {
		tvCategoryName.setText(subCategoryModel.getName());

		new ImageLoader.Builder()
				.setContext(mContext)
				.setImageUrl(subCategoryModel.getImageUrl())
				.into(imProductCategory)
				.load();
	}

	public ImageView getImClose() {
		return imClose;
	}

}