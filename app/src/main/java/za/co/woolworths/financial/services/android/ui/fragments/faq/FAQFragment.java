package za.co.woolworths.financial.services.android.ui.fragments.faq;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ProgressBar;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.FaqFragmentBinding;

import java.util.List;

import retrofit2.Call;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.models.dto.FAQ;
import za.co.woolworths.financial.services.android.models.dto.FAQDetail;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.adapters.FAQAdapter;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.fragments.faq.detail.FAQDetailFragment;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.Utils;

public class FAQFragment extends BaseFragment<FaqFragmentBinding, FAQViewModel> implements FAQNavigator, FAQAdapter.SelectedQuestion {

	private FAQViewModel faqViewModel;
	private Call<FAQ> mFAQRequest;
	private ErrorHandlerView mErrorHandlerView;
	private ProgressBar mProgressBar;
	private FAQAdapter mFAQAdapter;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		faqViewModel = ViewModelProviders.of(this).get(FAQViewModel.class);
		faqViewModel.setNavigator(this);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mProgressBar = getViewDataBinding().incProgressBar.progressCreditLimit;
		showBackNavigationIcon(true);
		setToolbarBackgroundDrawable(R.drawable.appbar_background);
		setTitle(getString(R.string.drawer_faq));
		showToolbar();
		Activity activity = getBaseActivity();
		if (activity != null) {
			mErrorHandlerView = new ErrorHandlerView(activity, getViewDataBinding().incConnectionHandler.noConnectionLayout);
		}
		executeFAQRequest();
		view.findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isNetworkConnected()) {
					executeFAQRequest();
				}
			}

		});
	}

	@Override
	public void onResume() {
		super.onResume();
		Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.FAQ_LIST);
	}

	@Override
	public FAQViewModel getViewModel() {
		return faqViewModel;
	}

	@Override
	public int getBindingVariable() {
		return BR.viewModel;
	}

	@Override
	public int getLayoutId() {
		return R.layout.faq_fragment;
	}

	@Override
	public void executeFAQRequest() {
		mErrorHandlerView.hideErrorHandler();
		mProgressBar.setVisibility(View.VISIBLE);
		mFAQRequest = getViewModel().faqRequest();
	}

	@Override
	public void faqSuccessResponse(List<FAQDetail> faqList) {
		if (faqList != null) {
			if (faqList.size() > 0) {
				mFAQAdapter = new FAQAdapter(faqList, this);
				LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
				mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
				getViewDataBinding().faqList.setLayoutManager(mLayoutManager);
				getViewDataBinding().faqList.setNestedScrollingEnabled(false);
				getViewDataBinding().faqList.setAdapter(mFAQAdapter);
				getViewDataBinding().textNotFound.setVisibility(View.GONE);
				getViewDataBinding().faqList.setVisibility(View.VISIBLE);
			} else {
				getViewDataBinding().textNotFound.setVisibility(View.VISIBLE);
				getViewDataBinding().faqList.setVisibility(View.GONE);
			}
			mProgressBar.setVisibility(View.GONE);
		}
	}

	@Override
	public void unhandledResponseCode(Response response) {
		mProgressBar.setVisibility(View.GONE);
		if (response != null) {
			if (response.desc != null) {
				Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.ERROR, response.desc);
			}
		}
	}

	@Override
	public void failureResponseHandler(final String errorMessage) {
		Activity activity = getBaseActivity();
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mErrorHandlerView.networkFailureHandler(errorMessage);
				}
			});
		}
		mProgressBar.setVisibility(View.GONE);
	}

	@Override
	public void onQuestionSelected(FAQDetail faqDetail) {
		Bundle bundle = new Bundle();
		bundle.putString("question", faqDetail.question);
		bundle.putString("answer", faqDetail.answer);
		FAQDetailFragment faqDetailFragment = new FAQDetailFragment();
		faqDetailFragment.setArguments(bundle);
		getBottomNavigator().pushFragment(faqDetailFragment);

	}

	@Override
	public void onDetach() {
		super.onDetach();
		cancelRequest(mFAQRequest);
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			showBackNavigationIcon(true);
			setToolbarBackgroundDrawable(R.drawable.appbar_background);
			setTitle(getString(R.string.drawer_faq));
			showToolbar();
			if (mFAQAdapter != null) {
				mFAQAdapter.resetIndex();
			}
		}
	}
}
