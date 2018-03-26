package za.co.woolworths.financial.services.android.models.rest.shoppinglist;

import android.text.TextUtils;

import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.AddToListResponse;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

public class PostAddToList extends HttpAsyncTask<String, String, AddToListResponse> {
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
	protected AddToListResponse httpDoInBackground(String... strings) {
		return WoolworthsApplication.getInstance().getApi().addToList(addToList,listId);
	}

	@Override
	protected AddToListResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		mException = errorMessage;
		mCallBack.onFailure(errorMessage);
		return new AddToListResponse();
	}

	@Override
	protected Class<AddToListResponse> httpDoInBackgroundReturnType() {
		return AddToListResponse.class;
	}

	@Override
	protected void onPostExecute(AddToListResponse response) {
		super.onPostExecute(response);
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(response);
			}
		}
	}
}
