package za.co.woolworths.financial.services.android.models.dto.statement;


public class PDF {

	private String docId;
	private String productOfferingId;
	private String accno;


	public PDF(String docId, String productOfferingId, String accno) {
		this.docId = docId;
		this.productOfferingId = productOfferingId;
		this.accno = accno;
	}

	public String getDocId() {
		return docId;
	}

	public String getProductOfferingId() {
		return productOfferingId;
	}

	public String getAccno() {
		return accno;
	}
}
