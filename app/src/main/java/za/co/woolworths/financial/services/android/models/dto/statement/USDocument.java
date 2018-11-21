package za.co.woolworths.financial.services.android.models.dto.statement;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class USDocument {
	@SerializedName("docType")
	@Expose
	public String docType;
	@SerializedName("docId")
	@Expose
	public String docId;
	@SerializedName("docDesc")
	@Expose
	public String docDesc;

	public USDocument(String docType, String docId, String docDesc) {
		this.docType = docType;
		this.docId = docId;
		this.docDesc = docDesc;
	}
}
