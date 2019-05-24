package za.co.woolworths.financial.services.android.models.network

import android.util.Log
import com.awfs.coordination.BuildConfig

import java.io.IOException

import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.ApiRequestDao
import za.co.woolworths.financial.services.android.models.dao.ApiResponseDao
import za.co.woolworths.financial.services.android.util.GZIPCompression

/**
 * Created by eesajacobs on 2016/12/29.
 */

class WfsApiInterceptor : Interceptor {

    companion object {
        const val TAG = "WfsApiInterceptor"
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        Log.i(TAG, "inside intercept callback")

        val request = chain.request()

        val t1 = System.nanoTime()

        val requestLog = String.format("Sending request %s on %s%n%s", request.url(), chain.connection(), request.headers())

        val cacheTimeHeaderValue = request.header("cacheTime")
        val cacheTime = Integer.parseInt(cacheTimeHeaderValue
                ?: "0").toLong()//cache time in seconds

        Log.d(TAG, "request\n$requestLog")

        if (cacheTime == 0L) {
            val originalResponse = chain.proceed(request)
            return originalResponse.newBuilder()
                    .header("Accept-Encoding", "gzip")
                    .build()
        }

        val endpoint = request.url().toString()
        var headers = request.headers().toString()

        val parametersJson = if (request.method().compareTo("post", ignoreCase = true) == 0) bodyToString(request) else "{}"

        val apiRequestDao = ApiRequestDao(cacheTime).get(request.method(), endpoint, headers, parametersJson)
        val apiResponseDao = ApiResponseDao().getByApiRequestId(apiRequestDao.id)

        if (apiResponseDao.id != null) {  //cache exists. return cached response
            return Response.Builder()
                    .code(apiResponseDao.code)
                    .message(apiResponseDao.message)
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_0)
                    .body(ResponseBody.create(MediaType.parse(apiResponseDao.contentType), apiResponseDao.body))
                    .build()
        }

        //cache does not exist. Proceed with service call.
        val response = chain.proceed(request)

        //save the newly created apiRequestDao
        apiRequestDao.save()

        apiResponseDao?.apply {
            apiRequestId = apiRequestDao.id
            message = response.message()
            code = response.code()
            headers = response.headers().toString()
            body = GZIPCompression.decompress(response.body()!!.bytes())

            contentType = response.body()!!.contentType()!!.toString()
            //save the newly created apiResponseDao
            save()

            val t2 = System.nanoTime()
            val responseLog = String.format("Received response for %s in %.1fms%n%s", body + response.request().url(), (t2 - t1) / 1e6, headers)
            Log.d(TAG, "response\n$responseLog")

        }

        return response.newBuilder()
                .header("Cache-Control", "max-age=60")
                .body(ResponseBody.create(MediaType.parse(apiResponseDao.contentType), apiResponseDao.body))
                .build()
    }

    private fun bodyToString(request: Request): String {
        return try {
            val copy = request.newBuilder().build()
            val buffer = Buffer()
            copy.body()!!.writeTo(buffer)
            buffer.readUtf8()
        } catch (e: IOException) {
            "did not work"
        }

    }


}
