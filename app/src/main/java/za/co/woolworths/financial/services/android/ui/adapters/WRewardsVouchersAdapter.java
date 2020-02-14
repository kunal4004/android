package za.co.woolworths.financial.services.android.ui.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;

import com.awfs.coordination.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.text.ParseException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import za.co.woolworths.financial.services.android.models.dto.Voucher;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

/**
 * Created by W7099877 on 12/01/2017.
 */

public class WRewardsVouchersAdapter extends BaseAdapter {

    public Activity mContext;
    public List<Voucher> vouchers;

    public WRewardsVouchersAdapter(Activity mContext, List<Voucher> vouchers) {
        this.mContext = mContext;
        this.vouchers = vouchers;
    }

    @Override
    public int getCount() {
        return vouchers.size();
    }

    @Override
    public Object getItem(int position) {
        return vouchers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mContext.getLayoutInflater().inflate(R.layout.wrewards_voucher_details_item, parent, false);
            holder.validFromDate = (TextView) convertView.findViewById(R.id.validFrom);
            holder.validUntilDate = (TextView) convertView.findViewById(R.id.validUntil);
            holder.voucherValue = (TextView) convertView.findViewById(R.id.voucherValue);
            holder.message = (TextView) convertView.findViewById(R.id.message);
            holder.voucherNumber = (TextView) convertView.findViewById(R.id.voucherNumber);
            holder.minimumSpend = (TextView) convertView.findViewById(R.id.minSpend);
            holder.barCode = (ImageView) convertView.findViewById(R.id.barcode);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        try {
            holder.validFromDate.setText(WFormatter.formatDate(vouchers.get(position).validFromDate));
            holder.validUntilDate.setText(WFormatter.formatDate(vouchers.get(position).validToDate));
        } catch (ParseException e) {
            holder.validFromDate.setText(String.valueOf(vouchers.get(position).validFromDate));
            holder.validUntilDate.setText(String.valueOf(vouchers.get(position).validToDate));
        }

        if ("PERCENTAGE".equals(vouchers.get(position).type)) {
            holder.voucherValue.setText(String.valueOf(WFormatter.formatPercent(vouchers.get(position).amount)));
        } else {
            holder.voucherValue.setText(String.valueOf(WFormatter.formatAmountNoDecimal(vouchers.get(position).amount)));
        }
        holder.message.setText(vouchers.get(position).description);
        holder.voucherNumber.setText(WFormatter.formatVoucher(vouchers.get(position).voucherNumber));
        holder.minimumSpend.setText(String.valueOf(WFormatter.formatAmount(vouchers.get(position).minimumSpend)));
        try {
            holder.barCode.setImageBitmap(Utils.encodeAsBitmap(vouchers.get(position).voucherNumber, BarcodeFormat.CODE_128, convertView.getWidth(), 60));
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return convertView;
    }

    public class ViewHolder {
        TextView validFromDate;
        TextView validUntilDate;
        TextView voucherValue;
        TextView message;
        TextView voucherNumber;
        TextView minimumSpend;
        ImageView barCode;
    }
}
