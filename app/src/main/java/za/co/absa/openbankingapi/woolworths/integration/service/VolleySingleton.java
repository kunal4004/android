package za.co.absa.openbankingapi.woolworths.integration.service;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;

public class VolleySingleton {
    private static VolleySingleton mInstance;
    private RequestQueue mRequestQueue;

    private VolleySingleton() {
        mRequestQueue = getRequestQueue();
    }

    public static synchronized VolleySingleton getInstance() {
        if (mInstance == null) {
            mInstance = new VolleySingleton();
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(WoolworthsApplication.getAppContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, Class tag) {
        req.setTag(tag.getSimpleName());
        getRequestQueue().add(req);

    }

    public void cancelRequest(String tag) {
        if (getRequestQueue() != null)
            getRequestQueue().cancelAll(tag);
    }
}