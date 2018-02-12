package za.co.woolworths.financial.services.android.ui.fragments.account;

import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
import za.co.woolworths.financial.services.android.util.rx.SchedulerProvider;

public class MyAccountsViewModel extends BaseViewModel<MyAccountsNavigator> {

	public MyAccountsViewModel() {
		super();
	}

	public MyAccountsViewModel(SchedulerProvider schedulerProvider) {
		super(schedulerProvider);
	}
}