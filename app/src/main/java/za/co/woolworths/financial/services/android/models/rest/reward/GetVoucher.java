package za.co.woolworths.financial.services.android.models.rest.reward;

import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.SessionUtilities;

public class GetVoucher extends HttpAsyncTask<String, String, VoucherResponse> {

	private OnEventListener mCallBack;
	private String mException;

	public GetVoucher(OnEventListener callback) {
		mCallBack = callback;
	}

	@Override
	protected VoucherResponse httpDoInBackground(String... params) {
		return WoolworthsApplication.getInstance().getApi().getVouchers();
	}

	@Override
	protected VoucherResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		mException = errorMessage;
		mCallBack.onFailure(errorMessage);
		return new VoucherResponse();
	}

	@Override
	protected Class<VoucherResponse> httpDoInBackgroundReturnType() {
		return VoucherResponse.class;
	}

	@Override
	protected void onPostExecute(VoucherResponse voucherResponse) {
		super.onPostExecute(voucherResponse);
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(voucherResponse);
			}
		}
	}

	@Override
	protected void onPreExecute() {
		if (!SessionUtilities.getInstance().isC2User())
			this.cancel(true);
		super.onPreExecute();
	}
}
