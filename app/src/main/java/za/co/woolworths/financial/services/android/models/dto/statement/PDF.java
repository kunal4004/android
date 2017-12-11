package za.co.woolworths.financial.services.android.models.dto.statement;


public class PDF {

	private String docId;
	private String productOfferingId;

	public PDF(String docId, String productOfferingId) {
		this.docId = docId;
		this.productOfferingId = productOfferingId;
	}

	public String getDocId() {
		return docId;
	}

	public String getProductOfferingId() {
		return productOfferingId;
	}
}
