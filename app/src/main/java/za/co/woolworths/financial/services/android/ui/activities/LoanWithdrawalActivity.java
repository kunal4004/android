package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.awfs.coordination.R;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

import za.co.woolworths.financial.services.android.ui.views.WEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;

public class LoanWithdrawalActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private WEditTextView mEditWithdrawalAmount;
    private MenuItem menuItem;
    private boolean isNextArrow  = false;
    private WeakReference<WEditTextView> mEditTextWeakReference;
    private WTextView mTextAvailableFund;
    private WTextView mTextCreditLimit;
    private WEditTextView mEditText;
    private Menu mMenu;
    private RelativeLayout mRelLoanWithdrawal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.updateStatusBarBackground(LoanWithdrawalActivity.this,R.color.purple);
        setContentView(R.layout.loan_withdrawal_activity);
        setActionBar();
        initViews();
        setContent();
        }

    private void initViews() {
        mTextAvailableFund = (WTextView)findViewById(R.id.textAvailableFunds);
        mTextCreditLimit = (WTextView)findViewById(R.id.textCreditLimit);
        mEditWithdrawalAmount = (WEditTextView)findViewById(R.id.editWithdrawAmount);
        mRelLoanWithdrawal = (RelativeLayout)findViewById(R.id.relLoanWithdrawal);
    }

    private void setActionBar(){
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setDisplayUseLogoEnabled(false);
        mActionBar.setDefaultDisplayHomeAsUpEnabled(false);
        mActionBar.setHomeAsUpIndicator(R.drawable.close_white);
    }

    private void setContent(){
        mEditWithdrawalAmount.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
        mRelLoanWithdrawal.setVisibility(View.VISIBLE);
        mEditWithdrawalAmount.setText("R ");
        mEditWithdrawalAmount.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard();
                    LoanWithdrawalActivity.this.finish();
                    handled = true;
                }
                return handled;
            }
        });

        /**
         * Set cursor to end of text in edittext when user clicks Next on Keyboard.
         */
        View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    if (!TextUtils.isEmpty(mEditWithdrawalAmount.getText().toString())){
                        ((EditText) view).setSelection(2);
                    }
                }
            }
        };

        /*
           Add Click listener and on put your code that will keep cursor on right side
         */
        mEditWithdrawalAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditWithdrawalAmount.setSelection(mEditWithdrawalAmount.getText().length());
            }
        });

        mEditWithdrawalAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                mEditTextWeakReference = new WeakReference<>(mEditWithdrawalAmount);
                mEditText = mEditTextWeakReference.get();
                if (mEditText == null) return;
                String s = editable.toString();
                mEditText.removeTextChangedListener(this);
                String cleanString = s.toString().replaceAll("[$,.]", "").replace(" ", "").replace("R ", "").replace("R", "");
                if (TextUtils.isEmpty(cleanString)){
                    cleanString =  "0.00";
                }
                BigDecimal parsed = new BigDecimal(cleanString).setScale(2, BigDecimal.ROUND_FLOOR).divide(new BigDecimal(100), BigDecimal.ROUND_FLOOR);
                java.util.Currency usd = java.util.Currency.getInstance("USD");
                java.text.NumberFormat format = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.US);
                format.setCurrency(usd);
                String formatted = format.format(parsed);
                String newFormat = formatted.replace(",", " ");
                String symbol = format.getCurrency().getSymbol(Locale.US);
                int checkAmount =0;
                if (!TextUtils.isEmpty(newFormat)) {
                    checkAmount = Double.valueOf(newFormat.replace(symbol, "").replace(" ","")).intValue();
                }
                if (newFormat.length() > 0) {
                    newFormat = newFormat.replace(symbol, "R ");
                }
                if (checkAmount==0){
                    menuItemVisible(mMenu,false);
                }else {
                    menuItemVisible(mMenu,true);
                }
                if(newFormat.equalsIgnoreCase("R 0.00")){
                    newFormat="R ";
                }
                mEditText.setText(newFormat);
                if (newFormat.length() > 6 && !newFormat.equalsIgnoreCase("R 0.00")) {
                    mEditText.setSelection(newFormat.length());
                } else {
                    mEditText.setSelection(newFormat.length());
                }
                mEditText.addTextChangedListener(this);
            }
        });
        mEditWithdrawalAmount.setOnFocusChangeListener(onFocusChangeListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.loan_withdrawal_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        this.mMenu = menu;
        menuItemVisible(menu,isNextArrow);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                    onBackPressed();
                return true;
            case R.id.itemNextArrow:
                Intent openLoanWithdrawal = new Intent(LoanWithdrawalActivity.this,LoanWithdrawalConfirmActivity.class);
                startActivity(openLoanWithdrawal);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public void menuItemVisible(Menu menu,boolean isVisible){
        menuItem = menu.findItem(R.id.itemNextArrow);
        if (isVisible) {
            menuItem.setEnabled(true);
            menuItem.getIcon().setAlpha(255);
        } else {
            menuItem.setEnabled(false);
            menuItem.getIcon().setAlpha(50);
        }
    }

    /**
     * Hides the soft keyboard
     */
    public void hideKeyboard() {
        try {
            if (getCurrentFocus() != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        }catch (NullPointerException ex){}
    }
}