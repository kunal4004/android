package za.co.woolworths.financial.services.android.ui.fragments.faq;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.FAQDetail;
import za.co.woolworths.financial.services.android.models.dto.Response;

public interface FAQNavigator {

	void executeFAQRequest();

	void faqSuccessResponse(List<FAQDetail> faqList);

	void unhandledResponseCode(Response response);

	void failureResponseHandler(String e);
}
