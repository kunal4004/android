package za.co.woolworths.financial.services.android.util;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by eesajacobs on 2016/07/25.
 */
public abstract class HttpAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    public static final String TAG = "HttpAsyncTask";

    public enum HttpErrorCode {
        NETWORK_UNREACHABLE,
        UNKOWN_ERROR
    }

    private Class type;
    private final List<String> jsonMimeTypes;

    protected abstract Result httpDoInBackground(Params... params);

    protected abstract Result httpError(String errorMessage, HttpErrorCode httpErrorCode);

    protected abstract Class<Result> httpDoInBackgroundReturnType();

    protected HttpAsyncTask() {
        jsonMimeTypes = Arrays.asList(new String[]{
                "application/json",
                "application/json; charset=utf-8"
        });
    }

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Result doInBackground(Params... params) {
        Result result = null;
        try {
            result = getResultFromDoInBackground(params);
        } catch (SocketTimeoutException e) {
            result = httpError("SocketTimeoutException", HttpErrorCode.NETWORK_UNREACHABLE);
        } catch (ConnectException e) {
            result = httpError("ConnectException", HttpErrorCode.NETWORK_UNREACHABLE);
        } catch (RuntimeExecutionException e) {
            result = httpError("RuntimeExecutionException", HttpErrorCode.UNKOWN_ERROR);
        } finally {
            if (result == null) {
                cancel(false);
            }
        }
        return result;
    }

    private Result getResultFromDoInBackground(Params... params) throws SocketTimeoutException,
            ConnectException, RuntimeExecutionException {
        Result result = null;
        try {

            result = httpDoInBackground(params);

        } catch (RetrofitError retrofitError) {
            final RetrofitError.Kind kind = retrofitError.getKind();
            final Response response = retrofitError.getResponse();
            final Throwable cause = retrofitError.getCause();

            if (kind == RetrofitError.Kind.NETWORK) {
                if (retrofitError.getCause() instanceof SocketTimeoutException) {
                    throw new SocketTimeoutException("SocketTimeoutException");
                } else {
                    throw new ConnectException("ConnectException");
                }
            } else if (response != null && this.jsonMimeTypes.contains(response.getBody().mimeType())) {
                try {
                    //check if retrofitError is kind of error object.
                    //if error object, return error description instead of Result's json
                    InputStreamReader is = new InputStreamReader(retrofitError.getResponse().getBody().in());
                    result = new Gson().fromJson(is, this.httpDoInBackgroundReturnType());
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                } finally {
                    if (result == null) {
                        result = httpError(retrofitError.getMessage(), HttpErrorCode.UNKOWN_ERROR);
                    }
                }
            } else {
                throw new SocketTimeoutException("SocketTimeoutException");
            }
        }
        return result;
    }
}