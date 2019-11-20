package za.co.woolworths.financial.services.android.models;

import com.google.gson.JsonElement;

import java.util.ArrayList;

/**
 * Created by eesajacobs on 2016/12/27.
 */

public class JWTDecodedModel {

	public String iss;
	public String aud;
	public Double exp;
	public Double nbf;
	public String nonce;
	public Double iat;
	public String sid;
	public String sub;
	public Double auth_time;
	public String idp;
	public String updated_at;
	public String preferred_username;
	public Boolean email_verified;
	public ArrayList<String> email;
	public ArrayList<String> name; //this is sometimes an array
	public ArrayList<String> family_name; //this is sometimes an array
	public JsonElement AtgId; //this is sometimes an array
	public ArrayList<String> AtgSession; //json
	public String C2Id;
	public ArrayList<String> amr;
}
