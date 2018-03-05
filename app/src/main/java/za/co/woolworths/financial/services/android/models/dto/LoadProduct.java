package za.co.woolworths.financial.services.android.models.dto;


import za.co.woolworths.financial.services.android.util.Utils;

public class LoadProduct {

	private boolean isBarcode;
	private int pageOffset;
	private String productId;
	private String searchProduct;

	public LoadProduct(boolean isBarcode, String productId) {
		this.isBarcode = isBarcode;
		this.productId = productId;
	}

	public LoadProduct(String searchProduct, boolean isBarcode) {
		this.isBarcode = isBarcode;
		this.searchProduct = searchProduct;
	}

	public boolean isBarcode() {
		return isBarcode;
	}

	public int getPageSize() {
		return Utils.PAGE_SIZE;
	}

	public String getProductId() {
		return productId;
	}

	public int getPageOffset() {
		return pageOffset;
	}

	public void setPageOffset(int pageOffset) {
		this.pageOffset = pageOffset;
	}

	public void setBarcode(boolean barcode) {
		isBarcode = barcode;
	}

	public String getSearchProduct() {
		return searchProduct;
	}
}
