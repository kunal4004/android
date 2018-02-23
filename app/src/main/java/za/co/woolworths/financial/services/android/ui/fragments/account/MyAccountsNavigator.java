package za.co.woolworths.financial.services.android.ui.fragments.account;

import za.co.woolworths.financial.services.android.models.dto.MessageResponse;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;

public interface MyAccountsNavigator {
	void onVoucherResponse(VoucherResponse voucherResponse);

	void onMessageResponse(MessageResponse messageResponse);
}
