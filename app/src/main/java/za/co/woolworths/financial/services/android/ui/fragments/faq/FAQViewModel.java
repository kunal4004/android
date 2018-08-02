package za.co.woolworths.financial.services.android.ui.fragments.faq;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.FAQ;
import za.co.woolworths.financial.services.android.models.dto.FAQDetail;
import za.co.woolworths.financial.services.android.models.rest.faq.FAQRequest;
import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.rx.SchedulerProvider;

public class FAQViewModel extends BaseViewModel<FAQNavigator> {

	public FAQViewModel() {
		super();
	}

	public FAQViewModel(SchedulerProvider schedulerProvider) {
		super(schedulerProvider);
	}

	public FAQRequest faqRequest() {
		setIsLoading(true);
		return new FAQRequest(new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				FAQ faq = (FAQ) object;
				switch (faq.httpCode) {
					case 200:
						List<FAQDetail> faqList = faq.faqs;
						if (faqList != null) {
							getNavigator().faqSuccessResponse(faqList);
						}
						break;

					default:
						if (faq.response != null) {
							getNavigator().unhandledResponseCode(faq.response);
						}
						break;
				}
				setIsLoading(false);
			}

			@Override
			public void onFailure(String e) {
				getNavigator().failureResponseHandler(e);
				setIsLoading(false);
			}
		});
	}


}
