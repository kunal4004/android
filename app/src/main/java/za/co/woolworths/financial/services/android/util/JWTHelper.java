package za.co.woolworths.financial.services.android.util;

import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
import za.co.woolworths.financial.services.android.models.JWTDecodedModel;

import java.security.Key;

import static android.R.attr.data;

/**
 * Created by eesajacobs on 2016/12/01.
 */

public class JWTHelper {
    static final String TAG = "JWTHelper";
    //List of keys
    static final String[] keys={"name","family_name","AtgId","AtgSession","email"};

    public static JWTDecodedModel decode(String jwt){
        if(jwt.equals(""))
            return null;

        JWTDecodedModel jwtDecodedModel = null;

        try {
            String[] parts = jwt.split("\\.");
            final String header = parts[0];
            final String payload = parts[1];
            final String secret = parts[2];
            String json = getJson(payload);
            jwtDecodedModel = new Gson().fromJson(convertJSONStringTypeToArrayType(json), JWTDecodedModel.class);
        } catch (Exception e) {
            System.out.print(e.getStackTrace());
            //don't trust JWT!
        }
        return jwtDecodedModel;
    }

    private static String getJson(String strEncoded) throws UnsupportedEncodingException {
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }

    //1. Inspect if key is a string or array
    //2. If string, change the json object to an array
    public static String convertJSONStringTypeToArrayType(String jsonData)  {
        JSONObject jObject= null;
        try {
            jObject = new JSONObject(jsonData);
            for(int i=0;i<keys.length;i++)
            {
                if(jObject.get(keys[i]) instanceof String)
                {
                    JSONObject jsonObject=new JSONObject(jsonData);
                    String value=jsonObject.getString(keys[i].toString());
                    jsonObject.remove(keys[i]);
                    jsonObject.put(keys[i],new JSONArray().put(value));
                    jsonData=jsonObject.toString();
                }
            }
            return jsonData;
        } catch (JSONException e) {
            e.printStackTrace();
            return jsonData;
        }

    }
}
