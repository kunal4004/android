package za.co.woolworths.financial.services.android.models.dto.statement;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.Response;

public class StatementResponse {

	@SerializedName("response")
	@Expose
	public Response response;
	@SerializedName("data")
	@Expose
	public List<Statement> data = null;
	@SerializedName("httpCode")
	@Expose
	public Integer httpCode;
}
