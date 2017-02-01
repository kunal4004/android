package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.WindowManager;

import com.awfs.coordination.R;


import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;

public class FAQDetailActivity extends AppCompatActivity {

    private String mQuestion;
    private String mAnswer;
    private WTextView mTitle;
    private WTextView mDescription;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        Utils.updateStatusBarBackground(this);
        setContentView(R.layout.faq_detail);
        init();
        setActionBar();
        getBundle();
        populateTextView();
    }

    private void init() {
        mTitle = (WTextView) findViewById(R.id.title);
        mDescription = (WTextView) findViewById(R.id.description);
        mToolbar = (Toolbar) findViewById(R.id.mToolbar);

    }

    private void setActionBar() {
        setSupportActionBar(mToolbar);
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setDisplayShowTitleEnabled(false);
            mActionBar.setDisplayUseLogoEnabled(false);
            mActionBar.setDefaultDisplayHomeAsUpEnabled(false);
            mActionBar.setHomeAsUpIndicator(R.drawable.back24);
        }
    }

    public void getBundle() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            mQuestion = bundle.getString("Question");
            mAnswer = bundle.getString("Answer");
        }
    }

    private void populateTextView() {
        mTitle.setText(mQuestion);
        mDescription.setText(Html.fromHtml(mAnswer));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
        }
        return false;
    }

}
