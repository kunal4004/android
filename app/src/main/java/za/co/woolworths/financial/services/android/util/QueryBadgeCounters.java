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

public class QueryBadgeCounters {

	public void setCartBadgeCounter() {

	}

	public void queryBadgeCounter() {
		this.loadVoucherCount().execute();
		this.loadShoppingCartCount().execute();
		this.getMessageResponse().execute();
	}

	public void configureBadges() {

	}

	private GetVoucher loadVoucherCount() {
		isUserAuthenticated();
		isC2User();
		return new GetVoucher(new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				VoucherResponse voucherResponse = (VoucherResponse) object;
				switch (voucherResponse.httpCode) {
					case 200:
						if (voucherResponse != null) {
							VoucherCollection voucherCollection = voucherResponse.voucherCollection;
							if (voucherCollection != null) {
								if (voucherCollection.vouchers != null) {
									voucherCollection.vouchers.size();
								}
							}
						}
						break;
				}
			}

			@Override
			public void onFailure(String errorMessage) {

			}
		});
	}

	private GetCartSummary loadShoppingCartCount() {
		isUserAuthenticated();
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

	private GetMessage getMessageResponse() {
		isUserAuthenticated();
		isC2User();
		return new GetMessage(new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				MessageResponse messageResponse = (MessageResponse) object;
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
}
