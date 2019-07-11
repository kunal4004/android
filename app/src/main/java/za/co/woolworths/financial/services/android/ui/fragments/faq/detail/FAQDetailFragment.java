package za.co.woolworths.financial.services.android.ui.fragments.faq.detail;

import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.Html;
import android.text.style.URLSpan;
import android.view.View;
import android.webkit.URLUtil;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.FaqDetailBinding;

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.fragments.faq.web.WebFragment;
import za.co.woolworths.financial.services.android.util.Utils;

public class FAQDetailFragment extends BaseFragment<FaqDetailBinding, FAQDetailViewModel> implements FAQDetailNavigator {

	private FAQDetailViewModel faqDetailViewModel;
	private String mQuestion;
	private String mAnswer;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bundle = getArguments();
		if (bundle != null) {
			mQuestion = bundle.getString("question");
			mAnswer = bundle.getString("answer");
		}

		faqDetailViewModel = ViewModelProviders.of(this).get(FAQDetailViewModel.class);
		faqDetailViewModel.setNavigator(this);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setToolbarBackgroundDrawable(R.drawable.appbar_background);
		showBackNavigationIcon(true);
		setTitle(getString(R.string.drawer_faq));
		showToolbar();
		populateTextView();
	}

	@Override
	public void onResume() {
		super.onResume();
		Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.FAQ_DETAIL);
	}

	@Override
	public FAQDetailViewModel getViewModel() {
		return faqDetailViewModel;
	}

	@Override
	public int getBindingVariable() {
		return BR.viewModel;
	}

	@Override
	public int getLayoutId() {
		return R.layout.faq_detail;
	}

	private void populateTextView() {
		getViewDataBinding().title.setText(mQuestion);
		getViewDataBinding().description.setText(Html.fromHtml(mAnswer));
		getViewDataBinding().description.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				URLSpan spans[] = getViewDataBinding().description.getUrls();
				if (spans.length != 0) {
					String url = spans[0].getURL();
					if (URLUtil.isValidUrl(url)) {
						Bundle bundle = new Bundle();
						bundle.putString("web_url", url);
						WebFragment webFragment = new WebFragment();
						webFragment.setArguments(bundle);
						pushFragment(webFragment);
					}
				}
			}
		});
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			showBackNavigationIcon(true);
			setToolbarBackgroundDrawable(R.drawable.appbar_background);
			setTitle(getString(R.string.drawer_faq));
			showToolbar();
		}
	}
}
