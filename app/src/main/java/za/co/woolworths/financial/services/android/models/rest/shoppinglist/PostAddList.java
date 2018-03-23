package za.co.woolworths.financial.services.android.models.rest.shoppinglist;

import android.text.TextUtils;

import za.co.woolworths.financial.services.android.CreateListResponse;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.CreateList;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

public class PostAddList extends HttpAsyncTask<String, String, CreateListResponse> {
	private CreateList listName;
	public OnEventListener mCallBack;
	private String mException;

	public PostAddList(OnEventListener mCallBack, CreateList listName) {
		this.mCallBack = mCallBack;
		this.listName = listName;
	}

	@Override
	protected CreateListResponse httpDoInBackground(String... strings) {
		return WoolworthsApplication.getInstance().getApi().createList(listName);
	}

	@Override
	protected CreateListResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		mException = errorMessage;
		mCallBack.onFailure(errorMessage);
		return new CreateListResponse();
	}

	@Override
	protected Class<CreateListResponse> httpDoInBackgroundReturnType() {
		return CreateListResponse.class;
	}

	@Override
	protected void onPostExecute(CreateListResponse createListResponse) {
		super.onPostExecute(createListResponse);
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(createListResponse);
			}
		}
	}
}
