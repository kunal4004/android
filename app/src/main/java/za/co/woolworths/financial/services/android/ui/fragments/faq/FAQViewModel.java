package za.co.woolworths.financial.services.android.ui.fragments.faq;

import java.util.List;

import retrofit2.Call;
import za.co.woolworths.financial.services.android.contracts.IResponseListener;
import za.co.woolworths.financial.services.android.models.dto.FAQ;
import za.co.woolworths.financial.services.android.models.dto.FAQDetail;
import za.co.woolworths.financial.services.android.models.network.CompletionHandler;
import za.co.woolworths.financial.services.android.models.network.OneAppService;
import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
import za.co.woolworths.financial.services.android.util.rx.SchedulerProvider;

public class FAQViewModel extends BaseViewModel<FAQNavigator> {

    public FAQViewModel() {
        super();
    }

    public FAQViewModel(SchedulerProvider schedulerProvider) {
        super(schedulerProvider);
    }

    public Call<FAQ> faqRequest() {
        setIsLoading(true);

        Call<FAQ> faqCall = OneAppService.INSTANCE.getFAQ();
        faqCall.enqueue(new CompletionHandler<>(new IResponseListener<FAQ>() {
            @Override
            public void onSuccess(FAQ faq) {
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
            public void onFailure(Throwable error) {
                if (error != null) {
                    getNavigator().failureResponseHandler(error.getMessage());
                }
                setIsLoading(false);
            }
        },FAQ.class));
        return faqCall;
    }
}
