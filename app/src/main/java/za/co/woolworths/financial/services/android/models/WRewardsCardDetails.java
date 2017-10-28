package za.co.woolworths.financial.services.android.models;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.dto.BankAccountTypes;
import za.co.woolworths.financial.services.android.models.dto.CardDetailsResponse;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

/**
 * Created by W7099877 on 2017/10/28.
 */

public class WRewardsCardDetails extends HttpAsyncTask<String,String,CardDetailsResponse> {
	private Context mContext;
	private WoolworthsApplication mWoolworthsApp;
	private OnEventListener<CardDetailsResponse> mCallBack=null;

	public WRewardsCardDetails(Context mContext) {
		this.mContext=mContext;
		mWoolworthsApp = ((WoolworthsApplication) ((AppCompatActivity) mContext).getApplication());
	}

	public WRewardsCardDetails(Context mContext, OnEventListener mCallBack) {
		this.mContext=mContext;
		this.mCallBack=mCallBack;
		mWoolworthsApp = ((WoolworthsApplication) ((AppCompatActivity) mContext).getApplication());
	}

	@Override
	protected CardDetailsResponse httpDoInBackground(String... params) {

		return mWoolworthsApp.getApi().getCardDetails();
	}

	@Override
	protected CardDetailsResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		if(mCallBack!=null)
		{
			mCallBack.onFailure(errorMessage);
		}
		return new CardDetailsResponse();
	}

	@Override
	protected Class<CardDetailsResponse> httpDoInBackgroundReturnType() {
		return CardDetailsResponse.class;
	}

	@Override
	protected void onPostExecute(CardDetailsResponse cardDetailsResponse) {
		super.onPostExecute(cardDetailsResponse);
		if(mCallBack!=null)
		{
			mCallBack.onSuccess(cardDetailsResponse);
		}
	}
}
