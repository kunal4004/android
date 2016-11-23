package za.co.woolworths.financial.services.android.util;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

/**
 * Created by eesajacobs on 2016/07/20.
/**
 * Formats the watched EditText to a credit card number
 */
public class FourDigitCardFormatWatcher implements TextWatcher {

    // Change this to what you want... ' ', '-' etc..
    private final char space = ' ';

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        String initial = s.toString();
        // remove all non-digits characters
        String processed = initial.replaceAll("\\D", "");
        // insert a space after all groups of 4 digits that are followed by another digit
        processed = processed.replaceAll("(\\d{4})(?=\\d)", "$1 ");
        // to avoid stackoverflow errors, check that the processed is different from what's already
        //  there before setting
        if (!initial.equals(processed)) {
            // set the value
            s.replace(0, initial.length(), processed);
        }
    }
}