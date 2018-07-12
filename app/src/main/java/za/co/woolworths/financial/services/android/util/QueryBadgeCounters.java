package za.co.woolworths.financial.services.android.util;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.CartSummary;
import za.co.woolworths.financial.services.android.models.dto.CartSummaryResponse;
import za.co.woolworths.financial.services.android.models.dto.VoucherCollection;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.models.rest.product.GetCartSummary;
import za.co.woolworths.financial.services.android.models.rest.reward.GetVoucher;

public class QueryBadgeCounters {

	public void setCartBadgeCounter() {

	}

	public void configureBadges() {
	}

	public GetVoucher loadVoucherCount() {
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

	public GetCartSummary loadShoppingCartCount() {
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
}
