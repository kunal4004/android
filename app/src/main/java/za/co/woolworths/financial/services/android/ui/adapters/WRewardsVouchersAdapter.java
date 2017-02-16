package za.co.woolworths.financial.services.android.ui.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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

import za.co.wigroup.androidutils.Util;
import za.co.woolworths.financial.services.android.models.dto.Voucher;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
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
            holder.validFromDate=(WTextView)convertView.findViewById(R.id.validFrom);
            holder.validUntilDate=(WTextView)convertView.findViewById(R.id.validUntil);
            holder.value = (WTextView) convertView.findViewById(R.id.value);
            holder.message = (WTextView) convertView.findViewById(R.id.message);
            holder.voucherNumber = (WTextView) convertView.findViewById(R.id.voucherNumber);
            holder.minimumSpend = (WTextView) convertView.findViewById(R.id.minSpend);
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
            holder.value.setText(String.valueOf(WFormatter.formatPercent(vouchers.get(position).amount)));
        } else {
            holder.value.setText(String.valueOf(WFormatter.formatAmountNoDecimal(vouchers.get(position).amount)));
        }
        holder.message.setText(vouchers.get(position).description);
        holder.voucherNumber.setText(WFormatter.formatVoucher(vouchers.get(position).voucherNumber));
        holder.minimumSpend.setText(String.valueOf(WFormatter.formatAmount(vouchers.get(position).minimumSpend)));
        try {
            holder.barCode.setImageBitmap(encodeAsBitmap(vouchers.get(position).voucherNumber, BarcodeFormat.CODE_128, convertView.getWidth(), 60));
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return convertView;
    }
    public class ViewHolder {
        WTextView validFromDate;
        WTextView validUntilDate;
        WTextView value;
        WTextView message;
        WTextView voucherNumber;
        WTextView minimumSpend;
        ImageView barCode;
    }
    Bitmap encodeAsBitmap(String contents, BarcodeFormat format, int img_width, int img_height) throws WriterException {

        String contentsToEncode = contents;
        if (contentsToEncode == null) {
            return null;
        }
        Map<EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(contentsToEncode);
        if (encoding != null) {
            hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result;
        try {
            result = writer.encode(contentsToEncode, format, img_width, img_height, hints);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }

}
