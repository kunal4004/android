package za.co.woolworths.financial.services.android.ui.activities.dashboard;

import android.app.Activity;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.CartSummary;
import za.co.woolworths.financial.services.android.models.dto.CartSummaryResponse;
import za.co.woolworths.financial.services.android.models.dto.MessageResponse;
import za.co.woolworths.financial.services.android.models.dto.VoucherCollection;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.models.rest.message.GetMessage;
import za.co.woolworths.financial.services.android.models.rest.product.GetCartSummary;
import za.co.woolworths.financial.services.android.models.rest.reward.GetVoucher;
import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.rx.SchedulerProvider;


public class BottomNavigationViewModel extends BaseViewModel<BottomNavigator> {

	public BottomNavigationViewModel() {
		super();
	}

	public BottomNavigationViewModel(SchedulerProvider schedulerProvider) {
		super(schedulerProvider);
	}

	public GetCartSummary getCartSummary() {
		return new GetCartSummary(new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				CartSummaryResponse cartSummaryResponse = (CartSummaryResponse) object;
				if (cartSummaryResponse != null) {
					switch (cartSummaryResponse.httpCode) {
						case 200:
							if (cartSummaryResponse.data != null) {
								List<CartSummary> cartSummary = cartSummaryResponse.data;
								if (cartSummary.get(0) != null) {
									getNavigator().updateCartSummaryCount(cartSummary.get(0));
								}
							}
							break;
						case 400:
							getNavigator().cartSummaryInvalidToken();
							break;
						default:
							break;
					}
				}
			}

			@Override
			public void onFailure(String e) {

			}
		});
	}

	public GetVoucher getVoucherCount() {
		return new GetVoucher(new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				VoucherResponse voucherResponse = (VoucherResponse) object;
				if (voucherResponse != null) {
					VoucherCollection voucherCollection = voucherResponse.voucherCollection;
					if (voucherCollection != null) {
						if (voucherCollection.vouchers != null) {
							getNavigator().updateVoucherCount(voucherCollection.vouchers.size());
						}
					}
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
				if (messageResponse != null) {
					getNavigator().updateMessageCount(messageResponse.unreadCount);
				}
			}

			@Override
			public void onFailure(String errorMessage) {
			}
		});
	}
}
