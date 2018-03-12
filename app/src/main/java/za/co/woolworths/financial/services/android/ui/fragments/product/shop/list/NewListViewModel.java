package za.co.woolworths.financial.services.android.ui.fragments.product.shop.list;


import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
import za.co.woolworths.financial.services.android.util.rx.SchedulerProvider;

public class NewListViewModel extends BaseViewModel<NewListNavigator> {
	public NewListViewModel() {
		super();
	}

	public NewListViewModel(SchedulerProvider schedulerProvider) {
		super(schedulerProvider);
	}
}
