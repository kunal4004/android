package za.co.woolworths.financial.services.android.models.dto.statement;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SendUserSatementData {

	@SerializedName("docId")
	@Expose
	public String docId;
	@SerializedName("docDesc")
	@Expose
	public String docDesc;
	@SerializedName("sent")
	@Expose
	public Boolean sent;
	@SerializedName("error")
	@Expose
	public String error;
}
