package za.co.woolworths.financial.services.android.util;


import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;

import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyEditText extends TextInputEditText {

    private String current = "";
    private CurrencyEditText editText = CurrencyEditText.this;

    //properties
    private String currencySymbol = "";
    private String Separator = " ";
    private Boolean Spacing = false;
    private Boolean Delimiter = false;
    private Boolean Decimals = true;

    public CurrencyEditText(Context context) {
        super(context);
        init();
    }

    public CurrencyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CurrencyEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {

        this.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String s = editable.toString();
                if (!s.equals(current)) {
                    editText.removeTextChangedListener(this);

                    String cleanString = s.replaceAll("[R $,.]", "").replaceAll(currencySymbol, "").replaceAll("\\s+", "");

                    if (cleanString.length() != 0) {
                        try {

                            String currencyFormat = "";
                            if (Spacing) {
                                if (Delimiter) {
                                    currencyFormat = currencySymbol + ". ";
                                } else {
                                    currencyFormat = currencySymbol + " ";
                                }
                            } else {
                                if (Delimiter) {
                                    currencyFormat = currencySymbol + ".";
                                } else {
                                    currencyFormat = currencySymbol;
                                }
                            }

                            double parsed;
                            int parsedInt;
                            String formatted;

                            if (Decimals) {
                                parsed = Double.parseDouble(cleanString);
                                formatted = NumberFormat.getCurrencyInstance(Locale.US).format((parsed / 100)).replace(NumberFormat.getCurrencyInstance(Locale.US).getCurrency().getSymbol(), "R ");
                            } else {
                                parsedInt = Integer.parseInt(cleanString);
                                formatted = TextUtils.isEmpty(currencySymbol) ? "R " : "" + NumberFormat.getNumberInstance(Locale.US).format(parsedInt);
                            }

                            formatted = formatted.replaceAll(",", " ").replace("$", currencyFormat);
                            current = formatted;

                            //if decimals are turned off and Separator is set as anything other than commas..
                            if (!Separator.equals(" ") && !Decimals) {
                                //..replace the commas with the new separator
                                String text = formatted.replaceAll(" ", Separator);
                                if(TextUtils.isEmpty(currencySymbol)){
                                    formatted = formatted.replace("R ", currencyFormat);
                                }
                                editText.setText(formatted);
                            } else {
                                //since no custom separators were set, proceed with comma separation
                                if(TextUtils.isEmpty(currencySymbol)){
                                    formatted = formatted.replace("R ", currencyFormat);
                                }
                                editText.setText(formatted);
                            }
                            try {
                                editText.setSelection(formatted.length());
                            } catch (IndexOutOfBoundsException ex) {
                                editText.setSelection(formatted.length() - 1);
                            }
                        } catch (NumberFormatException e) {

                        }
                    }

                    editText.addTextChangedListener(this);
                }
            }
        });
    }

    public void setDecimals(boolean value) {
        this.Decimals = value;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public void setSpacing(boolean value) {
        this.Spacing = value;
    }

    public void setDelimiter(boolean value) {
        this.Delimiter = value;
    }

    public void setCurrent(String value) {
        this.current = value;
    }


    /**
     * Separator allows a custom symbol to be used as the thousand separator. Default is set as comma (e.g: 20,000)
     * <p>
     * Custom Separator cannot be set when Decimals is set as `true`. Set Decimals as `false` to continue setting up custom separator
     *
     * @value is the custom symbol sent in place of the default comma
     */
    public void setSeparator(String value) {
        this.Separator = value;
    }
}