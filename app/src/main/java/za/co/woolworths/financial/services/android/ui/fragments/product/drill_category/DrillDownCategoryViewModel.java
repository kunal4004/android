package za.co.woolworths.financial.services.android.ui.fragments.product.drill_category;

import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
import za.co.woolworths.financial.services.android.util.rx.SchedulerProvider;

public class DrillDownCategoryViewModel extends BaseViewModel<DrillDownInterface> {


	public DrillDownCategoryViewModel() {
		super();
	}

	public DrillDownCategoryViewModel(SchedulerProvider schedulerProvider) {
		super(schedulerProvider);
	}
}
