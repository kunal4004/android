package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.util.List;

import retrofit.RetrofitError;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.FAQ;
import za.co.woolworths.financial.services.android.models.dto.FAQDetail;
import za.co.woolworths.financial.services.android.ui.adapters.FAQAdapter;
import za.co.woolworths.financial.services.android.ui.views.ProgressDialogFragment;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.BaseActivity;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.PopWindowValidationMessage;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.binder.view.FAQTypeBinder;


public class FAQActivity extends BaseActivity implements FAQTypeBinder.SelectedQuestion {

    private FragmentManager fm;
    private ProgressDialogFragment mGetProgressDialog;
    private RecyclerView mRecycleView;
    private FAQActivity mContext;
    private Toolbar mToolbar;
    private List<FAQDetail> mFAQ;
    private ConnectionDetector mConnectionDetector;
    //private PopWindowValidationMessage mPopWindowValidaitonMessage;
    private WTextView mtextNotFound;
    private ErrorHandlerView mErrorHandlerView;
    private WoolworthsApplication mWoolworthsApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.updateStatusBarBackground(this);
        setContentView(R.layout.faq_activity);
        mContext = this;
        mConnectionDetector = new ConnectionDetector();
       // mPopWindowValidaitonMessage = new PopWindowValidationMessage(this);
        mWoolworthsApplication = (WoolworthsApplication) getApplication();
        mErrorHandlerView = new ErrorHandlerView(this, mWoolworthsApplication,
                (RelativeLayout) findViewById(R.id.relEmptyStateHandler),
                (ImageView) findViewById(R.id.imgEmpyStateIcon),
                (WTextView) findViewById(R.id.txtEmptyStateTitle),
                (WTextView) findViewById(R.id.txtEmptyStateDesc),
                (RelativeLayout) findViewById(R.id.no_connection_layout));
        initUI();
        setActionBar();
        getFAQRequest();
        findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (new ConnectionDetector().isOnline(FAQActivity.this)) {
                    getFAQRequest();
                }
            }

        });

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
        mtextNotFound = (WTextView) findViewById(R.id.textNotFound);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissProgress();
    }

    private void getFAQRequest() {
        fm = getSupportFragmentManager();
        mGetProgressDialog = ProgressDialogFragment.newInstance();
        mGetProgressDialog.setCancelable(false);
            new HttpAsyncTask<String, String, FAQ>() {
                @Override
                protected FAQ httpDoInBackground(String... params) {
                    return ((WoolworthsApplication) FAQActivity.this.getApplication()).getApi()
                            .getFAQ();
                }

                @Override
                protected FAQ httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                    Log.e("errorMessage", errorMessage+" "+httpErrorCode);
                    dismissProgress();
                    networkFailureHandler(errorMessage);
                    return new FAQ();
                }


                @Override
                protected Class<FAQ> httpDoInBackgroundReturnType() {
                    return FAQ.class;
                }

                @Override
                protected void onPreExecute() {
                    mErrorHandlerView.hideErrorHandlerLayout();
                    try {
                        if (!mGetProgressDialog.isAdded()) {
                            mGetProgressDialog.show(fm, "v");
                        } else {
                            mGetProgressDialog.dismiss();
                            mGetProgressDialog = ProgressDialogFragment.newInstance();
                            mGetProgressDialog.show(fm, "v");
                        }

                    } catch (NullPointerException ignored) {
                    }
                    super.onPreExecute();
                }

                @Override
                protected void onPostExecute(FAQ faq) {
                    super.onPostExecute(faq);
                    mFAQ = faq.faqs;
                    if (mFAQ != null) {
                        if (mFAQ.size() > 0) {
                            FAQAdapter mFAQAdapter = new FAQAdapter(mFAQ, mContext);
                            LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
                            mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                            mRecycleView.setLayoutManager(mLayoutManager);
                            mRecycleView.setNestedScrollingEnabled(false);
                            mRecycleView.setAdapter(mFAQAdapter);
                            mFAQAdapter.setCLIContent();
                            mtextNotFound.setVisibility(View.GONE);
                            mRecycleView.setVisibility(View.VISIBLE);
                        } else {
                            mtextNotFound.setVisibility(View.VISIBLE);
                            mRecycleView.setVisibility(View.GONE);
                        }
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
        }, 50);
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
    public void networkFailureHandler(final String errorMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mErrorHandlerView.networkFailureHandler(errorMessage);
            }
        });
    }
}