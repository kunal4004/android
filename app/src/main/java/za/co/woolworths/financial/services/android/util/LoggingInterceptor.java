package za.co.woolworths.financial.services.android.util;

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit.RequestInterceptor;

/**
 * Created by dimitrij on 03/08/2017.
 */

public class LoggingInterceptor implements Interceptor, RequestInterceptor {
	@Override public Response intercept(Interceptor.Chain chain) throws IOException {
		Request request = chain.request();

		long t1 = System.nanoTime();
		Log.e("SpitSS",String.format("Sending request %s on %s%n%s",
				request.url(), chain.connection(), request.headers()));

		Response response = chain.proceed(request);

		long t2 = System.nanoTime();
		Log.e("SpitS3S",(String.format("Received response for %s in %.1fms%n%s",
				response.request().url(), (t2 - t1) / 1e6d, response.headers())));

		return response;
	}

	@Override
	public void intercept(RequestFacade request) {

	}
}