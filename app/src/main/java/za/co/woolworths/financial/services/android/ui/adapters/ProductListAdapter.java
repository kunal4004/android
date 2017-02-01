package za.co.woolworths.financial.services.android.ui.adapters;


import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.awfs.coordination.R;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.Product_;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class ProductListAdapter extends RecyclerSwipeAdapter<ProductListAdapter.SimpleViewHolder> {
    public interface SelectedProduct {
        void onProductSelected(View v, int position);
    }

    private SelectedProduct mSelectedProduct;
    public List<Product_> mProductList;
    private Product_ mProduct;
    private int selectedPosition = -1;

    public ProductListAdapter(List<Product_> product_list, SelectedProduct selectedProduct) {
        this.mProductList = product_list;
        this.mSelectedProduct = selectedProduct;
    }

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llProductSearch;
        WTextView mTextSubCategoryName;

        public SimpleViewHolder(View itemView) {
            super(itemView);
            mTextSubCategoryName = (WTextView) itemView.findViewById(R.id.subCategoryName);
            llProductSearch = (LinearLayout) itemView.findViewById(R.id.llProductSearch);
            mTextSubCategoryName.setGravity(Gravity.LEFT | Gravity.CENTER);
        }
    }

    @Override
    public void onBindViewHolder(final SimpleViewHolder holder, final int position) {
        mProduct = mProductList.get(position);
        if (mProduct != null) {
            holder.mTextSubCategoryName.setText(mProduct.productName);
        }

        if (selectedPosition == position) {
            holder.llProductSearch.setBackground(ContextCompat.getDrawable(holder.llProductSearch.getContext(), R.drawable.pressed_bg));
        } else {
            holder.llProductSearch.setBackgroundColor(Color.WHITE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPosition = holder.getAdapterPosition();
                mSelectedProduct.onProductSelected(v, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ps_search_row, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mProductList.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

}