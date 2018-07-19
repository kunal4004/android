package za.co.woolworths.financial.services.android.ui.fragments.wtoday;

import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
import za.co.woolworths.financial.services.android.util.rx.SchedulerProvider;

public class WTodayViewModel extends BaseViewModel<WTodayNavigator> {

	public WTodayViewModel() {
		super();
	}

	public WTodayViewModel(SchedulerProvider schedulerProvider) {
		super(schedulerProvider);
	}

}
