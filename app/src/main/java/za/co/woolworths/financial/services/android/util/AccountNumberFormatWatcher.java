package za.co.woolworths.financial.services.android.util;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
public class AccountNumberFormatWatcher implements TextWatcher {

// Change this to what you want... ' ', '-' etc..
    private final String charValue = " ";
    private final Context mContext;
    EditText et_filed;


    public AccountNumberFormatWatcher(Context context,EditText et_filed){
        this.mContext = context;
        this.et_filed = et_filed;
    }

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
        // processed = processed.replaceAll("(\\d{5})(\\d{4})(\\d{3})(?=\\d)(?=\\d)(?=\\d)", "$1 $2 $3 ");
        int length = processed.length();
        if(length<=5){
            processed = processed.replaceAll("(\\d{5})", "$1");
        }else if (length>=6&&length<=10){
            processed = processed.replaceAll("(\\d{5})(\\d{4})", "$1 $2");
        }else {
            processed = processed.replaceAll("(\\d{5})(\\d{4})(\\d{3})", "$1 $2 $3 ");
        }

        if (length == 12){
                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(et_filed.getWindowToken(), 0);
        }
        //Remove the listener
        et_filed.removeTextChangedListener(this);

        //Assign processed text
        et_filed.setText(processed);

        try {
            et_filed.setSelection(processed.length());
        } catch (Exception e) {
            // TODO: handle exception
        }

        //Give back the listener
        et_filed.addTextChangedListener(this);
    }
}