package za.co.woolworths.financial.services.android.models.network;

import java.io.IOException;
import java.net.SocketTimeoutException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import za.co.woolworths.financial.services.android.models.dao.ApiRequestDao;
import za.co.woolworths.financial.services.android.models.dao.ApiResponseDao;
import za.co.woolworths.financial.services.android.ui.activities.maintenance.NetworkRuntimeExceptionViewController;
import za.co.woolworths.financial.services.android.util.GZIPCompression;

/**
 * Created by eesajacobs on 2016/12/29.
 */

public class WfsApiInterceptor implements Interceptor {
    public static final String TAG = "WfsApiInterceptor";

    private NetworkConfig networkConfig;

    public WfsApiInterceptor(NetworkConfig networkConfig) {
        this.networkConfig = networkConfig;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request request = chain.request();
        long t1 = System.nanoTime();

        String requestLog = String.format("Sending request %s on %s%n%s", request.url(), chain.connection(), request.headers());

        String cacheTimeHeaderValue = request.header("cacheTime");
        long cacheTime = Integer.parseInt(cacheTimeHeaderValue == null ? "0" : cacheTimeHeaderValue);//cache time in seconds

        // getForceNetworkUpdate() will force the request to update
        if (cacheTime == 0) {
            try {
                Response originalResponse = chain.proceed(request);
                return originalResponse.newBuilder()
                        .header("Accept-Encoding", "gzip")
                        .build();
            } catch (Exception exception) {
                if (exception instanceof SocketTimeoutException) {
                    new NetworkRuntimeExceptionViewController().openSocketTimeOutDialog();
                }
                throw exception;
            }
        }

        final String endpoint = request.url().toString();
        final String headers = request.headers().toString();
        final String parametersJson = (request.method().compareToIgnoreCase("post") == 0 ? bodyToString(request) : "{}");

        ApiRequestDao apiRequestDao = new ApiRequestDao(cacheTime).get(request.method(), endpoint, headers, parametersJson);
        ApiResponseDao apiResponseDao = new ApiResponseDao().getByApiRequestId(apiRequestDao.id);

        // Override empty request type with current request type
        apiRequestDao.requestType = request.method();

        //cache exists. return cached response
        if (apiResponseDao.id != null && !OneAppService.Companion.getForceNetworkUpdate()) {
            return new Response.Builder()
                    .code(apiResponseDao.code)
                    .message(apiResponseDao.message)
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_0)
                    .body(ResponseBody.create(MediaType.parse(apiResponseDao.contentType), apiResponseDao.body))
                    .build();
        }

        //cache does not exist. Proceed with service call.
        Response response = null;
        try {
            response = chain.proceed(request);
        } catch (Exception exception) {
            if (exception instanceof SocketTimeoutException) {
                new NetworkRuntimeExceptionViewController().openSocketTimeOutDialog();
            }
            throw exception;
        }

        //save the newly created apiRequestDao
        apiRequestDao.save();

        OneAppService.Companion.setForceNetworkUpdate(false);

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
