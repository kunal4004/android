package za.co.woolworths.financial.services.android.models.service.event;

public class LoadState {

	private boolean loadComplete;
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
}
