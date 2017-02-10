package za.co.woolworths.financial.services.android.util.binder.view;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.SelectedProductView;
import za.co.woolworths.financial.services.android.util.WFormatter;
import za.co.woolworths.financial.services.android.util.binder.DataBindAdapter;
import za.co.woolworths.financial.services.android.util.binder.DataBinder;

public class ProductViewBinder extends DataBinder<ProductViewBinder.ViewHolder> {


    private SelectedProductView mSelectedProduct;
    private List<ProductList> mDataSet = new ArrayList<>();

    public ProductViewBinder(DataBindAdapter dataBindAdapter, SelectedProductView selectedProductView) {
        super(dataBindAdapter);
        this.mSelectedProduct = selectedProductView;
    }

    @Override
    public ViewHolder newViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.product_view_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void bindViewHolder(final ViewHolder holder, int position) {
        ProductList productItem = mDataSet.get(position);
        if (productItem != null) {
            String productName = productItem.productName;
            double price = productItem.fromPrice;
            String imagePath = productItem.imagePath;
            Log.e("mPrice",String.valueOf(price));
            holder.productName.setText(productName);
            holder.mTextAmount.setText(holder.mTextAmount.getContext().getString(R.string.product_from) + " : "
                    + WFormatter.formatAmount(price));
            if (productItem.imagePath != null) {
                Uri imageUri = Uri.parse(imagePath);
                ImageRequest request = ImageRequest.fromUri(imageUri);
                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setImageRequest(request)
                        .setOldController(holder.mSimpleDraweeView.getController()).build();
                holder.mSimpleDraweeView.setController(controller);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public void addAll(List<ProductList> dataSet) {
        mDataSet.addAll(dataSet);
        notifyBinderDataSetChanged();
    }

    public void clear() {
        mDataSet.clear();
        notifyBinderDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        WTextView productName;
        WTextView mTextAmount;
        SimpleDraweeView mSimpleDraweeView;

        public ViewHolder(View view) {
            super(view);
            productName = (WTextView) view.findViewById(R.id.textTitle);
            mTextAmount = (WTextView) view.findViewById(R.id.textAmount);
            mSimpleDraweeView = (SimpleDraweeView) view.findViewById(R.id.imProduct);
        }
    }
}
