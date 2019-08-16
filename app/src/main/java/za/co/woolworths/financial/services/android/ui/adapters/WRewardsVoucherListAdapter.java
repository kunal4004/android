package za.co.woolworths.financial.services.android.ui.adapters;

import android.app.Activity;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import java.text.ParseException;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.Voucher;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.WFormatter;


/**
 * Created by W7099877 on 06/01/2017.
 */

public class WRewardsVoucherListAdapter extends RecyclerView.Adapter<WRewardsVoucherListAdapter.VouchersViewHolder> {

    public Activity context;
    public List<Voucher> vouchers;
    public WRewardsVoucherListAdapter(Activity context, List<Voucher> vouchers)
    {
        this.context=context;
        this.vouchers=vouchers;
    }
    @Override
    public VouchersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= context.getLayoutInflater()
                .inflate(R.layout.wrewards_vouchers_list_item, parent, false);
        return new VouchersViewHolder(v);
    }

    @Override
    public void onBindViewHolder(VouchersViewHolder holder, int position) {
        Voucher mVoucher=vouchers.get(position);

        holder.voucherMessage.setText(mVoucher.description);
        if ("PERCENTAGE".equals(mVoucher.type))
        {
            holder.voucherValue.setText(String.valueOf(WFormatter.formatPercent(mVoucher.amount)));
        }
        else
        {
            holder.voucherValue.setText(String.valueOf(WFormatter.formatAmountNoDecimal(mVoucher.amount)));
        }

        try
        {
            holder.voucherExpireDate.setText(context.getString(R.string.expires)+String.valueOf(WFormatter.formatDate(mVoucher.validToDate)));
        }
        catch (ParseException e)
        {

            holder.voucherExpireDate.setText(context.getString(R.string.expires)+String.valueOf(mVoucher.validToDate));
        }

    }

    @Override
    public int getItemCount() {
        return vouchers.size();
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
