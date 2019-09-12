package za.co.woolworths.financial.services.android.util.expand;

import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.ImageView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ImageManager;

public class HeaderViewHolder extends RecyclerView.ViewHolder {

    private WTextView tvCategoryName;
    private ImageView imProductCategory;
    private ImageView imClose;

    HeaderViewHolder(View view) {
        super(view);
        tvCategoryName = view.findViewById(R.id.tvCategoryName);
        imProductCategory = view.findViewById(R.id.imProductCategory);
        imClose = view.findViewById(R.id.imClose);
    }

    public void bind(final SubCategoryModel subCategoryModel) {
        tvCategoryName.setText(subCategoryModel.getName());
        ImageManager.Companion.setPictureCenterInside(imProductCategory, subCategoryModel.getImageUrl());
    }

    public ImageView getImClose() {
        return imClose;
    }

}