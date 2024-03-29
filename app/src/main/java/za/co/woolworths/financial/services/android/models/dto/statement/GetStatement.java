package za.co.woolworths.financial.services.android.models.dto.statement;


public class GetStatement {

	private String docId;
	private String docDesc;
	private String productOfferingId;

	public GetStatement(String docId, String productOfferingId, String docDesc) {
		this.docId = docId;
		this.productOfferingId = productOfferingId;
		this.docDesc = docDesc;
	}

	public String getDocId() {
		return docId;
	}

	public String getProductOfferingId() {
		return productOfferingId;
	}

	public String getDocDesc() {
		return docDesc;
	}
}
