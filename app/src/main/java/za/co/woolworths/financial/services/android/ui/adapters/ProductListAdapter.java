package za.co.woolworths.financial.services.android.ui.adapters;


import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.awfs.coordination.R;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import java.util.List;
import za.co.woolworths.financial.services.android.models.dto.Product_;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class ProductListAdapter extends RecyclerSwipeAdapter<ProductListAdapter.SimpleViewHolder>{
    public Activity mContext;
    public List<Product_> mProductList;
    private Product_ mProduct;

    public ProductListAdapter(Activity mContext, List<Product_> product_list) {
        this.mContext=mContext;
        this.mProductList=product_list;
    }

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        WTextView mTextSubCategoryName;
        public SimpleViewHolder(View itemView) {
            super(itemView);
            mTextSubCategoryName=(WTextView)itemView.findViewById(R.id.subCategoryName);
            mTextSubCategoryName.setGravity(Gravity.LEFT|Gravity.CENTER);
        }
    }

    @Override
    public void onBindViewHolder(final SimpleViewHolder viewHolder, final int position) {
        mProduct = mProductList.get(position);
        if (mProduct!=null) {
            viewHolder.mTextSubCategoryName.setText(mProduct.productName);
            }
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