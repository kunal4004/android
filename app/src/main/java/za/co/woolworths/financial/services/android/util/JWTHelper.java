package za.co.woolworths.financial.services.android.util;

import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
import za.co.woolworths.financial.services.android.models.JWTDecodedModel;

import java.security.Key;

/**
 * Created by eesajacobs on 2016/12/01.
 */

public class JWTHelper {
    static final String TAG = "JWTHelper";

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
            jwtDecodedModel = new Gson().fromJson(json, JWTDecodedModel.class);
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
}
