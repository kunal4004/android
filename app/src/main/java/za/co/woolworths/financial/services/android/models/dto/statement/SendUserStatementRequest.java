package za.co.woolworths.financial.services.android.models.dto.statement;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SendUserStatementRequest {

	@SerializedName("channel")
	@Expose
	public String channel = "WWOneApp";
	@SerializedName("productOfferingId")
	@Expose
	public String productOfferingId;
	@SerializedName("to")
	@Expose
	public String to;
	@SerializedName("method")
	@Expose
	public String method = "email";
	@SerializedName("secure")
	@Expose
	public String secure = "yes";
	@SerializedName("documents")
	@Expose
	public USDocuments documents;
}
