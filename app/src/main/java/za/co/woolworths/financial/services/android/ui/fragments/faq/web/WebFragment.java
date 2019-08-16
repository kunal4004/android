package za.co.woolworths.financial.services.android.ui.fragments.faq.web;


import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import android.view.View;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.ActivityFaqdetailsWebBinding;

import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.fragments.faq.FAQViewModel;

public class WebFragment extends BaseFragment<ActivityFaqdetailsWebBinding, FAQViewModel> {

	private String mUrl;

	FAQViewModel faqViewModel;

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
		return R.layout.activity_faqdetails_web;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();
		if (bundle != null) {
			mUrl = bundle.getString("web_url");
		}
		faqViewModel = ViewModelProviders.of(this).get(FAQViewModel.class);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setToolbarBackgroundDrawable(R.drawable.appbar_background);
		showBackNavigationIcon(true);
		setTitle(getString(R.string.drawer_faq));
		showToolbar();
		bindDateWithUI();
	}

	public void bindDateWithUI() {
		getViewDataBinding().faqWeb.getSettings().setJavaScriptEnabled(true);
		getViewDataBinding().faqWeb.loadUrl(mUrl);
	}
}
