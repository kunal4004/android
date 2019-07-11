package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

/**
 * Created by denysvera on 2016/04/29.
 */
public class Environment {
	public String base_url;
	public String ssoRedirectURI;
	public String stsURI;
	public String ssoRedirectURILogout;
	public String ssoUpdateDetailsRedirectUri;
	public String wwTodayURI;
	public String authenticVersionStamp = "";
	public String authenticVersionReleaseNote;
	public int storeStockLocatorConfigStartRadius;
	public int storeStockLocatorConfigEndRadius;
	public boolean storeStockLocatorConfigFoodProducts;
	public boolean storeStockLocatorConfigClothingProducts;
	public JsonElement storeCardBlockReasons;
	public long emailSizeKB;

	@SerializedName("splashScreen.display")
	public boolean splashScreenDisplay;
	@SerializedName("splashScreen.persist")
	public boolean splashScreenPersist;
	@SerializedName("splashScreen.text")
	public String splashScreenText;

	public String getBase_url() {
		return base_url;
	}

	public String getSsoRedirectURI() {
		return ssoRedirectURI;
	}

	public void setStsURI(String stsURI) {
		this.stsURI = stsURI;
	}

	public void setBase_url(String base_url) {
		this.base_url = base_url;
	}

	public void setSsoRedirectURI(String ssoRedirectURI) {
		this.ssoRedirectURI = ssoRedirectURI;
	}

	public String getStsURI() {
		return stsURI;
	}

	public String getSsoRedirectURILogout() {
		return ssoRedirectURILogout;
	}

	public void setSsoRedirectURILogout(String ssoRedirectURILogout) {
		this.ssoRedirectURILogout = ssoRedirectURILogout;
	}

	public String getWwTodayURI() {
		return wwTodayURI;
	}

	public void setWwTodayURI(String wwTodayURI) {
		this.wwTodayURI = wwTodayURI;
	}

	public String getSsoUpdateDetailsRedirectUri() {
		return ssoUpdateDetailsRedirectUri;
	}

	public int getStoreStockLocatorConfigStartRadius() {
		return storeStockLocatorConfigStartRadius;
	}

	public void setStoreStockLocatorConfigStartRadius(int storeStockLocatorConfigStartRadius) {
		this.storeStockLocatorConfigStartRadius = storeStockLocatorConfigStartRadius;
	}

	public int getStoreStockLocatorConfigEndRadius() {
		return storeStockLocatorConfigEndRadius;
	}

	public void setStoreStockLocatorConfigEndRadius(int storeStockLocatorConfigEndRadius) {
		this.storeStockLocatorConfigEndRadius = storeStockLocatorConfigEndRadius;
	}

	public boolean storeStockLocatorConfigFoodProducts() {
		return storeStockLocatorConfigFoodProducts;
	}

	public void setStoreStockLocatorConfigFoodProducts(boolean storeStockLocatorConfigFoodProducts) {
		this.storeStockLocatorConfigFoodProducts = storeStockLocatorConfigFoodProducts;
	}

	public boolean storeStockLocatorConfigClothingProducts() {
		return storeStockLocatorConfigClothingProducts;
	}

	public void setStoreStockLocatorConfigClothingProducts(boolean storeStockLocatorConfigClothingProducts) {
		this.storeStockLocatorConfigClothingProducts = storeStockLocatorConfigClothingProducts;
	}

	public long getEmailSizeKB() {
		return emailSizeKB;
	}

	public void setEmailSizeKB(long emailSizeKB) {
		this.emailSizeKB = emailSizeKB;
	}

	public boolean isSplashScreenDisplay() {
		return splashScreenDisplay;
	}

	public boolean isSplashScreenPersist() {
		return splashScreenPersist;
	}

	public String getSplashScreenText() {
		return splashScreenText;
	}

	public String getAuthenticVersionStamp() {
		return authenticVersionStamp;
	}

	public void setAuthenticVersionStamp(String authenticVersionStamp) {
		this.authenticVersionStamp = authenticVersionStamp;
	}

	public String getAuthenticVersionReleaseNote() {
		return authenticVersionReleaseNote;
	}
}
