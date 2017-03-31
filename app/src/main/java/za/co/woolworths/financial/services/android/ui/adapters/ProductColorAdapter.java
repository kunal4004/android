package za.co.woolworths.financial.services.android.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.OtherSku;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.SelectedProductView;

public class ProductColorAdapter extends RecyclerView.Adapter<ProductColorAdapter.SimpleViewHolder> {
    private List<OtherSku> mProductColorList;
    private SelectedProductView mSelectedProductView;

    public ProductColorAdapter(List<OtherSku> mProductColorList,
                               SelectedProductView selectedProductView) {
        this.mProductColorList = mProductColorList;
        this.mSelectedProductView = selectedProductView;
    }

    class SimpleViewHolder extends RecyclerView.ViewHolder {

        WTextView productName;

        SimpleViewHolder(View view) {
            super(view);
            productName = (WTextView) view.findViewById(R.id.name);
        }
    }

    @Override
    public void onBindViewHolder(final SimpleViewHolder holder, final int position) {

        OtherSku mProductColor = mProductColorList.get(position);
        String colour = mProductColor.colour;
        holder.productName.setText(colour);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedProductView.onSelectedColor(v, holder.getAdapterPosition());
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
        return mProductColorList.size();
    }


}