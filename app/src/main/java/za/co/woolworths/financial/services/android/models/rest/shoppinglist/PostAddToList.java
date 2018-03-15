package za.co.woolworths.financial.services.android.models.rest.shoppinglist;

import android.text.TextUtils;

import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

public class PostAddToList extends HttpAsyncTask<String, String, Response> {
	public OnEventListener mCallBack;
	private List<AddToListRequest> addToList;
	private String mException;
	private String listId;

	public PostAddToList(OnEventListener mCallBack, List<AddToListRequest> addToList, String listId) {
		this.mCallBack = mCallBack;
		this.addToList = addToList;
		this.listId = listId;
	}

	@Override
	protected Response httpDoInBackground(String... strings) {
		return WoolworthsApplication.getInstance().getApi().addToList(addToList,listId);
	}

	@Override
	protected Response httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		mException = errorMessage;
		mCallBack.onFailure(errorMessage);
		return new Response();
	}

	@Override
	protected Class<Response> httpDoInBackgroundReturnType() {
		return Response.class;
	}

	@Override
	protected void onPostExecute(Response Response) {
		super.onPostExecute(Response);
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(Response);
			}
		}
	}
}
