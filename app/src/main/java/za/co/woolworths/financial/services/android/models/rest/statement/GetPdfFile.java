package za.co.woolworths.financial.services.android.models.rest.statement;

import android.text.TextUtils;

import retrofit.client.Response;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.statement.GetStatement;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

public class GetPdfFile extends HttpAsyncTask<String, String, Response> {

    private WoolworthsApplication mWoolworthApplication;
    private OnEventListener<Response> mCallBack;
    private String mException;
    private GetStatement statement;

    public GetPdfFile(GetStatement statement, OnEventListener callback) {
        this.statement = statement;
        this.mCallBack = callback;
        this.mWoolworthApplication = WoolworthsApplication.getInstance();
    }

    @Override
    protected Response httpDoInBackground(String... params) {
        return mWoolworthApplication.getApi().getPDFResponse(statement);
    }

    @Override
    protected Response httpError(String errorMessage, HttpErrorCode httpErrorCode) {
        this.mException = errorMessage;
        mCallBack.onFailure(errorMessage);
        return new Response(null, 0, null, null, null);
    }

    @Override
    protected Class<Response> httpDoInBackgroundReturnType() {
        return Response.class;
    }

    @Override
    protected void onPostExecute(Response response) {
        super.onPostExecute(response);
        if (mCallBack != null) {
            if (TextUtils.isEmpty(mException)) {
                mCallBack.onSuccess(response);
            }
        }
    }
}
