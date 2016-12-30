package za.co.woolworths.financial.services.android.ui.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.awfs.coordination.R;

import java.text.NumberFormat;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.ui.views.WEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;

public class LoanWithdrawalActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private WoolworthsApplication woolworthsApplication;
    private WEditTextView mEditWithdrawalAmount;
    private WTextView mTextAvailableFund;
    private WTextView mTextCreditLimit;
    private MenuItem menuItem;
    private boolean isNextArrow  = false;
    private String current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.updateStatusBarBackground(LoanWithdrawalActivity.this,R.color.purple);
        setContentView(R.layout.loan_withdrawal_activity);
        woolworthsApplication = (WoolworthsApplication)getApplication();
        setActionBar();
        initViews();
        setContent();
    }

    private void initViews() {
        mTextAvailableFund = (WTextView)findViewById(R.id.textAvailableFunds);
        mTextCreditLimit = (WTextView)findViewById(R.id.textCreditLimit);
        mEditWithdrawalAmount = (WEditTextView)findViewById(R.id.editWithdrawAmount);
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

        mEditWithdrawalAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {

                if (!s.toString().equals(current)) {
                    mEditWithdrawalAmount.removeTextChangedListener(this);

                    String replaceable = String.format("[%s \\s]", NumberFormat.getCurrencyInstance().getCurrency().getSymbol());
                    String cleanString = s.toString().replaceAll(replaceable, "").replace("R","").replace(",","");
                    double parsed;
                    try {
                        parsed = Double.parseDouble(cleanString);
                    } catch (NumberFormatException e) {
                        parsed = 0.00;
                    }

                    String formatted = Utils.formatCurrency(parsed);

                    current = formatted;
                    mEditWithdrawalAmount.setText(formatted);
                    mEditWithdrawalAmount.setSelection(formatted.length());

                    // Do whatever you want with position
                    mEditWithdrawalAmount.addTextChangedListener(this);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.loan_withdrawal_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menuItemVisible(menu,isNextArrow);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
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