package za.co.woolworths.financial.services.android.util;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Created by dimitrij on 2016/12/30.
 */
public class MoneyTextWatcher implements TextWatcher {


    private final WeakReference<EditText> editTextWeakReference;

    public MoneyTextWatcher(EditText editText) {
        editTextWeakReference = new WeakReference<>(editText);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        EditText editText = editTextWeakReference.get();
        if (editText == null) return;
        editText.setSelection(2);
    }

    @Override
    public void afterTextChanged(Editable editable) {
        EditText editText = editTextWeakReference.get();
        if (editText == null) return;
        String s = editable.toString();
        editText.removeTextChangedListener(this);
        String cleanString = s.toString().replaceAll("[$,.]", "").replace(" ","").replace("R ","").replace("R","");
        BigDecimal parsed = new BigDecimal(cleanString).setScale(2, BigDecimal.ROUND_FLOOR).divide(new BigDecimal(100), BigDecimal.ROUND_FLOOR);
        String formatted = NumberFormat.getCurrencyInstance().format(parsed);
        String newFormat = formatted.replace(","," ");
        Currency currency = Currency.getInstance(Locale.getDefault());
        String symbol = currency.getSymbol();
        if(newFormat.length()>0){
            newFormat = newFormat.replace(symbol,"R ");
        }
        editText.setText(newFormat);
       // editText.setSelection(newFormat.length());
        editText.setSelection(newFormat.length());
        editText.addTextChangedListener(this);
    }
}

