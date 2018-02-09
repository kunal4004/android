package za.co.woolworths.financial.services.android.models.dto.statement;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import za.co.woolworths.financial.services.android.models.dto.Response;

public class SendUserStatementResponse {

	@SerializedName("response")
	@Expose
	public Response response;
	@SerializedName("data")
	@Expose
	public List<EmailStatementResponse> data = null;
	@SerializedName("httpCode")
	@Expose
	public Integer httpCode;

}