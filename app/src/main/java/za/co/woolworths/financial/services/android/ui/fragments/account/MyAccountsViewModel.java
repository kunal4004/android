package za.co.woolworths.financial.services.android.ui.fragments.account;

import java.util.List;

import io.reactivex.functions.Consumer;
import za.co.woolworths.financial.services.android.models.dto.MessageResponse;
import za.co.woolworths.financial.services.android.models.dto.Voucher;
import za.co.woolworths.financial.services.android.models.dto.VoucherCollection;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.models.rest.message.GetMessage;
import za.co.woolworths.financial.services.android.models.rest.reward.GetVoucher;
import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.rx.SchedulerProvider;

public class MyAccountsViewModel extends BaseViewModel<MyAccountsNavigator> {

	public MyAccountsViewModel() {
		super();
	}

	public MyAccountsViewModel(SchedulerProvider schedulerProvider) {
		super(schedulerProvider);
	}


	public GetVoucher getVoucher() {
		return new GetVoucher(new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				try {
					if (object != null) {
						VoucherResponse voucherResponse = ((VoucherResponse) object);
						VoucherCollection voucherCollection = voucherResponse.voucherCollection;
						if (voucherCollection != null) {
							List<Voucher> voucher = voucherCollection.vouchers;
							if (!voucher.isEmpty()) {
								getNavigator().onVoucherResponse(voucherResponse);
							}
						}
					}
				} catch (IllegalStateException ignored) {
				}
			}

			@Override
			public void onFailure(String errorMessage) {
			}
		});
	}

	public GetMessage getMessageResponse() {
		return new GetMessage(new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				MessageResponse messageResponse = (MessageResponse) object;
				getNavigator().onMessageResponse(messageResponse);
			}

			@Override
			public void onFailure(String errorMessage) {
			}
		});
	}

	public void observableCallback(Consumer consumer) {
		consumeObservable(consumer);
	}
}
