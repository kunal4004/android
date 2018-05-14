package za.co.woolworths.financial.services.android.models.rest.cli;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.UpdateBankDetail;
import za.co.woolworths.financial.services.android.models.dto.UpdateBankDetailResponse;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

/**
 * Created by W7099877 on 2017/11/04.
 */

public class CLIUpdateBankDetails extends HttpAsyncTask<String, String, UpdateBankDetailResponse> {

	private UpdateBankDetail updateBankDetail;
	private WoolworthsApplication mWoolworthsApp;
	private OnEventListener<UpdateBankDetailResponse> mCallBack;
	private Context mContext;
	public String mException;

	public CLIUpdateBankDetails(Context context, UpdateBankDetail updateBankDetail, OnEventListener callback)
	{
		this.mContext = context;
		this.updateBankDetail = updateBankDetail;
		this.mCallBack = callback;
		this.mWoolworthsApp = ((WoolworthsApplication) ((AppCompatActivity) mContext).getApplication());
	}

	@Override
	protected UpdateBankDetailResponse httpDoInBackground(String... params) {
		return mWoolworthsApp.getApi().cliUpdateBankDetail(updateBankDetail);
	}

	@Override
	protected UpdateBankDetailResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		this.mException = errorMessage;
		mCallBack.onFailure(errorMessage);
		return new UpdateBankDetailResponse();
	}

	@Override
	protected Class<UpdateBankDetailResponse> httpDoInBackgroundReturnType() {
		return UpdateBankDetailResponse.class;
	}

	@Override
	protected void onPostExecute(UpdateBankDetailResponse updateBankDetailResponse) {
		super.onPostExecute(updateBankDetailResponse);
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(updateBankDetailResponse);
			}
		}
	}
}
