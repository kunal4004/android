package za.co.woolworths.financial.services.android.models;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;

import okio.Buffer;
import za.co.woolworths.financial.services.android.models.dao.ApiRequestDao;
import za.co.woolworths.financial.services.android.models.dao.ApiResponseDao;

/**
 * Created by eesajacobs on 2016/12/29.
 */

public class WfsApiInterceptor implements Interceptor {
    public static final String TAG = "WfsApiInterceptor";
    private final Context mContext;
    private final Gson gson;

    public WfsApiInterceptor(Context mContext) {
        this.mContext = mContext;
        this.gson = new GsonBuilder().create();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Log.i(TAG, "inside intercept callback");

        Request request = chain.request();
        long t1 = System.nanoTime();

        String requestLog = String.format("Sending request %s on %s%n%s", request.url(), chain.connection(), request.headers());
        Log.d(TAG, "request" + "\n" + requestLog);

        String cacheTimeHeaderValue = request.header("cacheTime");
        final long cacheTime = Integer.parseInt(cacheTimeHeaderValue == null ? "0" : cacheTimeHeaderValue);//cache time in seconds

        if (cacheTime == 0) {
            return chain.proceed(request);
        }

        final String endpoint = request.url().toString();
        final String headers = request.headers().toString();
        final String parametersJson = (request.method().compareToIgnoreCase("post") == 0 ? bodyToString(request) : "{}");

        ApiRequestDao apiRequestDao = new ApiRequestDao(mContext, cacheTime).get(request.method(), endpoint, headers, parametersJson);
        ApiResponseDao apiResponseDao = new ApiResponseDao(this.mContext).getByApiRequestId(apiRequestDao.id);
        if (apiResponseDao.id != null) {  //cache exists. return cached response

            return new Response.Builder()
                    .code(apiResponseDao.code)
                    .message(apiResponseDao.message)
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_0)
                    .body(ResponseBody.create(MediaType.parse(apiResponseDao.contentType), apiResponseDao.body))
                    .build();
        }

        //cache does not exist. Proceed with service call.
        com.squareup.okhttp.Response response = chain.proceed(request);

        //save the newly created apiRequestDao
        apiRequestDao.save();

        apiResponseDao.apiRequestId = apiRequestDao.id;
        apiResponseDao.message = response.message();
        apiResponseDao.code = response.code();
        apiResponseDao.headers = response.headers().toString();
        apiResponseDao.body = response.body().string();
        apiResponseDao.contentType = response.body().contentType().toString();

        //save the newly created apiResponseDao
        apiResponseDao.save();
        long t2 = System.nanoTime();

        String responseLog = String.format("Received response for %s in %.1fms%n%s", apiResponseDao.body + response.request().url(), (t2 - t1) / 1e6d, apiResponseDao.headers);

        return response.newBuilder()
                .body(ResponseBody.create(MediaType.parse(apiResponseDao.contentType), apiResponseDao.body))
                .build();
    }

    public static String bodyToString(final Request request) {
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }
}
