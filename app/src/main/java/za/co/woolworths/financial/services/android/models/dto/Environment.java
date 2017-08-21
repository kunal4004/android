package za.co.woolworths.financial.services.android.models.dto;

/**
 * Created by denysvera on 2016/04/29.
 */
public class Environment {
	public String base_url;
	public String apiKey;
	public String sha1Password;
	public String ssoRedirectURI;
	public String stsURI;
	public String ssoRedirectURILogout;
	public String ssoUpdateDetailsRedirectUri;
	public String wwTodayURI;
	public int storeStockLocatorConfigStartRadius;
	public int storeStockLocatorConfigEndRadius;
	public boolean storeStockLocatorConfigFoodProducts;
	public boolean storeStockLocatorConfigClothingProducts;


	public String getApiId() {
		return apiKey;
	}

	public String getApiPassword() {
		return sha1Password;
	}

	public String getBase_url() {
		return base_url;
	}

	public String getSsoRedirectURI() {
		return ssoRedirectURI;
	}

	public void setStsURI(String stsURI) {
		this.stsURI = stsURI;
	}

	public void setApiId(String apiId) {
		this.apiKey = apiId;
	}

	public void setApiPassword(String sha1Password) {
		this.sha1Password = sha1Password;
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
}
