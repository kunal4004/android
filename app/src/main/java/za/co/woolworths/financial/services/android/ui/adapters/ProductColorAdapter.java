package za.co.woolworths.financial.services.android.ui.adapters;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.OtherSku;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.SelectedProductView;

public class ProductColorAdapter extends RecyclerView.Adapter<ProductColorAdapter.SimpleViewHolder> {
    private List<OtherSku> mProductSizeList;
    private SelectedProductView mSelectedProductView;

    public ProductColorAdapter(List<OtherSku> mProductSizeList,
                               SelectedProductView selectedProductView) {
        this.mProductSizeList = mProductSizeList;
        this.mSelectedProductView = selectedProductView;
    }

    class SimpleViewHolder extends RecyclerView.ViewHolder {

        WTextView productName;
        SimpleDraweeView mPmColor;

        SimpleViewHolder(View view) {
            super(view);
            productName = (WTextView) view.findViewById(R.id.name);
            mPmColor = (SimpleDraweeView) view.findViewById(R.id.imColor);
        }
    }

    @Override
    public void onBindViewHolder(final SimpleViewHolder holder, final int position) {

        OtherSku productSize = mProductSizeList.get(position);
        String colour = productSize.colour;
        String imageUrl = productSize.externalColourRef;

        if (productSize != null) {
            holder.productName.setText(colour);
            if (!TextUtils.isEmpty(imageUrl)) {
                ImageRequest request = ImageRequest.fromUri(Uri.parse(imageUrl));
                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setImageRequest(request)
                        .setAutoPlayAnimations(true)
                        .setOldController(holder.mPmColor.getController()).build();
                holder.mPmColor.setController(controller);
            }
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedProductView.onSelectedProduct(v, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.color_row, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mProductSizeList.size();
    }


}