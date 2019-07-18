package za.co.woolworths.financial.services.android.ui.fragments.account;

import retrofit2.Call;
import za.co.woolworths.financial.services.android.contracts.RequestListener;
import za.co.woolworths.financial.services.android.models.dto.MessageResponse;
import za.co.woolworths.financial.services.android.models.network.CompletionHandler;
import za.co.woolworths.financial.services.android.models.network.OneAppService;
import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
import za.co.woolworths.financial.services.android.util.rx.SchedulerProvider;

public class MyAccountsViewModel extends BaseViewModel<MyAccountsNavigator> {

	public MyAccountsViewModel() {
		super();
	}

	public MyAccountsViewModel(SchedulerProvider schedulerProvider) {
		super(schedulerProvider);
	}


	public Call<MessageResponse> loadMessageCount() {
		Call<MessageResponse> messageRequestCall =  OneAppService.INSTANCE.getMessagesResponse(5, 1);
		messageRequestCall.enqueue(new CompletionHandler<>(new RequestListener<MessageResponse>() {
			@Override
			public void onSuccess(MessageResponse messageResponse) {
				getNavigator().onMessageResponse(messageResponse.unreadCount);
			}

			@Override
			public void onFailure(Throwable error) {

			}
		},MessageResponse.class));
		return messageRequestCall;
	}
}
