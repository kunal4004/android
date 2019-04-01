package za.co.absa.openbankingapi.woolworths.integration.dao;

import java.net.HttpCookie;

public class JSession {

	private String id;
	private HttpCookie cookie;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public HttpCookie getCookie() {
		return cookie;
	}

	public void setCookie(HttpCookie cookie) {
		this.cookie = cookie;
	}

	public JSession(){
	}

	public JSession(String id, HttpCookie cookie){

		this.id = id;
		this.cookie = cookie;
	}
}
