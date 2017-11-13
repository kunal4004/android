package za.co.woolworths.financial.services.android.models.rest;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.CliPoiOriginResponse;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

/**
 * Created by W7099877 on 2017/11/08.
 */

public class CLIPOIOriginRequest extends HttpAsyncTask<String,String,CliPoiOriginResponse> {
	private WoolworthsApplication mWoolworthsApp;
	private OnEventListener<CliPoiOriginResponse> mCallBack;
	private Context mContext;
	public String mException;
	public int cliID;
	public int productOfferingId;
	public CLIPOIOriginRequest(Context context,int cliID,int productOfferingId,OnEventListener callback) {
		this.mContext = context;
		this.mCallBack = callback;
		this.mWoolworthsApp = ((WoolworthsApplication) ((AppCompatActivity) mContext).getApplication());
		this.cliID=cliID;
		this.productOfferingId=productOfferingId;
	}

	@Override
	protected CliPoiOriginResponse httpDoInBackground(String... params) {
		return mWoolworthsApp.getApi().cliPOIOriginRequest(cliID,productOfferingId);
	}

	@Override
	protected CliPoiOriginResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		this.mException = errorMessage;
		mCallBack.onFailure(errorMessage);
		return new CliPoiOriginResponse();
	}

	@Override
	protected Class<CliPoiOriginResponse> httpDoInBackgroundReturnType() {
		return CliPoiOriginResponse.class;
	}

	@Override
	protected void onPostExecute(CliPoiOriginResponse cliPoiOriginResponse) {
		super.onPostExecute(cliPoiOriginResponse);
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(cliPoiOriginResponse);
			}
		}
	}
}
