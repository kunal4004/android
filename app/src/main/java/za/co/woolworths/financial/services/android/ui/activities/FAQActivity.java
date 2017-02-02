package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.FAQ;
import za.co.woolworths.financial.services.android.models.dto.FAQDetail;
import za.co.woolworths.financial.services.android.ui.adapters.FAQAdapter;
import za.co.woolworths.financial.services.android.ui.views.WProgressDialogFragment;
import za.co.woolworths.financial.services.android.util.BaseActivity;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.binder.view.FAQTypeBinder;


public class FAQActivity extends BaseActivity implements FAQTypeBinder.SelectedQuestion {

    private FragmentManager fm;
    private WProgressDialogFragment mGetProgressDialog;
    private RecyclerView mRecycleView;
    private FAQActivity mContext;
    private Toolbar mToolbar;
    private List<FAQDetail> mFAQ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        Utils.updateStatusBarBackground(this);
        setContentView(R.layout.faq_activity);
        mContext = this;
        initUI();
        setActionBar();
        getFAQRequest();

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

    private void initUI() {
        mRecycleView = (RecyclerView) findViewById(R.id.faqList);
        mToolbar = (Toolbar) findViewById(R.id.mToolbar);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
      //  overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissProgress();
    }

    private void getFAQRequest() {
        fm = getSupportFragmentManager();
        mGetProgressDialog = WProgressDialogFragment.newInstance("faq");
        mGetProgressDialog.setCancelable(false);
        new HttpAsyncTask<String, String, FAQ>() {
            @Override
            protected FAQ httpDoInBackground(String... params) {
                return ((WoolworthsApplication) getApplication()).getApi().getFAQ();
            }

            @Override
            protected FAQ httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                dismissProgress();
                return new FAQ();
            }

            @Override
            protected Class<FAQ> httpDoInBackgroundReturnType() {
                return FAQ.class;
            }

            @Override
            protected void onPreExecute() {
                mGetProgressDialog.show(fm, "faq");
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(FAQ faq) {
                super.onPostExecute(faq);
                mFAQ = faq.faqs;
                if (mFAQ.size() > 0) {
                    FAQAdapter mFAQAdapter = new FAQAdapter(mFAQ, mContext);
                    LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
                    mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                    mRecycleView.setLayoutManager(mLayoutManager);
                    mRecycleView.setNestedScrollingEnabled(false);
                    mRecycleView.setAdapter(mFAQAdapter);
                    mFAQAdapter.setCLIContent();

                } else {

                }

                dismissProgress();
            }
        }.execute();
    }

    private void dismissProgress() {
        if (mGetProgressDialog != null && mGetProgressDialog.isVisible()) {
            mGetProgressDialog.dismiss();
        }
    }

    @Override
    public void onQuestionSelected(View v, final int position) {
        final FAQDetail faqDetail = mFAQ.get(position);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent openFAQDetail = new Intent(FAQActivity.this, FAQDetailActivity.class);
                openFAQDetail.putExtra("Question", faqDetail.question);
                openFAQDetail.putExtra("Answer", faqDetail.answer);
                startActivity(openFAQDetail);
            }
        }, 300);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return false;
    }

}