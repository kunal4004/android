package za.co.woolworths.financial.services.android.util;

import java.util.ArrayList;

import io.jsonwebtoken.Jwts;

/**
 * Created by eesajacobs on 2016/12/01.
 */

public class JWTHelper {

    public static void decode(String jwt){

        String[] parts = jwt.split("\\.");

        final String header = parts[0];
        final String payload = parts[1];
        final String secret = parts[2];

        try {

            Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(jwt);


            //OK, we can trust this JWT

        } catch (Exception e) {
            System.out.print(e.getStackTrace());
            //don't trust the JWT!
        }
    }
}
