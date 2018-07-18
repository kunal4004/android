package za.co.woolworths.financial.services.android.util;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.CartSummary;
import za.co.woolworths.financial.services.android.models.dto.CartSummaryResponse;
import za.co.woolworths.financial.services.android.models.dto.MessageResponse;
import za.co.woolworths.financial.services.android.models.dto.VoucherCollection;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.models.rest.message.GetMessage;
import za.co.woolworths.financial.services.android.models.rest.product.GetCartSummary;
import za.co.woolworths.financial.services.android.models.rest.reward.GetVoucher;

public abstract class QueryBadgeCounters {

	public abstract void messageCount(int number);

	public abstract void voucherCount(int number);

	public abstract void cartCount(int number);

	private GetMessage mGetMessage;
	private GetVoucher mGetVoucher;
	private GetCartSummary mGetCartCount;

	public void queryAllBadgeCounters() {
		queryVoucherCount();
		queryCartCount();
		queryMessageCount();
	}

	public void queryMessageCount() {
		isUserAuthenticated();
		isC2User();
		mGetMessage = loadMessageCount();
		mGetMessage.execute();
	}

	public void queryVoucherCount() {
		isUserAuthenticated();
		isC2User();
		mGetVoucher = loadVoucherCount();
		mGetVoucher.execute();
	}

	public void queryCartCount() {
		isUserAuthenticated();
		mGetCartCount = loadShoppingCartCount();
		mGetCartCount.execute();
	}

	private GetVoucher loadVoucherCount() {
		return new GetVoucher(new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				VoucherResponse voucherResponse = (VoucherResponse) object;
				switch (voucherResponse.httpCode) {
					case 200:
						VoucherCollection voucherCollection = voucherResponse.voucherCollection;
						if (voucherCollection != null) {
							if (voucherCollection.vouchers != null) {
								voucherCount(voucherCollection.vouchers.size());
							}
						}
						break;
					default:
						break;
				}
			}

			@Override
			public void onFailure(String errorMessage) {

			}
		});
	}

	private GetCartSummary loadShoppingCartCount() {
		return new GetCartSummary(new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				CartSummaryResponse cartSummaryResponse = (CartSummaryResponse) object;
				if (cartSummaryResponse == null) return;
				switch (cartSummaryResponse.httpCode) {
					case 200:
						if (cartSummaryResponse.data == null) return;
						List<CartSummary> cartSummary = cartSummaryResponse.data;
						if (cartSummary.get(0) != null)
							cartCount(cartSummary.get(0).totalItemsCount);
						break;
					case 400:
						break;
					default:
						break;

				}
			}

			@Override
			public void onFailure(String e) {

			}
		});
	}

	private GetMessage loadMessageCount() {
		return new GetMessage(new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				MessageResponse messageResponse = (MessageResponse) object;
				messageCount(messageResponse.unreadCount);
			}

			@Override
			public void onFailure(String errorMessage) {
			}
		});
	}

	private void isUserAuthenticated() {
		if (!SessionUtilities.getInstance().isUserAuthenticated()) return;
	}

	private void isC2User() {
		if (!SessionUtilities.getInstance().isC2User()) return;
	}

	public void cancelCounterRequest() {
		cancelRequest(mGetMessage);
		cancelRequest(mGetVoucher);
		cancelRequest(mGetCartCount);
	}

	private void cancelRequest(HttpAsyncTask httpAsyncTask) {
		if (httpAsyncTask != null) {
			if (!httpAsyncTask.isCancelled()) {
				httpAsyncTask.cancel(true);
			}
		}
	}
}
