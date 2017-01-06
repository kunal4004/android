package za.co.woolworths.financial.services.android.ui.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WTextView;



/**
 * Created by W7099877 on 06/01/2017.
 */

public class WRewardsVoucherListAdapter extends RecyclerView.Adapter<WRewardsVoucherListAdapter.VouchersViewHolder> {

    Activity context;
    public WRewardsVoucherListAdapter(Activity context)
    {
        this.context=context;
    }
    @Override
    public VouchersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= context.getLayoutInflater()
                .inflate(R.layout.wrewards_vouchers_list_item, parent, false);
        return new VouchersViewHolder(v);
    }

    @Override
    public void onBindViewHolder(VouchersViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return 10;
    }

    public  class VouchersViewHolder extends RecyclerView.ViewHolder{
        WTextView voucherValue;
        WTextView voucherMessage;
        WTextView voucherExpireDate;

        public VouchersViewHolder(View cView) {
            super(cView);
            voucherValue=(WTextView)cView.findViewById(R.id.voucherValue);
            voucherMessage=(WTextView)cView.findViewById(R.id.voucherMessage);
            voucherExpireDate=(WTextView)cView.findViewById(R.id.voucherExpireDate);
        }
    }


}
