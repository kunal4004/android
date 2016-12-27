package za.co.woolworths.financial.services.android.util;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;

import retrofit.RetrofitError;
import za.co.woolworths.financial.services.android.models.dto.LoginResponse;

/**
 * Created by eesajacobs on 2016/07/25.
 */
public abstract class HttpAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result>{

    public static enum HttpErrorCode {
        NETWORK_UNREACHABLE,
        UNKOWN_ERROR
    }

    private Class type;
    protected abstract Result httpDoInBackground(Params... params);
    protected abstract Result httpError(String errorMessage, HttpErrorCode httpErrorCode);
    protected abstract Class<Result> httpDoInBackgroundReturnType();


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Result doInBackground(Params... params) {
        Result result = null;
        //get request from sqllite
        int apiRequestDaoId = -1;

        // httpUriRequest = params[0];
        //persist doesn't exist, proceed with service call
        if(apiRequestDaoId == 0 || apiRequestDaoId == -1){
            try{
                result = getResultFromDoInBackground(params);
            }
            catch (Exception e){
                result = httpError("An unknown error occured.", HttpErrorCode.UNKOWN_ERROR);
            }
            finally {
                if(result == null){
                    cancel(false);
                }
            }

            //save the respose
        }else{
            //get persistence record from sqllite
        }
        return result;
    }

    private Result getResultFromDoInBackground(Params... params) throws Exception{
        Result result = null;
        try{

            result = httpDoInBackground(params);

        }
        catch (RetrofitError retrofitError){
            if(retrofitError.getKind() == RetrofitError.Kind.NETWORK){

                if(retrofitError.getCause() instanceof SocketTimeoutException)
                    result = httpError("Request timed out. Please try again later.", HttpErrorCode.NETWORK_UNREACHABLE);
                else
                    result = httpError("Please ensure that your device is connected to the internet", HttpErrorCode.NETWORK_UNREACHABLE);
            }
            else if (retrofitError.getResponse() != null && retrofitError.getResponse().getBody().mimeType().equals("application/json")){
                try{
                    //check if retrofitError is kind of error object.
                    //if error object, return error description instead of Result's json
                    InputStreamReader is = new InputStreamReader(retrofitError.getResponse().getBody().in());
                    result = new Gson().fromJson(is, this.httpDoInBackgroundReturnType());
                }
                catch (IOException e){

                }
                finally {
                    if(result == null){
                        result = httpError(retrofitError.getMessage(), HttpErrorCode.UNKOWN_ERROR);
                    }
                }
            }
        }

        return result;
    }
}