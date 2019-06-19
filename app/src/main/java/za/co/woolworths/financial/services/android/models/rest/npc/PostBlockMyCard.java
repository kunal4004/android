package za.co.woolworths.financial.services.android.models.rest.npc;

import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.npc.BlockCardRequestBody;
import za.co.woolworths.financial.services.android.models.dto.npc.BlockMyCardResponse;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

public class PostBlockMyCard extends HttpAsyncTask<String, String, BlockMyCardResponse> {

    private OnEventListener<BlockMyCardResponse> mCallBack;
    private String mException;
    private BlockCardRequestBody blockCardRequestBody;
    private String productOfferingId;

    public PostBlockMyCard(BlockCardRequestBody blockCardRequestBody, String productOfferingId, OnEventListener callback) {
        this.blockCardRequestBody = blockCardRequestBody;
        this.productOfferingId = productOfferingId;
        this.mCallBack = callback;
    }

    @Override
    protected BlockMyCardResponse httpDoInBackground(String... params) {
        return WoolworthsApplication.getInstance().getApi().postBlockMyCard(blockCardRequestBody, productOfferingId);
    }

    @Override
    protected BlockMyCardResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
        this.mException = errorMessage;
        mCallBack.onFailure(errorMessage);
        return new BlockMyCardResponse();
    }

    @Override
    protected Class<BlockMyCardResponse> httpDoInBackgroundReturnType() {
        return BlockMyCardResponse.class;
    }

    @Override
    protected void onPostExecute(BlockMyCardResponse blockMyCardResponse) {
        super.onPostExecute(blockMyCardResponse);
        if (mCallBack != null) {
            if (TextUtils.isEmpty(mException)) {
                mCallBack.onSuccess(blockMyCardResponse);
            }
        }
    }
}
