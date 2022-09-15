package za.co.woolworths.financial.services.android.models.service.event;

public class LoadState {

	private boolean loadComplete, sendDeliveryDetails;
	private String searchProduct;

	public boolean onLoanCompleted() {
		return loadComplete;
	}

	public void setLoadComplete(boolean loadComplete) {
		this.loadComplete = loadComplete;
	}

	public void setSearchProduct(String searchProduct) {
		this.searchProduct = searchProduct;
	}

	public String getSearchProduct() {
		return searchProduct;
	}

	public void setSendDeliveryDetails(boolean isUserBrowsingDash) {
		this.sendDeliveryDetails = isUserBrowsingDash;
	}
	public boolean isSendDeliveryDetails() {
		return this.sendDeliveryDetails;
	}

}
