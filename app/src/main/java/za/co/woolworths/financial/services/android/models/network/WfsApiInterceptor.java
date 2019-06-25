package za.co.woolworths.financial.services.android.models.network;

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import za.co.woolworths.financial.services.android.models.dao.ApiRequestDao;
import za.co.woolworths.financial.services.android.models.dao.ApiResponseDao;
import za.co.woolworths.financial.services.android.util.GZIPCompression;

/**
 * Created by eesajacobs on 2016/12/29.
 */

public class WfsApiInterceptor extends NetworkConfig implements Interceptor {
    public static final String TAG = "WfsApiInterceptor";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Log.i(TAG, "inside intercept callback");

        Request request = chain.request();
        long t1 = System.nanoTime();

        String requestLog = String.format("Sending request %s on %s%n%s", request.url(), chain.connection(), request.headers());
        Log.d(TAG,"request" + "\n" + requestLog);

        String cacheTimeHeaderValue = request.header("cacheTime");
        final long cacheTime = Integer.parseInt(cacheTimeHeaderValue == null ? "0" : cacheTimeHeaderValue);//cache time in seconds

        if (cacheTime == 0) {
            Response originalResponse = chain.proceed(request);
            return originalResponse.newBuilder()
                    .header("Accept-Encoding", "gzip")
                    .build();
        }

        final String endpoint = request.url().toString();
        final String headers = request.headers().toString();
        final String parametersJson = (request.method().compareToIgnoreCase("post") == 0 ? bodyToString(request) : "{}");

        ApiRequestDao apiRequestDao = new ApiRequestDao(cacheTime).get(request.method(), endpoint, headers, parametersJson);
        ApiResponseDao apiResponseDao = new ApiResponseDao().getByApiRequestId(apiRequestDao.id);

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
        Response response = chain.proceed(request);

        //save the newly created apiRequestDao
        apiRequestDao.save();

        apiResponseDao.apiRequestId = apiRequestDao.id;
        apiResponseDao.message = response.message();
        apiResponseDao.code = response.code();
        apiResponseDao.headers = response.headers().toString();
        apiResponseDao.body = GZIPCompression.decompress(response.body().bytes());

        apiResponseDao.contentType = response.body().contentType().toString();

        //save the newly created apiResponseDao
        apiResponseDao.save();
        long t2 = System.nanoTime();

        String responseLog = String.format("Received response for %s in %.1fms%n%s", apiResponseDao.body + response.request().url(), (t2 - t1) / 1e6d, apiResponseDao.headers);

        return response.newBuilder()
                .header("Cache-Control", "max-age=60")
                .body(ResponseBody.create(MediaType.parse(apiResponseDao.contentType), apiResponseDao.body))
                .build();
    }

    public String bodyToString(final Request request) {
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
