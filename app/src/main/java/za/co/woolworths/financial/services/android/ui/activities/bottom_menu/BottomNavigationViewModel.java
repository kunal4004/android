package za.co.woolworths.financial.services.android.ui.activities.bottom_menu;

import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
import za.co.woolworths.financial.services.android.util.rx.SchedulerProvider;


public class BottomNavigationViewModel extends BaseViewModel<BottomNavigator> {

	public BottomNavigationViewModel() {
		super();
	}

	public BottomNavigationViewModel(SchedulerProvider schedulerProvider) {
		super(schedulerProvider);
	}

}
