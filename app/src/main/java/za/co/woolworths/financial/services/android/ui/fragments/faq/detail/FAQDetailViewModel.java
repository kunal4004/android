package za.co.woolworths.financial.services.android.ui.fragments.faq.detail;

import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
import za.co.woolworths.financial.services.android.util.rx.SchedulerProvider;

public class FAQDetailViewModel extends BaseViewModel<FAQDetailNavigator> {

	public FAQDetailViewModel() {
		super();
	}

	public FAQDetailViewModel(SchedulerProvider schedulerProvider) {
		super(schedulerProvider);
	}

}
