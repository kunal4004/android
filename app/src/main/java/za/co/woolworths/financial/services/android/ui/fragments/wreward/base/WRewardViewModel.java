package za.co.woolworths.financial.services.android.ui.fragments.wreward.base;

import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
import za.co.woolworths.financial.services.android.util.rx.SchedulerProvider;

public class WRewardViewModel extends BaseViewModel<WRewardNavigator> {

	public WRewardViewModel() {
		super();
	}

	public WRewardViewModel(SchedulerProvider schedulerProvider) {
		super(schedulerProvider);
	}

}
