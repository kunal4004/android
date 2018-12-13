package za.co.woolworths.financial.services.android.models.rest.shop;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.CheckoutSuccess;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.Utils;

public class PostCheckoutSuccess extends HttpAsyncTask<Void, Void, Void> {
    @Override
    protected Void httpDoInBackground(Void... voids) {
        return WoolworthsApplication.getInstance().getApi().postCheckoutSuccess(new CheckoutSuccess(Utils.getPreferredDeliveryLocation().suburb.id));
    }

    @Override
    protected Void httpError(String errorMessage, HttpErrorCode httpErrorCode) {
        return null;
    }

    @Override
    protected Class<Void> httpDoInBackgroundReturnType() {
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

    }
}
