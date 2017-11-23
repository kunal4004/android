package za.co.woolworths.financial.services.android.util;


import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.DecimalFormat;
import java.text.ParseException;

public class CurrencyTextWatcher implements TextWatcher {

	private DecimalFormat df;
	private DecimalFormat dfnd;
	private boolean hasFractionalPart;

	private EditText et;

	public CurrencyTextWatcher(EditText et) {
		df = new DecimalFormat("#,###.##");
		df.setDecimalSeparatorAlwaysShown(true);
		dfnd = new DecimalFormat("#,###");
		this.et = et;
		hasFractionalPart = false;
	}


	@Override
	public void afterTextChanged(Editable s) {
		et.removeTextChangedListener(this);

		try {
			int inilen, endlen;
			inilen = et.getText().length();
			String v = s.toString().replace(String.valueOf(df.getDecimalFormatSymbols().getGroupingSeparator()), "").replace(" ", "").replace("R ", "").replace("R", "");
			Number n = null;
			if (TextUtils.isEmpty(v)) {
				et.setText("");
			} else {
				try {
					n = df.parse(v);
				} catch (ParseException ignored) {
				}
				int cp = et.getSelectionStart();
				String finalAmount;
				if (hasFractionalPart) {
					finalAmount = "" + df.format(n).replace(".", " ").replace(",", " ");
				} else {
					finalAmount = "" + dfnd.format(n).replace(".", " ").replace(",", " ");
				}

				et.setText(finalAmount);
				endlen = et.getText().length();
				int sel = (cp + (endlen - inilen));
				if (sel > 0 && sel <= et.getText().length()) {
					et.setSelection(sel);
				} else {
					// place cursor at the end?
					et.setSelection(et.getText().length());
				}
			}
		} catch (NumberFormatException ignored) {
		}
		et.addTextChangedListener(this);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		hasFractionalPart = s.toString().contains(String.valueOf(df.getDecimalFormatSymbols().getDecimalSeparator()));
	}
}

